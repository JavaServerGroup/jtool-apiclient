package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;

import java.io.IOException;
import java.net.HttpURLConnection;

import static com.jtool.apiclient.Util.makeHeaderLogString;
import static com.jtool.apiclient.Util.params2paramsStr;
import static com.jtool.apiclient.Util.writeAndCloseStream;

public class PostProcessor extends Processor {

    public PostProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {
        request.setParamsString(params2paramsStr(request.getParam()));
        if(log.isDebugEnabled()) {
            log.debug("发送请求: curl '{}' {} -X POST -d '{}'", request.getUrl(), makeHeaderLogString(request.getHeader(), request.isRest()), request.getParamsString());
        }

    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException {
        httpURLConnection.setDoOutput(true);
        if (!"".equals(request.getParamsString())) {
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            byte[] data = request.getParamsString().getBytes("UTF-8");
            httpURLConnection.setFixedLengthStreamingMode(data.length);

            writeAndCloseStream(httpURLConnection.getOutputStream(), data);
        } else {
            httpURLConnection.setFixedLengthStreamingMode(0);
        }
        return httpURLConnection;
    }

}
