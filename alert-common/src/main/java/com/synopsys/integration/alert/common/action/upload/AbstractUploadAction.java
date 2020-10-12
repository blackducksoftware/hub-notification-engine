/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.action.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.UploadValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.rest.api.AbstractFunctionController;
import com.synopsys.integration.alert.common.rest.model.ExistenceModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;

public abstract class AbstractUploadAction {
    public static final String API_FUNCTION_UPLOAD_URL = AbstractFunctionController.API_FUNCTION_URL + "/upload";
    private static final String META_DATA_MISSING = "Target meta data missing.";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private UploadTarget target;
    private final AuthorizationManager authorizationManager;
    private final FilePersistenceUtil filePersistenceUtil;

    public AbstractUploadAction(AuthorizationManager authorizationManager, FilePersistenceUtil filePersistenceUtil) {
        this.authorizationManager = authorizationManager;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public UploadTarget getTarget() {
        return target;
    }

    public void setTarget(UploadTarget target) {
        this.target = target;
    }

    public ActionResponse<ExistenceModel> uploadFileExists() {
        if (!isTargetDefined()) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, META_DATA_MISSING);
        }
        if (!authorizationManager.hasUploadReadPermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        String targetFilename = target.getFilename();
        Boolean exists = filePersistenceUtil.uploadFileExists(targetFilename);
        ExistenceModel content = new ExistenceModel(exists);

        return new ActionResponse<>(HttpStatus.OK, content);
    }

    public ActionResponse<Void> uploadFile(Resource fileToUpload) {
        if (!isTargetDefined()) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, META_DATA_MISSING);
        }
        if (!authorizationManager.hasUploadWritePermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        return writeFile(fileToUpload);
    }

    public ActionResponse<Void> deleteFile() {
        if (!isTargetDefined()) {
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, META_DATA_MISSING);
        }
        if (!authorizationManager.hasUploadDeletePermission(target.getContext().name(), target.getDescriptorKey().getUniversalKey())) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }
        try {
            String targetFilename = target.getFilename();
            File fileToValidate = filePersistenceUtil.createUploadsFile(targetFilename);
            filePersistenceUtil.delete(fileToValidate);
        } catch (IOException ex) {
            logger.error("Error deleting file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Error deleting file caused by: ", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting uploaded file from server.");
        }
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private boolean isTargetDefined() {
        if (null == target) {
            return false;
        }
        return true;
    }

    private ActionResponse<Void> writeFile(Resource fileResource) {
        try {
            String targetFilename = target.getFilename();
            String tempFilename = "temp_" + targetFilename;

            Optional<UploadValidationFunction> validationFunction = target.getValidationFunction();
            if (validationFunction.isPresent()) {
                writeFile(tempFilename, fileResource);
                File tempFileToValidate = filePersistenceUtil.createUploadsFile(tempFilename);
                ValidationResult validationResult = validationFunction.get().apply(tempFileToValidate);
                filePersistenceUtil.delete(tempFileToValidate);
                if (validationResult.hasErrors()) {
                    return new ActionResponse<>(HttpStatus.BAD_REQUEST, validationResult.combineErrorMessages());
                }
            }
            writeFile(targetFilename, fileResource);
        } catch (IOException ex) {
            logger.error("Error uploading file - file: {}, context: {}, descriptor: {} ", target.getFilename(), target.getContext(), target.getDescriptorKey().getUniversalKey());
            logger.error("Error uploading file caused by: ", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading file to server.");
        }

        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private void writeFile(String fileName, Resource fileResource) throws IOException {
        try (InputStream inputStream = fileResource.getInputStream()) {
            filePersistenceUtil.writeFileToUploadsDirectory(fileName, inputStream);
        } catch (IOException ex) {
            logger.error("Error writing file to resource - file: {}", fileName);
            throw ex;
        }
    }
}