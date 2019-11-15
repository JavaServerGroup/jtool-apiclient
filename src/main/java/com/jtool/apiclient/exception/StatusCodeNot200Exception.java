package com.jtool.apiclient.exception;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@Getter
public class StatusCodeNot200Exception extends RuntimeException {

    private final String url;
    private final int statusCode;
    private final Object params;

    public StatusCodeNot200Exception(String url, Object params, int statusCode) {
        this.url = url;
        this.params = params;
        this.statusCode = statusCode;
        log.error(this.getStatusCode() + "\t" + this.getUrl() + "\t" + this.getParams(), this);
    }

}
