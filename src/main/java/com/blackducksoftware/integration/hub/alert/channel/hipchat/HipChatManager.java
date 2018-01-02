/**
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;

@Component
public class HipChatManager extends DistributionChannelManager<GlobalHipChatConfigEntity, HipChatDistributionConfigEntity, HipChatEvent, HipChatDistributionRestModel> {
    @Autowired
    public HipChatManager(final HipChatChannel distributionChannel, final GlobalHipChatRepository globalRepository, final HipChatDistributionRepository localRepository, final ObjectTransformer objectTransformer) {
        super(distributionChannel, globalRepository, localRepository, objectTransformer);
    }

    @Override
    public boolean isApplicable(final String supportedChannelName) {
        return SupportedChannels.HIPCHAT.equals(supportedChannelName);
    }

    @Override
    public HipChatEvent createChannelEvent(final ProjectData projectData, final Long commonDistributionConfigId) {
        return new HipChatEvent(projectData, commonDistributionConfigId);
    }

    @Override
    public String sendTestMessage(final HipChatDistributionRestModel restModel) throws AlertException {
        if (getDistributionChannel().getGlobalConfigEntity() != null) {
            return super.sendTestMessage(restModel);
        }
        return "ERROR: Missing global configuration!";
    }

    @Override
    public Class<HipChatDistributionConfigEntity> getDatabaseEntityClass() {
        return HipChatDistributionConfigEntity.class;
    }

}
