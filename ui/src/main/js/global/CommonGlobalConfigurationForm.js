import React, { useEffect, useState } from 'react';
import * as PropTypes from 'prop-types';
import ConfigButtons from 'component/common/ConfigButtons';
import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import GlobalTestModal from 'global/GlobalTestModal';
import StatusMessage from 'field/StatusMessage';

const CommonGlobalConfigurationForm = ({
    formData,
    setFormData,
    testFormData,
    setTestFormData,
    csrfToken,
    setErrors,
    displaySave,
    displayTest,
    displayDelete,
    children,
    testFields,
    buttonIdPrefix,
    afterSuccessfulSave,
    retrieveData,
    readonly
}) => {
    const [showTest, setShowTest] = useState(false);
    const [errorMessage, setErrorMessage] = useState(null);
    const [actionMessage, setActionMessage] = useState(null);
    const [errorIsDetailed, setErrorIsDetailed] = useState(false);
    const [inProgress, setInProgress] = useState(false);

    const testRequest = (fieldModel) => ConfigRequestBuilder.createTestRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, fieldModel);
    const deleteRequest = () => ConfigRequestBuilder.createDeleteRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, FieldModelUtilities.getFieldModelId(formData));
    const validateRequest = () => ConfigRequestBuilder.createValidateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

    const fetchData = async () => {
        const content = await retrieveData();
        if (content) {
            setFormData(content);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleTestCancel = () => {
        setShowTest(false);
        setTestFormData({});
    };

    const performTestRequest = async () => {
        setInProgress(true);
        let copy = JSON.parse(JSON.stringify(formData));
        Object.keys(testFormData).forEach((key) => {
            copy = FieldModelUtilities.updateFieldModelSingleValue(copy, key, testFormData[key]);
        });

        const response = await testRequest(copy);
        if (response.ok) {
            const json = await response.json();
            if (json.hasErrors) {
                setErrorIsDetailed(json.detailed);
                setErrorMessage(json.message);
                setErrors(json.errors);
            } else {
                setActionMessage('Test Successful');
            }
        }

        handleTestCancel();
        setInProgress(false);
    };

    const handleTestClick = () => {
        setErrorMessage(null);
        setErrors({});

        if (testFields) {
            setShowTest(true);
        } else {
            performTestRequest();
        }
    };

    const performSaveRequest = async (event) => {
        event.preventDefault();
        event.stopPropagation();

        setInProgress(true);
        setErrorMessage(null);
        setErrors({});
        const validateResponse = await validateRequest();
        if (validateResponse.ok) {
            const validateJson = await validateResponse.json();
            if (validateJson.hasErrors) {
                setErrorMessage(validateJson.message);
                setErrors(validateJson.errors);
            } else {
                const id = FieldModelUtilities.getFieldModelId(formData);
                const request = (id)
                    ? () => ConfigRequestBuilder.createUpdateRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id, formData)
                    : () => ConfigRequestBuilder.createNewConfigurationRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, formData);

                await request();
                await fetchData();

                setActionMessage('Save Successful');
                afterSuccessfulSave();
            }
        }
        setInProgress(false);
    };

    const performDeleteRequest = async () => {
        setInProgress(true);
        await deleteRequest();
        setFormData({});
        setActionMessage('Delete Successful');
        setInProgress(false);
    };

    return (
        <div>
            <StatusMessage
                id="global-config-status-message"
                errorMessage={errorMessage}
                actionMessage={actionMessage}
                errorIsDetailed={errorIsDetailed}
            />
            <form className="form-horizontal" onSubmit={performSaveRequest} noValidate>
                <div>
                    {children}
                </div>
                <ConfigButtons
                    submitId={`${buttonIdPrefix}-submit`}
                    cancelId={`${buttonIdPrefix}-cancel`}
                    deleteId={`${buttonIdPrefix}-delete`}
                    testId={`${buttonIdPrefix}-test`}
                    includeSave={!readonly && displaySave}
                    includeTest={!readonly && displayTest}
                    includeDelete={!readonly && displayDelete}
                    type="submit"
                    onTestClick={handleTestClick}
                    onDeleteClick={performDeleteRequest}
                    confirmDeleteMessage="Are you sure you want to delete the configuration?"
                    performingAction={inProgress}
                />
            </form>
            <GlobalTestModal
                showTestModal={showTest}
                handleTest={performTestRequest}
                handleCancel={handleTestCancel}
                buttonIdPrefix={buttonIdPrefix}
            >
                <div>
                    {testFields}
                </div>
            </GlobalTestModal>
        </div>
    );
};

CommonGlobalConfigurationForm.propTypes = {
    children: PropTypes.node.isRequired,
    formData: PropTypes.object.isRequired,
    csrfToken: PropTypes.string.isRequired,
    setFormData: PropTypes.func.isRequired,
    setErrors: PropTypes.func.isRequired,
    retrieveData: PropTypes.func.isRequired,
    displaySave: PropTypes.bool,
    displayTest: PropTypes.bool,
    displayDelete: PropTypes.bool,
    testFields: PropTypes.node,
    testFormData: PropTypes.object,
    setTestFormData: PropTypes.func,
    buttonIdPrefix: PropTypes.string,
    afterSuccessfulSave: PropTypes.func,
    readonly: PropTypes.bool
};

CommonGlobalConfigurationForm.defaultProps = {
    displaySave: true,
    displayTest: true,
    displayDelete: true,
    testFields: null,
    testFormData: {},
    setTestFormData: () => null,
    buttonIdPrefix: 'common-form',
    afterSuccessfulSave: () => null,
    readonly: false
};

export default CommonGlobalConfigurationForm;
