package com.jtool.apiclient;

import com.jtool.apiclient.model.Request;
import lombok.Getter;

public class ApiClient {

    /**
     * 全局连接超时时间
     */
    @Getter
    private static int defaultConnectionTimeout = 30000;

    /**
     * 全局读超时时间
     */
    @Getter
    private static int defaultReadTimeout = 30000;

    @Getter
    private static final String charsetName = "UTF-8";

    private ApiClient() {
    }

    public static Request Api() {
        return new Request();
    }

    public static void setDefaultConnectionTimeout(int defaultConnectionTimeout) {
        ApiClient.defaultConnectionTimeout = defaultConnectionTimeout;
    }

    public static void setDefaultReadTimeout(int defaultReadTimeout) {
        ApiClient.defaultReadTimeout = defaultReadTimeout;
    }
}
