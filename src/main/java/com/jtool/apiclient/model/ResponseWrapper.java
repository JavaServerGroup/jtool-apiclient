package com.jtool.apiclient.model;

import com.alibaba.fastjson.JSON;

import java.util.List;
import java.util.Map;

public class ResponseWrapper {

    private int responseCode;

    private String responseBody;

    private Map<String, List<String>> responseHeader;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public <T> T getResponseBody(Class<T> clazz) {
        return JSON.parseObject(responseBody, clazz);
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Map<String, List<String>> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Map<String, List<String>> responseHeader) {
        this.responseHeader = responseHeader;
    }

}
