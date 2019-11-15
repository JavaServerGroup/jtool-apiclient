package com.jtool.apiclient.processor;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.Request;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.util.HttpUtil.makeHeaderLogString;
import static com.jtool.apiclient.util.HttpUtil.params2paramsStr;

@Slf4j
public class HeadProcessor extends AbstractProcessor {

    private Map<String, List<String>> responseHeader;

    public HeadProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {
        appendParamStrToUrl(params2paramsStr(request.getParam()));
        if (log.isDebugEnabled()) {
            log.debug("发送请求: curl -X HEAD {} '{}'", makeHeaderLogString(request.getHeader()), request.getUrl());
        }
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) {
        responseHeader = httpUrlConnection.getHeaderFields();
        if (log.isDebugEnabled()) {
            log.debug("获得header: {}", JSON.toJSON(responseHeader));
        }
        return httpUrlConnection;
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

}
