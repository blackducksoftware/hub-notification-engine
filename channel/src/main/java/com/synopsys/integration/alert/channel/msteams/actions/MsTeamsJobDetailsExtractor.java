/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.actions;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsExtractor;

@Component
public class MsTeamsJobDetailsExtractor extends JobDetailsExtractor {
    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new MSTeamsJobDetailsModel(jobId, extractFieldValueOrEmptyString(MsTeamsDescriptor.KEY_WEBHOOK, configuredFieldsMap));
    }

}