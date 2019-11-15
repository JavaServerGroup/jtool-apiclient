package com.jtool.apiclient.model;

import java.util.HashMap;

/**
 * Created by jialechan on 2017/2/22.
 */
public class ParamMap extends HashMap<String, Object> {

    public ParamMap add(String key, Object value) {
        this.put(key, value);
        return this;
    }

    public static ParamMap newInstance() {
        return new ParamMap();
    }
}