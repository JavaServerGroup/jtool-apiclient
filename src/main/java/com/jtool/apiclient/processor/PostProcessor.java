package com.jtool.apiclient.processor;

import com.jtool.apiclient.ApiClient;
import com.jtool.apiclient.model.Request;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;

import static com.jtool.apiclient.util.HttpUtil.*;

@Slf4j
public class PostProcessor extends AbstractProcessor {

    public PostProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {
        request.setParamsString(params2paramsStr(request.getParam()));
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("发送请求: curl '{}' {} -X POST -d '{}'", request.getUrl(), makeHeaderLogString(request.getHeader()), request.getParamsString());
        }
        httpUrlConnection.setDoOutput(true);
        if (!"".equals(request.getParamsString())) {
            httpUrlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            byte[] data = request.getParamsString().getBytes(ApiClient.getCharsetName());
            httpUrlConnection.setFixedLengthStreamingMode(data.length);

            writeAndCloseStream(httpUrlConnection.getOutputStream(), data);
        } else {
            httpUrlConnection.setFixedLengthStreamingMode(0);
        }
        return httpUrlConnection;
    }

}
