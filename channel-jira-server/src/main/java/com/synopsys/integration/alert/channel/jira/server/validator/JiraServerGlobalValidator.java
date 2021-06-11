/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class JiraServerGlobalValidator extends GlobalValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        FieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_URL).ifPresent(statuses::add);
        FieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_USERNAME).ifPresent(statuses::add);
        FieldValidator.validateIsARequiredField(fieldModel, JiraServerDescriptor.KEY_SERVER_PASSWORD).ifPresent(statuses::add);

        return statuses;
    }
}