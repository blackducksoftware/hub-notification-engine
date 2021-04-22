package com.synopsys.integration.alert.channel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.synopsys.integration.rest.request.Request;

public class RestMessageResponse {
    //May want to use an enum or a new object instead of String as the value. It would hold the status (SUCCESS/FAIL) and the error in the case of failure
    private Map<Request, RestResponseStatus> responseStatuses = new HashMap<>();

    public void add(Request request, RestResponseStatus status) {
        responseStatuses.put(request, status);
    }

    public Map<Request, RestResponseStatus> getResponseStatus() {
        return responseStatuses;
    }

    public Map<Request, RestResponseStatus> getRequestsWithFailures() {
        return responseStatuses.entrySet()
                   .stream()
                   .filter(map -> RestResponseEnum.FAILURE.equals(map.getValue().getStatus()))
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public boolean hasErrors() {
        return responseStatuses.values()
                   .stream()
                   .map(RestResponseStatus::getStatus)
                   .anyMatch(RestResponseEnum.FAILURE::equals);
        /* //TODO: might be able to do this as a stream
        for (Map.Entry<Request, RestResponseStatus> entry : responseStatuses.entrySet()) {
            if (entry.getValue().getStatus().equals(RestResponseEnum.FAILURE)) {
                return true;
            }
        }
        return false;
         */
    }

}
