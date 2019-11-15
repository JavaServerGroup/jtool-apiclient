package com.jtool.apiclient;

public class ApiClient {

    private static int connectionTimeout = 30000;
    private static int readTimeout = 30000;
    private static String charsetName = "UTF-8";

    private ApiClient() {
    }

    public static Request Api() {
        return new Request();
    }

    public static int getConnectionTimeout() {
        return connectionTimeout;
    }

    public static void setConnectionTimeout(int connectionTimeout) {
        ApiClient.connectionTimeout = connectionTimeout;
    }

    public static int getReadTimeout() {
        return readTimeout;
    }

    public static void setReadTimeout(int readTimeout) {
        ApiClient.readTimeout = readTimeout;
    }

    public static String getCharsetName() {
        return charsetName;
    }

    public static void setCharsetName(String charsetName) {
        ApiClient.charsetName = charsetName;
    }
}