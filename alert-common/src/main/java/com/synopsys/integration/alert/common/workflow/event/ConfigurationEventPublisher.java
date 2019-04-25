/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.workflow.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class ConfigurationEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ConfigurationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishConfigurationEvent(final FieldModel fieldModel, final ConfigurationEventType eventType) {
        final ConfigurationEvent configurationEvent = new ConfigurationEvent(fieldModel, eventType);
        applicationEventPublisher.publishEvent(configurationEvent);
    }

    public void publishConfigurationEvent(final FieldModel fieldModel, final String descriptorName, final String context, final ConfigurationEventType eventType) {
        final ConfigurationEvent configurationEvent = new ConfigurationEvent(fieldModel, descriptorName, context, eventType);
        applicationEventPublisher.publishEvent(configurationEvent);
    }

    public void publishConfigurationEvent(final String descriptorName, final String context, final ConfigurationEventType eventType) {
        final ConfigurationEvent configurationEvent = new ConfigurationEvent(descriptorName, context, eventType);
        applicationEventPublisher.publishEvent(configurationEvent);
    }

}
