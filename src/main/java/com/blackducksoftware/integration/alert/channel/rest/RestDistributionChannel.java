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
package com.blackducksoftware.integration.alert.channel.rest;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.datasource.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.google.gson.Gson;

public abstract class RestDistributionChannel<G extends GlobalChannelConfigEntity, C extends DistributionChannelConfigEntity> extends DistributionChannel<G, C> {
    private final ChannelRestConnectionFactory channelRestConnectionFactory;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RestDistributionChannel(final Gson gson, final GlobalProperties globalProperties, final AuditEntryRepository auditEntryRepository, final JpaRepository<G, Long> globalRepository,
            final JpaRepository<C, Long> distributionRepository,
            final CommonDistributionRepository commonDistributionRepository, final ChannelRestConnectionFactory channelRestConnectionFactory, final ContentConverter contentExtractor) {
        super(gson, globalProperties, auditEntryRepository, globalRepository, distributionRepository, commonDistributionRepository, contentExtractor);
        this.channelRestConnectionFactory = channelRestConnectionFactory;
    }

    @Override
    public void sendMessage(final ChannelEvent event, final C config) throws IntegrationException {
        final G globalConfig = getGlobalConfigEntity();
        try (final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection(getApiUrl(globalConfig))) {
            final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
            final Optional<DigestModel> optionalModel = extractContentFromEvent(event, DigestModel.class);
            if (optionalModel.isPresent()) {
                final Request request = createRequest(channelRequestHelper, config, globalConfig, optionalModel.get());
                channelRequestHelper.sendMessageRequest(request, event.getDestination());
            } else {
                logger.info("No data found to send.");
            }
        } catch (final IOException ex) {
            throw new AlertException(ex);
        }
    }

    public ChannelRestConnectionFactory getChannelRestConnectionFactory() {
        return channelRestConnectionFactory;
    }

    public abstract String getApiUrl(G globalConfig);

    public abstract Request createRequest(final ChannelRequestHelper channelRequestHelper, final C config, G globalConfig, final DigestModel digestModel) throws IntegrationException;

}
