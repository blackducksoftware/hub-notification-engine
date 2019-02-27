package com.synopsys.integration.alert.database.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.PolarisIssueModel;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueEntity;
import com.synopsys.integration.alert.database.provider.polaris.issue.PolarisIssueRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;

@Component
@Transactional
// TODO test this class
public class PolarisIssueAccessor {
    private final PolarisIssueRepository polarisIssueRepository;
    private final ProviderProjectRepository providerProjectRepository;

    @Autowired
    public PolarisIssueAccessor(final PolarisIssueRepository polarisIssueRepository, final ProviderProjectRepository providerProjectRepository) {
        this.polarisIssueRepository = polarisIssueRepository;
        this.providerProjectRepository = providerProjectRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<PolarisIssueModel> getProjectIssues(final String projectHref) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(projectHref)) {
            throw new AlertDatabaseConstraintException("The field projectHref cannot be blank");
        }
        final Long projectId = providerProjectRepository.findFirstByHref(projectHref)
                                   .map(ProviderProjectEntity::getId)
                                   .orElseThrow(() -> new AlertDatabaseConstraintException("No project with that href existed: " + projectHref));
        return polarisIssueRepository.findByProjectId(projectId)
                   .stream()
                   .filter(entity -> projectId == entity.getProjectId())
                   .map(entity -> new PolarisIssueModel(entity.getIssueType(), entity.getPreviousCount(), entity.getCurrentCount()))
                   .collect(Collectors.toList());
    }

    public PolarisIssueModel updateIssueType(final String projectHref, final String issueType, final Integer newCount) throws AlertDatabaseConstraintException {
        if (StringUtils.isBlank(projectHref)) {
            throw new AlertDatabaseConstraintException("The field projectHref cannot be blank");
        }
        if (StringUtils.isBlank(issueType)) {
            throw new AlertDatabaseConstraintException("The field issueType cannot be blank");
        }
        if (null != newCount) {
            throw new AlertDatabaseConstraintException("The field newCount cannot be null");
        }

        final Long projectId = providerProjectRepository.findFirstByHref(projectHref)
                                   .map(ProviderProjectEntity::getId)
                                   .orElseThrow(() -> new AlertDatabaseConstraintException("No project with that href existed: " + projectHref));
        final Optional<PolarisIssueEntity> optionalIssueEntity = polarisIssueRepository.findFirstByIssueType(issueType);

        final PolarisIssueEntity newIssueEntity;
        if (optionalIssueEntity.isPresent()) {
            final PolarisIssueEntity oldIssueEntity = optionalIssueEntity.get();
            newIssueEntity = new PolarisIssueEntity(oldIssueEntity.getIssueType(), oldIssueEntity.getCurrentCount(), newCount, projectId);
            newIssueEntity.setId(oldIssueEntity.getId());
        } else {
            newIssueEntity = new PolarisIssueEntity(issueType, 0, newCount, projectId);
            polarisIssueRepository.save(newIssueEntity);
        }
        final PolarisIssueEntity savedIssueEntity = polarisIssueRepository.save(newIssueEntity);
        return new PolarisIssueModel(savedIssueEntity.getIssueType(), savedIssueEntity.getPreviousCount(), savedIssueEntity.getCurrentCount());
    }

}
