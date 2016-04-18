package com.jtool.apiclient;

class RedirectException extends RuntimeException {
    private String url;

    public RedirectException(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
