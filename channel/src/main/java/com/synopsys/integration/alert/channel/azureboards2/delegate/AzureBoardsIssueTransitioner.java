/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.azureboards2.delegate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.issue.search.ExistingIssueDetails;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueCommenter;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.channel.api.issue.send.IssueTrackerIssueTransitioner;
import com.synopsys.integration.alert.channel.azureboards2.AzureBoardsWorkItemTypeStateRetriever;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.azure.boards.common.http.HttpServiceException;
import com.synopsys.integration.azure.boards.common.service.state.WorkItemTypeStateResponseModel;
import com.synopsys.integration.azure.boards.common.service.workitem.AzureWorkItemService;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperation;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemElementOperationModel;
import com.synopsys.integration.azure.boards.common.service.workitem.request.WorkItemRequest;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemFieldsWrapper;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseFields;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public class AzureBoardsIssueTransitioner extends IssueTrackerIssueTransitioner<Integer> {
    public static final String WORK_ITEM_STATE_CATEGORY_PROPOSED = "Proposed";
    public static final String WORK_ITEM_STATE_CATEGORY_IN_PROGRESS = "InProgress";
    public static final String WORK_ITEM_STATE_CATEGORY_RESOLVED = "Resolved";
    public static final String WORK_ITEM_STATE_CATEGORY_COMPLETED = "Completed";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final String organizationName;
    private final AzureBoardsJobDetailsModel distributionDetails;
    private final AzureWorkItemService workItemService;
    private final AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever;

    public AzureBoardsIssueTransitioner(
        IssueTrackerIssueCommenter<Integer> commenter,
        IssueTrackerIssueResponseCreator<Integer> issueResponseCreator,
        Gson gson,
        String organizationName,
        AzureBoardsJobDetailsModel distributionDetails,
        AzureWorkItemService workItemService,
        AzureBoardsWorkItemTypeStateRetriever workItemTypeStateRetriever
    ) {
        super(commenter, issueResponseCreator);
        this.gson = gson;
        this.organizationName = organizationName;
        this.distributionDetails = distributionDetails;
        this.workItemService = workItemService;
        this.workItemTypeStateRetriever = workItemTypeStateRetriever;
    }

    @Override
    protected Optional<String> retrieveJobTransitionName(IssueOperation issueOperation) {
        String nullableTransitionName = null;
        if (IssueOperation.OPEN.equals(issueOperation)) {
            nullableTransitionName = distributionDetails.getWorkItemReopenState();
        } else if (IssueOperation.RESOLVE.equals(issueOperation)) {
            nullableTransitionName = distributionDetails.getWorkItemCompletedState();
        }
        return Optional.ofNullable(nullableTransitionName).filter(StringUtils::isNotBlank);
    }

    @Override
    protected boolean isTransitionRequired(ExistingIssueDetails<Integer> existingIssueDetails, IssueOperation issueOperation) throws AlertException {
        Integer issueId = existingIssueDetails.getIssueId();
        WorkItemResponseModel workItem = retrieveWorkItem(issueId);

        List<WorkItemTypeStateResponseModel> availableStates = retrieveAvailableStates(issueId);
        Map<String, String> stateNameToCategory = mapStateNameToCategory(availableStates);

        WorkItemFieldsWrapper fieldsWrapper = workItem.createFieldsWrapper(gson);
        Optional<String> optionalCurrentState = fieldsWrapper.getField(WorkItemResponseFields.System_State);
        if (optionalCurrentState.isPresent()) {
            String workItemStateCategory = stateNameToCategory.get(optionalCurrentState.get());
            if (IssueOperation.OPEN.equals(issueOperation) && WORK_ITEM_STATE_CATEGORY_PROPOSED.equals(workItemStateCategory)) {
                return false;
            } else if (IssueOperation.RESOLVE.equals(issueOperation) && WORK_ITEM_STATE_CATEGORY_COMPLETED.equals(workItemStateCategory)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void findAndPerformTransition(ExistingIssueDetails<Integer> existingIssueDetails, String transitionName) {
        WorkItemElementOperationModel replaceSystemStateField = WorkItemElementOperationModel.fieldElement(WorkItemElementOperation.REPLACE, WorkItemResponseFields.System_State, transitionName);
        WorkItemRequest request = new WorkItemRequest(List.of(replaceSystemStateField));

        Integer issueId = existingIssueDetails.getIssueId();
        try {
            workItemService.updateWorkItem(organizationName, distributionDetails.getProjectNameOrId(), issueId, request);
        } catch (HttpServiceException ex) {
            // TODO determine if catching this exception is correct
            logger.error("Error transitioning work item {} to {}: cause: {}", issueId, transitionName, ex);
        }
    }

    private WorkItemResponseModel retrieveWorkItem(Integer issueId) throws AlertException {
        try {
            return workItemService.getWorkItem(organizationName, issueId);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("Failed to retrieve available state categories from Azure. Work Item ID: %s", issueId), e);
        }
    }

    private Map<String, String> mapStateNameToCategory(List<WorkItemTypeStateResponseModel> workItemTypeStates) {
        return workItemTypeStates
                   .stream()
                   .collect(Collectors.toMap(WorkItemTypeStateResponseModel::getName, WorkItemTypeStateResponseModel::getCategory));
    }

    private List<WorkItemTypeStateResponseModel> retrieveAvailableStates(Integer issueId) throws AlertException {
        try {
            return workItemTypeStateRetriever.retrieveAvailableWorkItemStates(organizationName, issueId);
        } catch (HttpServiceException e) {
            throw new AlertException(String.format("Failed to retrieve available work item states from Azure. Work Item ID: %s", issueId), e);
        }
    }

}
