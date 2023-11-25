package com.jtool.apiclient.model;

import com.jtool.apiclient.ApiClient;
import com.jtool.apiclient.processor.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class Request {

    // http请求的header
    private Map<String, String> header = new HashMap<>();

    // http请求参数
    private Object param;

    // http请求的地址
    private String url;

    // 默认使用全局超时时间配置
    private int connectionTimeout = ApiClient.getDefaultConnectionTimeout();

    // 默认使用全局超时时间配置
    private int readTimeout = ApiClient.getDefaultReadTimeout();

    // url上的请求字符串
    private String paramsString;

    // 是否跟随跳转
    private boolean isFollowRedirects = true;

    public Request() {
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
        HeadProcessor processor = new HeadProcessor(this);
        processor.process();
        return processor.getResponseHeader();
    }

    public ResponseWrapper getResponseWrapper(String url) throws IOException {
        this.url = url;
        return new GetProcessor(this).process();
    }

    public ResponseWrapper postResponseWrapper(String url) throws IOException {
        this.url = url;
        AbstractProcessor processor = new PostProcessor(this);
        return processor.process();
    }

    public ResponseWrapper restPostResponseWrapper(String url) throws IOException {
        this.url = url;
        AbstractProcessor processor = new RestPostProcessor(this);
        return processor.process();
    }

    public ResponseWrapper filePostResponseWrapper(String url) throws IOException {
        this.url = url;
        AbstractProcessor processor = new MultipartPostProcessor(this);
        return processor.process();
    }

}
