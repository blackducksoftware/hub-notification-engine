/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.actions;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsExtractor;

@Component
public class SlackJobDetailsExtractor extends JobDetailsExtractor {

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new SlackJobDetailsModel(
            jobId,
            extractFieldValueOrEmptyString(SlackDescriptor.KEY_WEBHOOK, configuredFieldsMap),
            extractFieldValueOrEmptyString(SlackDescriptor.KEY_CHANNEL_NAME, configuredFieldsMap),
            extractFieldValueOrEmptyString(SlackDescriptor.KEY_CHANNEL_USERNAME, configuredFieldsMap)
        );
    }

}