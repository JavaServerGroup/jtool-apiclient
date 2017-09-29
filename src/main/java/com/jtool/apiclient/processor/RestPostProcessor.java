package com.jtool.apiclient.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jtool.apiclient.Request;
import com.jtool.apiclient.Util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.Util.makeHeaderLogString;
import static com.jtool.apiclient.Util.writeAndCloseStream;

public class RestPostProcessor extends Processor {

    private SerializerFeature[] features = new SerializerFeature[] {
            SerializerFeature.WriteClassName,
    };

    public RestPostProcessor(Request request) {
        this.request = request;
        request.setRest(true);
    }

    @Override
    void processingParam() {
        checkIsNotPostFile(Util.obj2Map(request.getParam()));
        if(request.getParam() != null) {
            if(request.isWithClassName()) {
                request.setParamsString(JSON.toJSONString(request.getParam(), features));
            } else {
                request.setParamsString(JSON.toJSONString(request.getParam()));
            }
        }
        if(log.isDebugEnabled()) {
            if(request.getParamsString() == null) {
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
    HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException {
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        if (request.getParamsString() != null) {
            byte[] data = request.getParamsString().getBytes("UTF-8");
            httpURLConnection.setFixedLengthStreamingMode(data.length);

            writeAndCloseStream(httpURLConnection.getOutputStream(), data);
        } else {
            httpURLConnection.setFixedLengthStreamingMode(0);
        }
        return httpURLConnection;
    }


    private void checkIsNotPostFile(Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof File) {
                    throw new RuntimeException("rest post方式不支持发送文件");
                }
                if (entry.getValue() instanceof List) {
                    Iterator iterator = ((List)entry.getValue()).iterator();
                    while (iterator.hasNext()) {
                        Object obj = iterator.next();
                        if (obj instanceof File) {
                            new RuntimeException("rest post方式不支持发送文件");
                        }
                    }
                }
            }
        }
    }


}
