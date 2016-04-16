package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static com.jtool.support.log.LogBuilder.buildLog;

class ApiGet {

    private static Logger log = LoggerFactory.getLogger(ApiGet.class);

    public static String sent(String urlStr) throws IOException {
        return process(urlStr, null, true);
    }

    public static String sent(String urlStr, boolean needLog) throws IOException {
        return process(urlStr, null, needLog);
    }

    public static String sent(String urlStr, Object param) throws IOException {
        return sent(urlStr, param, true);
    }

    public static String sent(String urlStr, Object param, boolean needLog) throws IOException {
        if(param == null) {
            return sent(urlStr);
        }

        if (param instanceof Map) {
            return process(urlStr, (Map) param, needLog);
        } else {
            return process(urlStr, HttpUtil.bean2Map(param), needLog);
        }
    }

    private static String process(String urlStr, Map<String, Object> params, boolean needLog) throws IOException {

        String paramsString = HttpUtil.params2paramsStr(params);

        if(!"".equals(paramsString)) {
            urlStr += "?" + paramsString;
        }

        log.debug(buildLog("发送请求: curl '" + urlStr + "'"));

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                result = HttpUtil.readAndCloseStream(httpURLConnection.getInputStream());
            } else if(300 < responseCode && responseCode < 400) {
                return sent(httpURLConnection.getHeaderField("Location"));
            } else {
                log.debug(buildLog("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + urlStr));
                throw new StatusCodeNot200Exception(urlStr, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                log.debug(buildLog("访问发生IO错误，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + result));
            } catch(IOException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
            throw new IOException();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        log.debug(buildLog("请求返回: " + result));

        return result;
    }

}
