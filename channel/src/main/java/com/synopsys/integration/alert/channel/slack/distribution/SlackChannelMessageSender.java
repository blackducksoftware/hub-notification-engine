/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.api.ChannelMessageSender;
import com.synopsys.integration.alert.channel.util.RestChannelUtility;
import com.synopsys.integration.alert.channel.util.RestMessageResponse;
import com.synopsys.integration.alert.channel.util.RestResponseStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.message.model.MessageResultStatus;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.rest.request.Request;

@Component
public class SlackChannelMessageSender implements ChannelMessageSender<SlackJobDetailsModel, SlackChannelMessageModel, MessageResult> {
    public static final String SLACK_DEFAULT_USERNAME = "Alert";

    private final RestChannelUtility restChannelUtility;
    private final SlackChannelKey slackChannelKey;

    @Autowired
    public SlackChannelMessageSender(RestChannelUtility restChannelUtility, SlackChannelKey slackChannelKey) {
        this.restChannelUtility = restChannelUtility;
        this.slackChannelKey = slackChannelKey;
    }

    @Override
    public MessageResult sendMessages(SlackJobDetailsModel slackJobDetails, List<SlackChannelMessageModel> channelMessages) {
        String webhook = slackJobDetails.getWebhook();
        String channelName = slackJobDetails.getChannelName();
        String channelUsername = Optional.ofNullable(slackJobDetails.getChannelUsername())
                                     .orElse(SLACK_DEFAULT_USERNAME);
        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        List<Request> requests = channelMessages.stream()
                                     .map(message -> createRequestsForMessage(channelName, channelUsername, webhook, message.getMarkdownContent(), requestHeaders))
                                     .collect(Collectors.toList());

        RestMessageResponse messageResponse = restChannelUtility.sendMessage(requests, slackChannelKey.getUniversalKey());
        if (messageResponse.hasErrors()) {
            List<AlertFieldStatus> responseErrors = new ArrayList<>();
            Map<Request, RestResponseStatus> errorMessageResponses = messageResponse.getRequestsWithFailures();

            for (Map.Entry<Request, RestResponseStatus> entry : errorMessageResponses.entrySet()) {
                //TODO: May not be able to use AlertFieldStatus appropriately. May need a new object
                //responseErrors.add(AlertFieldStatus.error("", entry.getValue().getMessage()));
            }

            //TODO: Fix this string
            return new MessageResult(MessageResultStatus.FAILURE, "Error while sending x out of y Slack message(s)", responseErrors);
        }

        return new MessageResult(String.format("Successfully sent %d Slack message(s)", requests.size()));
    }

    private Request createRequestsForMessage(String channelName, String channelUsername, String webhook, String message, Map<String, String> requestHeaders) {
        String jsonString = getJsonString(message, channelName, channelUsername);
        return restChannelUtility.createPostMessageRequest(webhook, requestHeaders, jsonString);
    }

    private String getJsonString(String markdownMessage, String channel, String username) {
        JsonObject json = new JsonObject();
        json.addProperty("text", markdownMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

}
