/*
 * alert-database
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
package com.synopsys.integration.alert.database.job.jira.cloud;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.JiraCloudJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldEntity;
import com.synopsys.integration.alert.database.job.jira.cloud.custom_field.JiraCloudJobCustomFieldRepository;

@Component
public class DefaultJiraCloudJobDetailsAccessor implements JiraCloudJobDetailsAccessor {
    private final JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository;
    private final JiraCloudJobCustomFieldRepository jiraCloudJobCustomFieldRepository;

    @Autowired
    public DefaultJiraCloudJobDetailsAccessor(JiraCloudJobDetailsRepository jiraCloudJobDetailsRepository, JiraCloudJobCustomFieldRepository jiraCloudJobCustomFieldRepository) {
        this.jiraCloudJobDetailsRepository = jiraCloudJobDetailsRepository;
        this.jiraCloudJobCustomFieldRepository = jiraCloudJobCustomFieldRepository;
    }

    @Override
    public Optional<JiraCloudJobDetailsModel> retrieveDetails(UUID jobId) {
        return jiraCloudJobDetailsRepository.findById(jobId).map(this::convertToModel);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraCloudJobDetailsEntity saveJiraCloudJobDetails(UUID jobId, JiraCloudJobDetailsModel jiraCloudJobDetails) {
        JiraCloudJobDetailsEntity jiraCloudJobDetailsToSave = new JiraCloudJobDetailsEntity(
            jobId,
            jiraCloudJobDetails.isAddComments(),
            jiraCloudJobDetails.getIssueCreatorEmail(),
            jiraCloudJobDetails.getProjectNameOrKey(),
            jiraCloudJobDetails.getIssueType(),
            jiraCloudJobDetails.getResolveTransition(),
            jiraCloudJobDetails.getReopenTransition()
        );
        JiraCloudJobDetailsEntity savedJobDetails = jiraCloudJobDetailsRepository.save(jiraCloudJobDetailsToSave);
        jiraCloudJobCustomFieldRepository.deleteByJobId(jobId);
        List<JiraCloudJobCustomFieldEntity> customFieldsToSave = jiraCloudJobDetails.getCustomFields()
                                                                     .stream()
                                                                     .map(model -> new JiraCloudJobCustomFieldEntity(savedJobDetails.getJobId(), model.getFieldName(), model.getFieldValue()))
                                                                     .collect(Collectors.toList());
        List<JiraCloudJobCustomFieldEntity> savedCustomFields = jiraCloudJobCustomFieldRepository.saveAll(customFieldsToSave);
        savedJobDetails.setJobCustomFields(savedCustomFields);
        return savedJobDetails;
    }

    public List<JiraJobCustomFieldModel> retrieveCustomFieldsForJob(UUID jobId) {
        return jiraCloudJobCustomFieldRepository.findByJobId(jobId)
                   .stream()
                   .map(customFieldEntity -> new JiraJobCustomFieldModel(customFieldEntity.getFieldName(), customFieldEntity.getFieldValue()))
                   .collect(Collectors.toList());
    }

    private JiraCloudJobDetailsModel convertToModel(JiraCloudJobDetailsEntity jobDetails) {
        List<JiraJobCustomFieldModel> customFields = jobDetails.getJobCustomFields()
                                                         .stream()
                                                         .map(entity -> new JiraJobCustomFieldModel(entity.getFieldName(), entity.getFieldValue()))
                                                         .collect(Collectors.toList());
        return new JiraCloudJobDetailsModel(
            jobDetails.getJobId(),
            jobDetails.getAddComments(),
            jobDetails.getIssueCreatorEmail(),
            jobDetails.getProjectNameOrKey(),
            jobDetails.getIssueType(),
            jobDetails.getResolveTransition(),
            jobDetails.getReopenTransition(),
            customFields
        );
    }

}