package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;

import static com.jtool.apiclient.util.HttpUtil.makeHeaderLogString;
import static com.jtool.apiclient.util.HttpUtil.params2paramsStr;

@Slf4j
public class GetProcessor extends AbstractProcessor {

    public GetProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {
        appendParamStrToUrl(params2paramsStr(request.getParam()));
        if (log.isDebugEnabled()) {
            log.debug("发送请求: curl {} '{}'", makeHeaderLogString(request.getHeader()),  request.getUrl());
        }
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) {
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
}
