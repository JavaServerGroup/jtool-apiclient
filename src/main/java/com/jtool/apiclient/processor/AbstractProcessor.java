package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;
import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import com.jtool.apiclient.model.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static com.jtool.apiclient.util.HttpUtil.*;

@Slf4j
public abstract class AbstractProcessor {

    protected Request request;

    private HttpURLConnection commonPreProcess() throws IOException {
        final URL url = new URL(request.getUrl());

        HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
        httpUrlConnection.setRequestProperty("Charset", "UTF-8");
        httpUrlConnection.setConnectTimeout(request.getConnectionTimeout());
        httpUrlConnection.setReadTimeout(request.getReadTimeout());
        httpUrlConnection.setInstanceFollowRedirects(request.isFollowRedirects());

        addHeaderToHttpUrlConnection(request.getHeader(), httpUrlConnection);

        if (request.isGzipResponse()) {
            httpUrlConnection.setRequestProperty("Accept-Encoding", "gzip");
        }

        return httpUrlConnection;
    }

    abstract void processingParam();

    abstract HttpURLConnection doProcess(HttpURLConnection httpUrlConnection) throws IOException;

    private String loadResponseString(HttpURLConnection httpUrlConnection, boolean loadResponseString) throws IOException {

        try {
            int responseCode = httpUrlConnection.getResponseCode();

            if (is2XX(responseCode) || isRedirect(responseCode)) {
                return handleResponseString(httpUrlConnection, loadResponseString);
            } else {
                logHttpUrlConnectionErrorStream(httpUrlConnection);
                throw new StatusCodeNot200Exception(request.getUrl(), request.getParam(), responseCode);
            }

        } catch (IOException e) {
            logHttpUrlConnectionErrorStream(httpUrlConnection);
            log.error("请求返回时发生IOException", e);
            throw e;
        } finally {
            httpUrlConnection.disconnect();
        }
    }

    private boolean isRedirect(int responseCode) {
        return responseCode >= 300 && responseCode <= 399 && !request.isFollowRedirects();
    }

    private boolean is2XX(int responseCode) {
        return responseCode >= 200 && responseCode <= 299;
    }

    private InputStream getInputStreamByConnection(HttpURLConnection httpURLConnection) throws IOException {
        if ("gzip".equals(httpURLConnection.getContentEncoding())) {
            return new GZIPInputStream(httpURLConnection.getInputStream());
        } else {
            return httpURLConnection.getInputStream();
        }
    }

    public ResponseWrapper process() throws IOException {
        return process(true);
    }

    public ResponseWrapper process(boolean loadResponseString) throws IOException {
        processingParam();
        final HttpURLConnection httpUrlConnection = commonPreProcess();
        doProcess(httpUrlConnection);
        return loadResponseWrapper(httpUrlConnection, loadResponseString);
    }

    /**
     * 处理responseBody输入流
     *
     * @param httpUrlConnection
     * @param loadResponseString
     * @return
     * @throws IOException
     */
    private String handleResponseString(HttpURLConnection httpUrlConnection, boolean loadResponseString)
            throws IOException {
        try (InputStream is = getInputStreamByConnection(httpUrlConnection)) {
            if (loadResponseString) {

                final String result = readAndCloseStream(is);
                log.debug("返回: {}", result);
                return result;
            } else {
                return "";
            }
        }
    }

    /**
     * 处理response获取wrapper
     *
     * @param httpUrlConnection
     * @param loadResponseString
     * @return
     * @throws IOException
     */
    private ResponseWrapper loadResponseWrapper(HttpURLConnection httpUrlConnection, boolean loadResponseString)
            throws IOException {
        ResponseWrapper wrapper = new ResponseWrapper();
        try {
            int responseCode = httpUrlConnection.getResponseCode();
            wrapper.setResponseCode(responseCode);
            wrapper.setResponseHeader(httpUrlConnection.getHeaderFields());

            if (is2XX(responseCode) || isRedirect(responseCode)) {
                wrapper.setResponseBody(handleResponseString(httpUrlConnection, loadResponseString));
                return wrapper;
            } else {
                logHttpUrlConnectionErrorStream(httpUrlConnection);
                throw new StatusCodeNot200Exception(request.getUrl(), request.getParam(), responseCode);
            }

        } catch (IOException e) {
            logHttpUrlConnectionErrorStream(httpUrlConnection);
            log.error("请求返回时发生IOException", e);
            throw e;
        } finally {
            httpUrlConnection.disconnect();
        }
    }


}
