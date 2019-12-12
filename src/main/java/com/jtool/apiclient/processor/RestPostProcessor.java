package com.jtool.apiclient.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jtool.apiclient.Request;
import com.jtool.apiclient.exception.RestPostNotSupportFileException;
import com.jtool.apiclient.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.util.HttpUtil.makeHeaderLogString;
import static com.jtool.apiclient.util.HttpUtil.writeAndCloseStream;

@Slf4j
public class RestPostProcessor extends AbstractProcessor {

    private SerializerFeature[] features = new SerializerFeature[]{
            SerializerFeature.WriteClassName,
    };

    public RestPostProcessor(Request request) {
        this.request = request;
        request.setRest(true);
    }

    @Override
    void processingParam() {
        checkIsNotPostFile(HttpUtil.obj2Map(request.getParam()));
        if (request.getParam() != null) {
            if (request.isWithClassName()) {
                request.setParamsString(JSON.toJSONString(request.getParam(), features));
            } else {
                request.setParamsString(JSON.toJSONString(request.getParam()));
            }
        }
        if (log.isDebugEnabled()) {
            if (request.getParamsString() == null) {
                log.debug("发送请求: curl '{}' {} -X POST",
                        request.getUrl(),
                        makeHeaderLogString(request.getHeader(), request.isRest()));
            } else {
                log.debug("发送请求: curl '{}' {} -X POST -d '{}'",
                        request.getUrl(),
                        makeHeaderLogString(request.getHeader(), request.isRest()),
                        request.getParamsString());
            }
        }
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) throws IOException {
        httpUrlConnection.setDoOutput(true);
        if(httpUrlConnection.getRequestProperty("Content-Type") == null) {
            httpUrlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        }
        if (request.getParamsString() != null) {
            byte[] data = request.getParamsString().getBytes(StandardCharsets.UTF_8);
            httpUrlConnection.setFixedLengthStreamingMode(data.length);

            writeAndCloseStream(httpUrlConnection.getOutputStream(), data);
        } else {
            httpUrlConnection.setFixedLengthStreamingMode(0);
        }
        return httpUrlConnection;
    }

    private void checkIsNotPostFile(Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                checkIsNotPostFileByEntry(entry);
            }
        }
    }

    private void checkIsNotPostFileByEntry(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof File) {
            throw new RestPostNotSupportFileException();
        }
        if (entry.getValue() instanceof List) {

            for (Object obj : (List) entry.getValue()) {
                if (obj instanceof File) {
                    throw new RestPostNotSupportFileException();
                }
            }
        }
    }

}
