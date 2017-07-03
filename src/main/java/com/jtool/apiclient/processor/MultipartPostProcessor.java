package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;
import com.jtool.apiclient.Util;
import com.jtool.apiclient.model.MultipartFileItem;
import com.jtool.apiclient.model.MultipartItem;
import com.jtool.apiclient.model.MultipartTextItem;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MultipartPostProcessor extends Processor {

    private static final String PREFIX = "--";
    private static final String MULTIPART_FROM_DATA = "multipart/form-data";
    private List<MultipartItem> multipartItemList = new ArrayList<>();

    public static final String LINE_END = "\r\n";
    public static final String CHARSET = "UTF-8";

    public MultipartPostProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {

        if(request.getParam() != null) {

            Map<String, Object> param;
            if(request.getParam() instanceof Map) {
                param = (Map)request.getParam();
            } else {
                param = Util.obj2Map(request.getParam());
            }

            for (Map.Entry<String, Object> entry : param.entrySet()) {
                if (entry.getValue() != null) {
                    if(entry.getValue() instanceof File) {
                        multipartItemList.add(genMultipartFileItem(entry.getKey(), (File)entry.getValue()));
                    } else if(entry.getValue() instanceof List && !((List)entry.getValue()).isEmpty() && allListItemIsFile((List) entry.getValue())) {
                        for (Object file : ((List) entry.getValue())) {
                            multipartItemList.add(genMultipartFileItem(entry.getKey(), (File)file));
                        }
                    } else {
                        multipartItemList.add(genMultipartTextItem(entry.getKey(), entry.getValue().toString()));
                    }
                }
            }
        }
    }

    private MultipartItem genMultipartTextItem(String key, String value) {
        MultipartTextItem multipartTextItem = new MultipartTextItem();
        multipartTextItem.setKey(key);
        multipartTextItem.setValue(value);

        return multipartTextItem;
    }

    private MultipartItem genMultipartFileItem(String key, File file) {
        MultipartFileItem multipartFileItem = new MultipartFileItem();
        multipartFileItem.setKey(key);
        multipartFileItem.setFile(file);

        return multipartFileItem;
    }

    @Override
    HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException {
        String BOUNDARY = UUID.randomUUID().toString();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Charset", CHARSET);
        httpURLConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

        try (OutputStream outputStream = new BufferedOutputStream(httpURLConnection.getOutputStream())) {

            for(MultipartItem multipartItem : multipartItemList) {
                outputStream.write(PREFIX.getBytes(CHARSET));
                outputStream.write(BOUNDARY.getBytes(CHARSET));
                outputStream.write(LINE_END.getBytes(CHARSET));
                outputStream.write(multipartItem.genContentDispositionStr().getBytes(CHARSET));
                outputStream.write(multipartItem.genContentType().getBytes(CHARSET));
                outputStream.write(LINE_END.getBytes(CHARSET));
                multipartItem.genBody(outputStream);
                outputStream.write(LINE_END.getBytes(CHARSET));
            }
            outputStream.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes(CHARSET));
            outputStream.flush();
        }

        return httpURLConnection;
    }

    private boolean allListItemIsFile(List list) {

        for (Object obj : list) {
            if (!(obj instanceof File)) {
                return false;
            }
        }

        return true;
    }
}


