/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public interface ChannelDistributionTestAction {
    MessageResult testConfig(
        DistributionJobModel distributionJobModel,
        @Nullable String customTopic,
        @Nullable String customMessage,
        @Nullable String destination
    ) throws AlertException;

}
