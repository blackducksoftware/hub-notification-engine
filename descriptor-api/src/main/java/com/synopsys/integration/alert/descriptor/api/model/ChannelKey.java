/**
 * descriptor-api
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
package com.synopsys.integration.alert.descriptor.api.model;

import java.util.HashMap;
import java.util.Map;

import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

public class ChannelKey extends DescriptorKey {
    public static final AzureBoardsChannelKey AZURE_BOARDS = new AzureBoardsChannelKey();
    public static final EmailChannelKey EMAIL = new EmailChannelKey();
    public static final JiraCloudChannelKey JIRA_CLOUD = new JiraCloudChannelKey();
    public static final JiraServerChannelKey JIRA_SERVER = new JiraServerChannelKey();
    public static final MsTeamsKey MS_TEAMS = new MsTeamsKey();
    public static final SlackChannelKey SLACK = new SlackChannelKey();

    private static final Map<String, ChannelKey> channels = new HashMap<>();
    private static void putChannel(ChannelKey channelKey) {
        channels.put(channelKey.getUniversalKey(), channelKey);
    }

    public static ChannelKey getChannelKey(String universalKey) {
        return channels.get(universalKey);
    }

    static {
        putChannel(AZURE_BOARDS);
        putChannel(EMAIL);
        putChannel(JIRA_CLOUD);
        putChannel(JIRA_SERVER);
        putChannel(MS_TEAMS);
        putChannel(SLACK);
    }

    public ChannelKey(String universalKey, String displayName) {
        super(universalKey, displayName);
    }

}
