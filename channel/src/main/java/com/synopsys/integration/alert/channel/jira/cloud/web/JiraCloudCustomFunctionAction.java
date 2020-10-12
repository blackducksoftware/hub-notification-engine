/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.cloud.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudChannelKey;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.util.JiraPluginCheckUtil;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.CustomFunctionAction;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.FieldValidationUtility;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.rest.HttpServletContentWrapper;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.rest.service.PluginManagerService;

@Component
public class JiraCloudCustomFunctionAction extends CustomFunctionAction<String> {
    private final Logger logger = LoggerFactory.getLogger(JiraCloudCustomFunctionAction.class);

    private final JiraCloudChannelKey jiraChannelKey;
    private final ConfigurationAccessor configurationAccessor;
    private final Gson gson;

    @Autowired
    public JiraCloudCustomFunctionAction(AuthorizationManager authorizationManager, JiraCloudChannelKey jiraChannelKey, ConfigurationAccessor configurationAccessor, Gson gson, DescriptorMap descriptorMap,
        FieldValidationUtility fieldValidationUtility) {
        super(JiraCloudDescriptor.KEY_JIRA_CONFIGURE_PLUGIN, authorizationManager, descriptorMap, fieldValidationUtility);
        this.jiraChannelKey = jiraChannelKey;
        this.configurationAccessor = configurationAccessor;
        this.gson = gson;
    }

    @Override
    public ActionResponse<String> createActionResponse(FieldModel fieldModel, HttpServletContentWrapper ignoredServletContent) {
        JiraCloudProperties jiraProperties = createJiraProperties(fieldModel);
        try {
            JiraCloudServiceFactory jiraServicesCloudFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            PluginManagerService jiraAppService = jiraServicesCloudFactory.createPluginManagerService();
            int statusCode = jiraAppService.installMarketplaceCloudApp(JiraConstants.JIRA_APP_KEY);
            if (!HttpStatusCodes.isSuccess(statusCode)) {
                return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The Jira Cloud server responded with error code: " + statusCode);
            }

            boolean jiraPluginInstalled = JiraPluginCheckUtil.checkIsAppInstalledAndRetryIfNecessary(jiraAppService);
            if (!jiraPluginInstalled) {
                return new ActionResponse<>(HttpStatus.NOT_FOUND, String.format(
                    "Unable to confirm successful installation of the Jira Cloud '%s' plugin. Please verify the installation on your Jira Cloud server.", JiraConstants.JIRA_ALERT_APP_NAME));
            }
            return new ActionResponse<>(HttpStatus.OK, String.format("Successfully installed the '%s' plugin on Jira Cloud", JiraConstants.JIRA_ALERT_APP_NAME));
        } catch (IntegrationException e) {
            logger.error("There was an issue connecting to Jira Cloud", e);
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, "The following error occurred when connecting to Jira Cloud: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("Thread was interrupted while validating jira install.", e);
            Thread.currentThread().interrupt();
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, String.format("Thread was interrupted while validating Jira '%s' plugin installation: %s", JiraConstants.JIRA_ALERT_APP_NAME, e.getMessage()));
        }
    }

    private JiraCloudProperties createJiraProperties(FieldModel fieldModel) {
        String url = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_URL).orElse("");
        String username = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS).orElse("");
        String accessToken = fieldModel.getFieldValueModel(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN)
                                 .map(this::getAppropriateAccessToken)
                                 .orElse("");
        boolean pluginCheckDisabled = fieldModel.getFieldValue(JiraCloudDescriptor.KEY_JIRA_DISABLE_PLUGIN_CHECK)
                                          .map(Boolean::parseBoolean)
                                          .orElse(false);

        return new JiraCloudProperties(url, accessToken, username, pluginCheckDisabled);
    }

    private String getAppropriateAccessToken(FieldValueModel fieldAccessToken) {
        String accessToken = fieldAccessToken.getValue().orElse("");
        boolean accessTokenSet = fieldAccessToken.getIsSet();
        if (StringUtils.isBlank(accessToken) && accessTokenSet) {
            try {
                return configurationAccessor.getConfigurationsByDescriptorKeyAndContext(jiraChannelKey, ConfigContextEnum.GLOBAL)
                           .stream()
                           .findFirst()
                           .flatMap(configurationModel -> configurationModel.getField(JiraCloudDescriptor.KEY_JIRA_ADMIN_API_TOKEN))
                           .flatMap(ConfigurationFieldModel::getFieldValue)
                           .orElse("");

            } catch (AlertDatabaseConstraintException e) {
                logger.error("Unable to retrieve existing Jira configuration.");
            }
        }
        return accessToken;
    }

}