package com.jtool.apiclient.processor;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.Request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.Util.*;

public class HeadProcessor extends Processor {

    private Map<String, List<String>> responseHeader;

    public HeadProcessor(Request request) throws IOException {
        this.request = request;
    }

    @Override
    void processingParam() {
        appendParamStrToUrl(params2paramsStr(request.getParam()));
        if(log.isDebugEnabled()) {
            log.debug("发送请求: curl -X HEAD " + makeHeaderLogString(request.getHeader()) + " '" + request.getUrl() + "'");
        }
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException {
        responseHeader = httpURLConnection.getHeaderFields();
        if(log.isDebugEnabled()) {
            log.debug("获得header: {}", JSON.toJSON(responseHeader));
        }
        return httpURLConnection;
    }

    private void appendParamStrToUrl(String paramsString) {
        if (!"".equals(paramsString)) {
            if (request.getUrl().contains("?")) {
                request.setUrl(request.getUrl() + "&" + paramsString);
            } else {
                request.setUrl(request.getUrl() + "?" + paramsString);
            }
        }
    }


    public Map<String, List<String>> getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(Map<String, List<String>> responseHeader) {
        this.responseHeader = responseHeader;
    }
}
