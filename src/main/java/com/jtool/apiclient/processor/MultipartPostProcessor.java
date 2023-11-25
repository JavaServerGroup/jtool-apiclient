package com.jtool.apiclient.processor;

import com.jtool.apiclient.model.Request;
import com.jtool.apiclient.model.MultipartFileItem;
import com.jtool.apiclient.model.MultipartItem;
import com.jtool.apiclient.model.MultipartTextItem;
import com.jtool.apiclient.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class MultipartPostProcessor extends AbstractProcessor {

    private static final String PREFIX = "--";
    private static final String MULTIPART_FROM_DATA = "multipart/form-data";
    private List<MultipartItem> multipartItemList = new ArrayList<>();
    public static final String LINE_END = "\r\n";

    public MultipartPostProcessor(Request request) {
        this.request = request;
    }

    @Override
    void processingParam() {

        if (request.getParam() != null) {

            Map<String, Object> param = fixParamToMap();

            for (Map.Entry<String, Object> entry : param.entrySet()) {
                if (entry.getValue() != null) {
                    paramToMultipartItem(entry);
                }
            }
        }
    }

    private void paramToMultipartItem(Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof File) {
            multipartItemList.add(genMultipartFileItem(entry.getKey(), (File) entry.getValue()));
        } else if (entry.getValue() instanceof List && !((List) entry.getValue()).isEmpty() && allListItemIsFile((List) entry.getValue())) {
            for (Object file : ((List) entry.getValue())) {
                multipartItemList.add(genMultipartFileItem(entry.getKey(), (File) file));
            }
        } else {
            multipartItemList.add(genMultipartTextItem(entry.getKey(), entry.getValue().toString()));
        }
    }

    private Map<String, Object> fixParamToMap() {
        Map<String, Object> param;
        if (request.getParam() instanceof Map) {
            param = (Map) request.getParam();
        } else {
            param = HttpUtil.obj2Map(request.getParam());
        }
        return param;
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
    HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) throws IOException {
        String boundaryStr = UUID.randomUUID().toString();
        httpUrlConnection.setDoOutput(true);
        httpUrlConnection.setUseCaches(false);
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setRequestProperty("Charset", UTF_8.toString());
        httpUrlConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + boundaryStr);

        try (OutputStream outputStream = new BufferedOutputStream(httpUrlConnection.getOutputStream())) {

            for (MultipartItem multipartItem : multipartItemList) {
                outputStream.write(PREFIX.getBytes(UTF_8));
                outputStream.write(boundaryStr.getBytes(UTF_8));
                outputStream.write(LINE_END.getBytes(UTF_8));
                outputStream.write(multipartItem.genContentDispositionStr().getBytes(UTF_8));
                outputStream.write(multipartItem.genContentType().getBytes(UTF_8));
                outputStream.write(LINE_END.getBytes(UTF_8));
                multipartItem.genBody(outputStream);
                outputStream.write(LINE_END.getBytes(UTF_8));
            }
            outputStream.write((PREFIX + boundaryStr + PREFIX + LINE_END).getBytes(UTF_8));
            outputStream.flush();
        }

        return httpUrlConnection;
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


