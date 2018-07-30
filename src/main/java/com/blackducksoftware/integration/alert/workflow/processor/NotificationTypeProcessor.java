/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.alert.workflow.processor;

import java.util.List;
import java.util.Set;

import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.provider.hub.HubProperties;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;

public abstract class NotificationTypeProcessor {
    private final Set<NotificationType> applicableNotificationTypes;

    public NotificationTypeProcessor(final Set<NotificationType> applicableNotificationTypes) {
        this.applicableNotificationTypes = applicableNotificationTypes;
    }

    public boolean isApplicable(final NotificationDetailResult notificationDetailResult) {
        final boolean isApplicable = applicableNotificationTypes.contains(notificationDetailResult.getType());
        return isApplicable;
    }

    public abstract List<NotificationModel> process(final HubProperties hubProperties, final NotificationDetailResult notificationDetailResult, final HubBucket bucket);
}
