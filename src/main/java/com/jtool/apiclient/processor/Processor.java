package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;
import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.jtool.apiclient.Util.*;

public abstract class Processor {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected Request request;

    HttpURLConnection commonPreProcess() throws IOException {
        URL mURL = new URL(request.getUrl());

        HttpURLConnection httpURLConnection = (HttpURLConnection) mURL.openConnection();
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setConnectTimeout(request.getConnectionTimeout());
        httpURLConnection.setReadTimeout(request.getReadTimeout());

        addHeaderToHttpURLConnection(request.getHeader(), httpURLConnection);

        return httpURLConnection;
    }

    abstract void processingParam();

    abstract HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException;

    private String loadResponseString(HttpURLConnection httpURLConnection) throws IOException {

        try {
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                try(InputStream is = httpURLConnection.getInputStream()) {
                    return readAndCloseStream(is);
                }
            } else {
                logHttpURLConnectionErrorStream(httpURLConnection);
                throw new StatusCodeNot200Exception(request.getUrl(), request.getParam(), responseCode);
            }

        } catch (IOException e) {
            logHttpURLConnectionErrorStream(httpURLConnection);
            log.error("请求返回时发生IOException", e);
            throw e;
        } finally {
            httpURLConnection.disconnect();
        }
    }

    public String process() throws IOException{
        processingParam();
        final HttpURLConnection httpURLConnection = commonPreProcess();
        doProcess(httpURLConnection);
        return loadResponseString(httpURLConnection);
    }


}
