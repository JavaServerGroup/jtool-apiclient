package com.jtool.apiclient;

import com.jtool.apiclient.model.ParamMap;
import com.jtool.apiclient.model.ResponseWrapper;
import com.jtool.apiclient.processor.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    private Map<String, String> header = new HashMap<>();
    private Object param;
    private String url;
    private boolean isRest;
    private int connectionTimeout = ApiClient.getConnectionTimeout();
    private int readTimeout = ApiClient.getReadTimeout();
    private String paramsString;
    private boolean isWithClassName;
    private boolean isGzipResponse = false;
    private boolean isFollowRedirects = true;

    protected Request() {
    }

    public String getParamsString() {
        return paramsString;
    }

    public void setParamsString(String paramsString) {
        this.paramsString = paramsString;
    }

    public boolean isWithClassName() {
        return this.isWithClassName;
    }

    public Request isWithClassName(boolean isWithClassName) {
        this.isWithClassName = isWithClassName;
        return this;
    }

    public Request header(Map<String, String> header) {
        this.header.putAll(header);
        return this;
    }

    public Request header(String key, String value) {
        this.header.put(key, value);
        return this;
    }

    public Request setConnectionTimeout(int connectionTimeout) {
        if (connectionTimeout < 1) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public Request setReadTimeout(int readTimeout) {
        if (connectionTimeout < 1) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }
        this.readTimeout = readTimeout;
        return this;
    }

    public Request param(Object param) {
        //param方法应该只调用一次
        if (this.param != null) {
            throw new IllegalArgumentException("param方法应该只调用一次");
        }
        this.param = param;
        return this;
    }

    public Request gzipResponse() {
        this.isGzipResponse = true;
        return this;
    }

    public Request paramMap(ParamMap paramMap) {
        return this.param(paramMap);
    }

    public String get(String url) throws IOException {
        this.url = url;
        return getResponseWrapper(url).getResponseBody();
    }

    public <T> T get(String url, Class<T> clazz) throws IOException {
        return getResponseWrapper(url).getResponseBody(clazz);
    }

    public String post(String url) throws IOException {
        this.url = url;
        return postResponseWrapper(url).getResponseBody();
    }

    public <T> T post(String url, Class<T> clazz) throws IOException {
        return postResponseWrapper(url).getResponseBody(clazz);
    }

    public String restPost(String url) throws IOException {
        this.url = url;
        return restPostResponseWrapper(url).getResponseBody();
    }

    public <T> T restPost(String url, Class<T> clazz) throws IOException {
        return restPostResponseWrapper(url).getResponseBody(clazz);
    }

    public String filePost(String url) throws IOException {
        this.url = url;
        return filePostResponseWrapper(url).getResponseBody();
    }

    public <T> T filePost(String url, Class<T> clazz) throws IOException {
        return filePostResponseWrapper(url).getResponseBody(clazz);
    }

    public Map<String, List<String>> head(String url) throws IOException {
        this.url = url;
        Processor processor = new HeadProcessor(this);
        processor.process(false);
        return ((HeadProcessor)processor).getResponseHeader();
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param;
    }

    public boolean isRest() {
        return isRest;
    }

    public void setRest(boolean rest) {
        isRest = rest;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isGzipResponse() {
        return isGzipResponse;
    }

    public boolean isFollowRedirects() {
        return isFollowRedirects;
    }

    public Request setFollowRedirects(boolean followRedirects) {
        isFollowRedirects = followRedirects;
        return this;
    }

    /**
     * 增加wrapper返回
     */
	public ResponseWrapper getResponseWrapper(String url) throws IOException {
		this.url = url;
		return new GetProcessor(this).process();
	}

    public ResponseWrapper postResponseWrapper(String url) throws IOException {
        this.url = url;
        Processor processor = new PostProcessor(this);
        return processor.process();
    }

    public ResponseWrapper restPostResponseWrapper(String url) throws IOException {
        this.url = url;
        Processor processor = new RestPostProcessor(this);
        return processor.process();
    }

    public ResponseWrapper filePostResponseWrapper(String url) throws IOException {
        this.url = url;
        Processor processor = new MultipartPostProcessor(this);
        return processor.process();
    }

}
