package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;

import java.io.IOException;
import java.net.HttpURLConnection;

import static com.jtool.apiclient.Util.*;

public class GetProcessor extends Processor {

    public GetProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {
        appendParamStrToUrl(params2paramsStr(request.getParam()));
        if(log.isDebugEnabled()) {
            log.debug("发送请求: curl " + makeHeaderLogString(request.getHeader()) + " '" + request.getUrl() + "'");
        }
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException {
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
}
