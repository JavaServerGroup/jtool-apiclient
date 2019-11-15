package com.jtool.apiclient.model;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by jialechan on 2017/2/22.
 */
public abstract class MultipartItem {

    String key;

    public abstract String genContentDispositionStr();

    public abstract String genContentType();

    public abstract void genBody(OutputStream out) throws UnsupportedEncodingException;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}