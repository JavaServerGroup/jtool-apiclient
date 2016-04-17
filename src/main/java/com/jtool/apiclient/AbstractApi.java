package com.jtool.apiclient;

import com.jtool.support.log.LogPojo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractApi {

    public String sent(String urlStr) throws IOException {
        return process(urlStr, null, true);
    }

    public String sent(String urlStr, String logId) throws IOException {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put(LogPojo.logKey, logId);
        return process(urlStr, param, true);
    }

    public String sent(String urlStr, boolean needLog) throws IOException {
        return process(urlStr, null, needLog);
    }

    public String sent(String urlStr, Object param) throws IOException {
        return sent(urlStr, param, true);
    }

    public String sent(String urlStr, Object param, String logId) throws IOException {

        if(param == null) {
            if(logId != null && !"".equals(logId)) {
                return sent(urlStr, logId, true);
            } else {
                return sent(urlStr, true);
            }
        }

        Map paramMap = new HashMap();

        if (param instanceof Map) {
            ((Map) param).put(LogPojo.logKey, logId);
        } else {
            paramMap = HttpUtil.bean2Map(param);
        }

        paramMap.put(LogPojo.logKey, logId);

        return sent(urlStr, paramMap, true);
    }

    public String sent(String urlStr, Object param, boolean needLog) throws IOException {
        if(param == null) {
            return sent(urlStr);
        }

        if (param instanceof Map) {
            return process(urlStr, (Map) param, needLog);
        } else {
            return process(urlStr, HttpUtil.bean2Map(param), needLog);
        }
    }

    protected abstract String process(String urlStr, Map<String, Object> params, boolean needLog) throws IOException ;
}
