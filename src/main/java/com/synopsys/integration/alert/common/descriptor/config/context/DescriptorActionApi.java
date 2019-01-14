/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.context;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.event.DistributionEvent;
import com.synopsys.integration.alert.common.configuration.CommonDistributionConfiguration;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.RestConstants;

public abstract class DescriptorActionApi {

    public void validateConfig(final Collection<ConfigField> descriptorFields, final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        for (final ConfigField field : descriptorFields) {
            final String fieldKey = field.getKey();
            final Optional<FieldValueModel> optionalField = fieldModel.getField(fieldKey);
            if (field.isRequired()) {
                final boolean missingValue = optionalField.isEmpty() || optionalField.filter(valueModel -> valueModel.hasValues()).isEmpty();
                if (missingValue) {
                    fieldErrors.put(fieldKey, "Required Field Missing");
                }
            }

            // field is present now validate the field
            if (!fieldErrors.containsKey(fieldKey) && optionalField.isPresent()) {
                final Collection<String> validationErrors = field.validate(optionalField.get());
                if (!validationErrors.isEmpty()) {
                    fieldErrors.put(fieldKey, StringUtils.join(validationErrors, ","));
                }
            }
        }
    }

    public TestConfigModel createTestConfigModel(final FieldModel fieldModel, final String destination) throws AlertFieldException {
        return new TestConfigModel(fieldModel, destination);
    }

    public abstract void testConfig(final Collection<ConfigField> configFields, final TestConfigModel testConfig) throws IntegrationException;

    public DistributionEvent createChannelEvent(final CommonDistributionConfiguration commmonDistributionConfig, final AggregateMessageContent messageContent) {
        return new DistributionEvent(commmonDistributionConfig.getId().toString(), commmonDistributionConfig.getChannelName(), RestConstants.formatDate(new Date()), commmonDistributionConfig.getProviderName(),
            commmonDistributionConfig.getFormatType().name(), messageContent, commmonDistributionConfig.getFieldAccessor());
    }

    public AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, List.of());
    }

    protected FieldModel createFieldModelCopy(final FieldModel fieldModel) {
        final HashMap<String, FieldValueModel> fields = new HashMap<>();
        fields.putAll(fieldModel.getKeyToValues());

        final FieldModel modelToSave = new FieldModel(fieldModel.getDescriptorName(), fieldModel.getContext(), fields);
        modelToSave.setId(fieldModel.getId());
        return modelToSave;
    }

    public FieldModel readConfig(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel updateConfig(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel saveConfig(final FieldModel fieldModel) {
        return fieldModel;
    }

    public FieldModel deleteConfig(final FieldModel fieldModel) {
        return fieldModel;
    }

    protected void validateFieldFormatting(final Collection<ConfigField> descriptorFields, final FieldModel fieldModel) throws AlertFieldException {
        final Map<String, String> fieldErrors = new HashMap<>();
        validateConfig(descriptorFields, fieldModel, fieldErrors);

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(fieldErrors);
        }
    }

}
