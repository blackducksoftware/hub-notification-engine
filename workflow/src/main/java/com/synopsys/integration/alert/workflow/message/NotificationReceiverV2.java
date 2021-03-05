/*
 * workflow
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.workflow.message;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.AlertDefaultEventListener;
import com.synopsys.integration.alert.common.event.NotificationReceivedEventV2;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.workflow.MessageReceiver;
import com.synopsys.integration.alert.processor.api.NotificationProcessorV2;

@Component(value = NotificationReceiverV2.COMPONENT_NAME)
public class NotificationReceiverV2 extends MessageReceiver<NotificationReceivedEventV2> implements AlertDefaultEventListener {
    public static final String COMPONENT_NAME = "notification_receiverV2";

    private static final int PAGE_SIZE = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final NotificationAccessor notificationAccessor;
    private final NotificationProcessorV2 notificationProcessor;

    @Autowired
    public NotificationReceiverV2(Gson gson, NotificationAccessor notificationAccessor, NotificationProcessorV2 notificationProcessor) {
        super(gson, NotificationReceivedEventV2.class);
        this.notificationAccessor = notificationAccessor;
        this.notificationProcessor = notificationProcessor;
    }

    @Override
    public void handleEvent(NotificationReceivedEventV2 event) {
        logger.debug("Event {}", event);
        logger.info("Processing event for notifications.");

        int numPagesProcessed = 0;

        AlertPagedModel<AlertNotificationModel> pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(PAGE_SIZE);
        while (!CollectionUtils.isEmpty(pageOfAlertNotificationModels.getModels())) {
            List<AlertNotificationModel> notifications = pageOfAlertNotificationModels.getModels();
            logger.info("Sending {} notifications.", notifications.size());
            notificationProcessor.processNotifications(notifications, List.of(FrequencyType.REAL_TIME));
            numPagesProcessed++;
            pageOfAlertNotificationModels = notificationAccessor.getFirstPageOfNotificationsNotProcessed(PAGE_SIZE);
            logger.trace("Processing Page: {}. New pages found: {}",
                numPagesProcessed,
                pageOfAlertNotificationModels.getTotalPages());
        }
        logger.info("Finished processing event for notifications.");
    }

    @Override
    public String getDestinationName() {
        return NotificationReceivedEventV2.NOTIFICATION_RECEIVED_EVENT_TYPE;
    }
}