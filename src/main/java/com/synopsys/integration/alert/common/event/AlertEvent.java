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
package com.synopsys.integration.alert.common.event;

import java.util.UUID;

public class AlertEvent {

    private final String eventId;
    private final String destination;
    private final Long notificationId;

    public AlertEvent(final String destination, final Long notificationId) {
        this.eventId = UUID.randomUUID().toString();
        this.destination = destination;
        this.notificationId = notificationId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDestination() {
        return destination;
    }

    public Long getNotificationId() { return notificationId;}
}
