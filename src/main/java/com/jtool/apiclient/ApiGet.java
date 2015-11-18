package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ApiGet {

    private static Logger logger = LoggerFactory.getLogger(ApiGet.class);

    public static String sentByBean(String urlStr, Object bean) throws IOException {
        return sentByMap(urlStr, HttpUtil.bean2Map(bean));
    }

    public static String sentByMap(String urlStr, Map<String, Object> params) throws IOException {
        String paramsString = HttpUtil.params2paramsStr(params);
        if(!"".equals(paramsString)) {
            urlStr += "?" + paramsString;
        }
        return sent(urlStr);
    }

    public static String sent(String urlStr) throws IOException {

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
                logger.debug("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + urlStr);
                throw new StatusCodeNot200Exception(urlStr, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                logger.debug("访问发生IO错误，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + result);
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

        return result;
    }

}
