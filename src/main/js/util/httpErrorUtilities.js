export const MESSAGES = {
    FORBIDDEN_ACTION: 'You are not permitted to perform this action.',
    FORBIDDEN_READ: 'You are not permitted to view this information.'
};

export function createErrorObject(errorResponse) {
    const responseMessage = Object.prototype.hasOwnProperty.call('message') && errorResponse.message;
    const responseErrors = Object.prototype.hasOwnProperty.call('errors') && errorResponse.errors;
    const message = responseMessage || '';
    const fieldErrors = responseErrors || {};
    return {
        fieldErrors,
        message
    };
}

export function createEmptyErrorObject() {
    return {
        message: '',
        fieldErrors: {}
    };
}

export function createStatusCodeHandler(statusCode, callback) {
    return {
        statusCode,
        callback
    };
}

export function createDefaultHandler(callback) {
    return createStatusCodeHandler(null, callback);
}

export function createBadRequestHandler(callback) {
    return createStatusCodeHandler(400, callback);
}

export function createUnauthorizedHandler(callback) {
    return createStatusCodeHandler(401, callback);
}

export function createForbiddenHandler(callback) {
    return createStatusCodeHandler(403, callback);
}

export function createNotFoundHandler(callback) {
    return createStatusCodeHandler(404, callback);
}

export function createPreconditionFailedHandler(callback) {
    return createStatusCodeHandler(412, callback);
}

export function createHttpErrorHandler(statusHandlers) {
    return (statusCode) => {
        const handler = statusHandlers && statusHandlers.find((statusHandler) => statusHandler.statusCode === statusCode);
        if (handler) {
            return handler.callback();
        }
        const defaultHandler = statusHandlers && statusHandlers.find((statusHandler) => !statusHandler.statusCode);

        if (defaultHandler) {
            return defaultHandler.callback();
        }

        return () => {
        };
    };
};