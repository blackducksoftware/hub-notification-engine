/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.web.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryEntity;
import com.blackducksoftware.integration.alert.database.audit.AuditEntryRepository;
import com.blackducksoftware.integration.alert.database.audit.AuditNotificationRepository;
import com.blackducksoftware.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.exception.AlertFieldException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class CommonDistributionConfigActions extends DistributionConfigActions<CommonDistributionConfigEntity, CommonDistributionConfigRestModel, CommonDistributionRepository> {
    private final AuditEntryRepository auditEntryRepository;
    private final AuditNotificationRepository auditNotificationRepository;

    @Autowired
    public CommonDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final AuditEntryRepository auditEntryRepository,
            final ConfiguredProjectsActions configuredProjectsActions, final NotificationTypesActions notificationTypesActions, final ObjectTransformer objectTransformer,
            final AuditNotificationRepository auditNotificationRepository) {
        super(CommonDistributionConfigEntity.class, CommonDistributionConfigRestModel.class, commonDistributionRepository, commonDistributionRepository, configuredProjectsActions, notificationTypesActions, objectTransformer);
        this.auditEntryRepository = auditEntryRepository;
        this.auditNotificationRepository = auditNotificationRepository;
    }

    @Override
    public List<CommonDistributionConfigRestModel> getConfig(final Long id) throws AlertException {
        final List<CommonDistributionConfigRestModel> restModels = super.getConfig(id);
        addAuditEntryInfoToRestModels(restModels);
        return restModels;
    }

    private void addAuditEntryInfoToRestModels(final List<CommonDistributionConfigRestModel> restModels) {
        for (final CommonDistributionConfigRestModel restModel : restModels) {
            addAuditEntryInfoToRestModel(restModel);
        }
    }

    private void addAuditEntryInfoToRestModel(final CommonDistributionConfigRestModel restModel) {
        String lastRan = "Unknown";
        String status = "Unknown";
        final Long id = getObjectTransformer().stringToLong(restModel.getId());
        final AuditEntryEntity lastRanEntry = auditEntryRepository.findFirstByCommonConfigIdOrderByTimeLastSentDesc(id);
        if (lastRanEntry != null) {
            lastRan = getObjectTransformer().objectToString(lastRanEntry.getTimeLastSent());
            status = lastRanEntry.getStatus().getDisplayName();
        }
        restModel.setLastRan(lastRan);
        restModel.setStatus(status);
    }

    @Override
    public CommonDistributionConfigEntity saveConfig(final CommonDistributionConfigRestModel restModel) throws AlertException {
        if (restModel != null) {
            try {
                CommonDistributionConfigEntity createdEntity = getObjectTransformer().configRestModelToDatabaseEntity(restModel, getDatabaseEntityClass());
                if (createdEntity != null) {
                    createdEntity = getCommonDistributionRepository().save(createdEntity);
                    if (Boolean.TRUE.equals(createdEntity.getFilterByProject())) {
                        getConfiguredProjectsActions().saveConfiguredProjects(createdEntity.getId(), restModel.getConfiguredProjects());
                    }
                    getNotificationTypesActions().saveNotificationTypes(createdEntity.getId(), restModel.getNotificationTypes());
                    return createdEntity;
                }
            } catch (final Exception e) {
                throw new AlertException(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void deleteConfig(final Long id) {
        if (id != null) {
            deleteAuditEntries(id);
            getCommonDistributionRepository().deleteById(id);
            getConfiguredProjectsActions().cleanUpConfiguredProjects();
            getNotificationTypesActions().removeOldNotificationTypes(id);
        }
    }

    @Override
    public String channelTestConfig(final CommonDistributionConfigRestModel restModel) throws IntegrationException {
        // Should not be tested
        return "Configuration should not be tested at this level.";
    }

    @Override
    public CommonDistributionConfigRestModel constructRestModel(final CommonDistributionConfigEntity entity) throws AlertException {
        final Optional<CommonDistributionConfigEntity> foundEntity = getCommonDistributionRepository().findById(entity.getId());
        if (foundEntity.isPresent()) {
            return constructRestModel(foundEntity.get(), null);
        }
        return null;
    }

    @Override
    public CommonDistributionConfigRestModel constructRestModel(final CommonDistributionConfigEntity commonEntity, final CommonDistributionConfigEntity distributionEntity) throws AlertException {
        final CommonDistributionConfigRestModel restModel = getObjectTransformer().databaseEntityToConfigRestModel(commonEntity, CommonDistributionConfigRestModel.class);
        restModel.setConfiguredProjects(getConfiguredProjectsActions().getConfiguredProjects(commonEntity));
        restModel.setNotificationTypes(getNotificationTypesActions().getNotificationTypes(commonEntity));
        return restModel;
    }

    @Override
    public String getDistributionName() {
        // This does not have a distribution name
        return null;
    }

    private void deleteAuditEntries(final Long configID) {
        final List<AuditEntryEntity> auditEntryList = auditEntryRepository.findByCommonConfigId(configID);
        auditEntryList.forEach((auditEntry) -> {
            final List<AuditNotificationRelation> relationList = auditNotificationRepository.findByAuditEntryId(auditEntry.getId());
            auditNotificationRepository.deleteAll(relationList);
        });
        auditEntryRepository.deleteAll(auditEntryList);
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) throws AlertFieldException {
        // This does not validate anything
    }
}
