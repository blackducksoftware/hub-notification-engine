/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.MessageBoardChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;

@Component
public class SlackChannelV2 extends MessageBoardChannel<SlackJobDetailsModel, SlackChannelMessageModel> {
    @Autowired
    protected SlackChannelV2(SlackChannelMessageConverter slackChannelMessageConverter, SlackChannelMessageSender slackChannelMessageSender) {
        super(slackChannelMessageConverter, slackChannelMessageSender);
    }

}