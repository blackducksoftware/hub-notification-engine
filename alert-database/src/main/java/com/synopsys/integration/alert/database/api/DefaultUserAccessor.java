/**
 * alert-database
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.database.api;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.common.persistence.model.UserRoleModel;
import com.synopsys.integration.alert.database.authorization.AccessOperationEntity;
import com.synopsys.integration.alert.database.authorization.AccessOperationRepository;
import com.synopsys.integration.alert.database.authorization.PermissionKeyEntity;
import com.synopsys.integration.alert.database.authorization.PermissionKeyRepository;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRelation;
import com.synopsys.integration.alert.database.authorization.PermissionMatrixRepository;
import com.synopsys.integration.alert.database.user.RoleEntity;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;
import com.synopsys.integration.alert.database.user.UserRoleRelation;
import com.synopsys.integration.alert.database.user.UserRoleRepository;

@Component
@Transactional
public class DefaultUserAccessor {
    public static final String DEFAULT_ADMIN_USER = "sysadmin";
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder defaultPasswordEncoder;
    private final PermissionMatrixRepository permissionMatrixRepository;
    private final AccessOperationRepository accessOperationRepository;
    private final PermissionKeyRepository permissionKeyRepository;

    @Autowired
    public DefaultUserAccessor(final UserRepository userRepository, final RoleRepository roleRepository, final UserRoleRepository userRoleRepository, final PasswordEncoder defaultPasswordEncoder,
        final PermissionMatrixRepository permissionMatrixRepository, final AccessOperationRepository accessOperationRepository, final PermissionKeyRepository permissionKeyRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.defaultPasswordEncoder = defaultPasswordEncoder;
        this.permissionMatrixRepository = permissionMatrixRepository;
        this.accessOperationRepository = accessOperationRepository;
        this.permissionKeyRepository = permissionKeyRepository;
    }

    public List<UserModel> getUsers() {
        final List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    public Optional<UserModel> getUser(final String username) {
        return userRepository.findByUserName(username).map(this::createModel);
    }

    private UserModel createModel(final UserEntity user) {
        final List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        final List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        final Set<UserRoleModel> roles = createRoles(roleIdsForUser);
        return UserModel.of(user.getUserName(), user.getPassword(), user.getEmailAddress(), roles);
    }

    public UserModel addOrUpdateUser(final UserModel user) {
        return addOrUpdateUser(user, false);
    }

    public UserModel addOrUpdateUser(final UserModel user, final boolean passwordEncoded) {
        final String password = (passwordEncoded ? user.getPassword() : defaultPasswordEncoder.encode(user.getPassword()));
        final UserEntity userEntity = new UserEntity(user.getName(), password, user.getEmailAddress());

        final Optional<UserEntity> existingUser = userRepository.findByUserName(user.getName());
        if (existingUser.isPresent()) {
            final Long userId = existingUser.get().getId();
            userEntity.setId(userId);
            userRoleRepository.deleteAllByUserId(userId);
        }

        if (null != user.getRoles()) {
            final Collection<String> roles = user.getRoles().stream().map(UserRoleModel::getName).collect(Collectors.toSet());

            final List<RoleEntity> roleEntities = roleRepository.findRoleEntitiesByRoleName(roles);
            final List<UserRoleRelation> roleRelations = new LinkedList<>();
            for (final RoleEntity role : roleEntities) {
                roleRelations.add(new UserRoleRelation(userEntity.getId(), role.getId()));
            }
            userRoleRepository.saveAll(roleRelations);
        }

        return createModel(userRepository.save(userEntity));
    }

    public UserModel addUser(final String userName, final String password, final String emailAddress) {
        return addOrUpdateUser(UserModel.of(userName, password, emailAddress, Collections.emptySet()));
    }

    public boolean assignRoles(final String username, final Set<Long> roles) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        boolean assigned = false;
        if (entity.isPresent()) {
            final UserModel model = addOrUpdateUser(UserModel.of(entity.get().getUserName(), entity.get().getPassword(), entity.get().getEmailAddress(), createRoles(roles)));
            assigned = model.getName().equals(username) && model.getRoles().size() == roles.size();
        }
        return assigned;
    }

    public boolean changeUserPassword(final String username, final String newPassword) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            final UserEntity oldEntity = entity.get();
            final UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), defaultPasswordEncoder.encode(newPassword), oldEntity.getEmailAddress());
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    public boolean changeUserEmailAddress(final String username, final String emailAddress) {
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            final UserEntity oldEntity = entity.get();
            final UserEntity updatedEntity = new UserEntity(oldEntity.getUserName(), oldEntity.getPassword(), emailAddress);
            updatedEntity.setId(oldEntity.getId());
            return userRepository.save(updatedEntity) != null;
        }
        return false;
    }

    public void deleteUser(final String userName) {
        final Optional<UserEntity> userEntity = userRepository.findByUserName(userName);
        userEntity.ifPresent(entity -> {
            assignRoles(entity.getUserName(), Collections.emptySet());
            userRepository.delete(entity);
        });
    }

    private Set<UserRoleModel> createRoles(final Collection<Long> roleIds) {
        final Set<UserRoleModel> userRoles = new LinkedHashSet<>();
        for (final Long roleId : roleIds) {
            final Optional<String> roleName = getRoleName(roleId);
            roleName.ifPresent(role -> userRoles.add(UserRoleModel.of(role, createPermissionMatrix(roleId))));
        }
        return userRoles;
    }

    private Optional<String> getRoleName(final Long roleId) {
        return roleRepository.findById(roleId).map(RoleEntity::getRoleName);
    }

    private PermissionMatrixModel createPermissionMatrix(final Long roleId) {
        final List<PermissionMatrixRelation> permissions = permissionMatrixRepository.findAllByRoleId(roleId);
        final Map<String, EnumSet<AccessOperation>> permissionOperations = new HashMap<>();

        for (final PermissionMatrixRelation relation : permissions) {
            final Optional<PermissionKeyEntity> permissionKey = permissionKeyRepository.findById(relation.getPermissionKeyId());
            final Optional<AccessOperationEntity> accessOperation = accessOperationRepository.findById(relation.getAccessOperationId());
            permissionKey.ifPresent(key -> {
                final String keyName = key.getKeyName();
                permissionOperations.computeIfAbsent(keyName, ignored -> EnumSet.noneOf(AccessOperation.class));
                accessOperation.ifPresent(operation -> permissionOperations.get(keyName).add(AccessOperation.valueOf(operation.getOperationName())));
            });
        }

        final PermissionMatrixModel model = new PermissionMatrixModel(permissionOperations);
        return model;
    }
}
