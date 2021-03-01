/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.msteams;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MSTeamsJobDetailsRepository extends JpaRepository<MSTeamsJobDetailsEntity, UUID> {
}
