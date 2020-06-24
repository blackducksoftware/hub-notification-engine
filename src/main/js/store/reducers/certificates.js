import {
    CERTIFICATES_CLEAR_FIELD_ERRORS,
    CERTIFICATES_DELETE_ERROR,
    CERTIFICATES_DELETED,
    CERTIFICATES_DELETING,
    CERTIFICATES_FETCH_ERROR_ALL,
    CERTIFICATES_FETCHED_ALL,
    CERTIFICATES_FETCHING_ALL,
    CERTIFICATES_SAVE_ERROR,
    CERTIFICATES_SAVED,
    CERTIFICATES_SAVING,
    SERIALIZE
} from 'store/actions/types';
import * as HTTPErrorUtils from 'util/httpErrorUtilities';

const initialState = {
    inProgress: false,
    fetching: false,
    deleteSuccess: false,
    data: [],
    error: HTTPErrorUtils.createEmptyErrorObject(),
    saveStatus: ''
};

const certificates = (state = initialState, action) => {
    switch (action.type) {
        case CERTIFICATES_DELETE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: ''
            });
        case CERTIFICATES_DELETED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: true,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            });
        case CERTIFICATES_DELETING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: ''
            });
        case CERTIFICATES_FETCH_ERROR_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                certificatesFetchError: action.certificatesFetchError,
                fetching: false,
                saveStatus: ''
            });
        case CERTIFICATES_FETCHED_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: action.certificates,
                fetching: false,
                saveStatus: ''
            });
        case CERTIFICATES_FETCHING_ALL:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                data: [],
                fetching: true,
                saveStatus: ''

            });
        case CERTIFICATES_SAVE_ERROR:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createErrorObject(action),
                saveStatus: 'ERROR'
            });
        case CERTIFICATES_SAVED:
            return Object.assign({}, state, {
                inProgress: false,
                deleteSuccess: false,
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: 'SAVED'
            });
        case CERTIFICATES_SAVING:
            return Object.assign({}, state, {
                inProgress: true,
                deleteSuccess: false,
                saveStatus: 'SAVING'
            });
        case CERTIFICATES_CLEAR_FIELD_ERRORS: {
            return Object.assign({}, state, {
                error: HTTPErrorUtils.createEmptyErrorObject(),
                saveStatus: ''
            });
        }
        case SERIALIZE:
            return initialState;

        default:
            return state;
    }
};

export default certificates;
