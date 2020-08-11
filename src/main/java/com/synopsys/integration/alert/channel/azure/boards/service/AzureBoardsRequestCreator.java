/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.azure.boards.service;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsSearchProperties;
import com.synopsys.integration.alert.common.channel.issuetracker.service.IssueTrackerRequestCreator;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

@Component
public class AzureBoardsRequestCreator extends IssueTrackerRequestCreator {
    public AzureBoardsRequestCreator(AzureBoardsMessageParser messageParser) {
        super(messageParser);
    }

    @Override
    protected AzureBoardsSearchProperties createIssueSearchProperties(String providerName, String providerUrl, LinkableItem topic, @Nullable LinkableItem subTopic, @Nullable ComponentItem componentItem, String additionalInfo) {
        String topLevelKey = createTopLevelKey(providerName, providerUrl, topic, subTopic);
        String componentLevelKey = createComponentLevelKey(componentItem, additionalInfo);
        return new AzureBoardsSearchProperties(topLevelKey, componentLevelKey);
    }

    private String createTopLevelKey(String providerName, String providerUrl, LinkableItem topic, @Nullable LinkableItem subTopic) {
        StringBuilder topLevelKeyBuilder = new StringBuilder();

        topLevelKeyBuilder.append("Provider=(");
        topLevelKeyBuilder.append(providerName);
        topLevelKeyBuilder.append(", ");
        topLevelKeyBuilder.append(providerUrl);
        topLevelKeyBuilder.append(')');

        topLevelKeyBuilder.append("Topic=(");
        appendLinkableItem(topLevelKeyBuilder, topic);
        topLevelKeyBuilder.append(')');

        if (null != subTopic) {
            topLevelKeyBuilder.append("SubTopic=(");
            appendLinkableItem(topLevelKeyBuilder, subTopic);
            topLevelKeyBuilder.append(')');
        }

        return topLevelKeyBuilder.toString();
    }

    private String createComponentLevelKey(@Nullable ComponentItem componentItem, String additionalInfo) {
        if (null == componentItem) {
            return null;
        }

        StringBuilder componentLevelKeyBuilder = new StringBuilder();

        componentLevelKeyBuilder.append("Category=(");
        componentLevelKeyBuilder.append(componentItem.getCategory());
        componentLevelKeyBuilder.append(')');

        componentLevelKeyBuilder.append("Component=(");
        appendLinkableItem(componentLevelKeyBuilder, componentItem.getComponent());
        componentLevelKeyBuilder.append(')');

        Optional<LinkableItem> subComponent = componentItem.getSubComponent();
        if (subComponent.isPresent()) {
            componentLevelKeyBuilder.append("SubComponent=(");
            appendLinkableItem(componentLevelKeyBuilder, subComponent.get());
            componentLevelKeyBuilder.append(')');
        }

        if (StringUtils.isNotBlank(additionalInfo)) {
            componentLevelKeyBuilder.append("AdditionalInfo=(");
            componentLevelKeyBuilder.append(additionalInfo);
            componentLevelKeyBuilder.append(')');
        }

        return componentLevelKeyBuilder.toString();
    }

    private void appendLinkableItem(StringBuilder stringBuilder, LinkableItem linkableItem) {
        stringBuilder.append(linkableItem.getName());
        stringBuilder.append(", ");
        stringBuilder.append(linkableItem.getValue());
        linkableItem.getUrl()
            .ifPresent(url -> {
                stringBuilder.append(", ");
                stringBuilder.append(url);
            });
    }

}