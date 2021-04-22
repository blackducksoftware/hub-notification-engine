package com.synopsys.integration.alert.channel.util;

public class RestResponseStatus {
    private final RestResponseEnum status;
    private final String message;

    public static RestResponseStatus success() {
        return new RestResponseStatus(RestResponseEnum.SUCCESS, "");
    }

    public static RestResponseStatus failure(String message) {
        return new RestResponseStatus(RestResponseEnum.FAILURE, message);
    }

    private RestResponseStatus(RestResponseEnum status, String message) {
        this.status = status;
        this.message = message;
    }

    public RestResponseEnum getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
