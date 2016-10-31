package com.jtool.apiclient;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import com.jtool.support.encrypt.AES256Cipher;
import com.jtool.support.encrypt.EncryptPojo;
import com.jtool.support.log.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class.getName());

    private static String PREFIX = "--";
    private static String LINE_END = "\r\n";
    private static String MULTIPART_FROM_DATA = "multipart/form-data";
    private static String CHARSET = "UTF-8";

    public static Request Api() {
        return new Request();
    }

    public static class Request {

        private Map<String, String> header;
        private Map<String, Object> param;
        private String _logId;
        private String url;
        private boolean isRest;
        private int connectionTimeout = 30000;
        private int readTimeout = 30000;
        private EncryptPojo encryptPojo;
        private boolean isEncryption = false;

        private Request() {
        }

        public Request encryptionSeed(EncryptPojo encryptPojo) {
            isEncryption = true;
            this.encryptPojo = encryptPojo;

            if(this.header == null) {
                this.header = new HashMap<String, String>();
            }

            this.header.put("encryptId", encryptPojo.getEncryptId());

            return this;
        }

        public Request header(Map<String, String> header) {
            //header方法应该只调用一次
            if (this.header != null) {
                throw new IllegalArgumentException("header方法应该只调用一次");
            }
            this.header = header;
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

            if (param instanceof Map) {
                this.param = (Map) param;
            } else {
                this.param = bean2Map(param);
            }

            return this;
        }

        public Request logId(String _logId) {
            this._logId = _logId;
            return this;
        }

        public String get(String url) throws IOException {
            this.url = url;
            addLogSeed();
            return processGet(this);
        }

        public String post(String url) throws IOException {
            this.url = url;
            addLogSeed();
            return processPost(this);
        }

        public String restPost(String url) throws IOException {
            this.url = url;
            addLogSeed();
            this.isRest = true;
            return processPost(this);
        }

        private void addLogSeed() {
            Map<String, String> header = this.header == null ? new HashMap<String, String>() : this.header;
            if (this._logId != null && !"".equals(this._logId)) {
                header.put(LogHelper.JTOOL_LOG_ID, this._logId);
            } else if (LogHelper.getLogId() != null) {
                header.put(LogHelper.JTOOL_LOG_ID, LogHelper.getLogId());
            }
            LogHelper.setLogId(header.get(LogHelper.JTOOL_LOG_ID));
            this.header = header;
        }

        private Map<String, String> getHeader() {
            return header;
        }

        private Map<String, Object> getParam() {
            return param;
        }

        private String getUrl() {
            return url;
        }
    }

    private static String processGet(Request request) throws IOException {

        String urlStr = request.getUrl();
        Map<String, String> header = request.getHeader();
        Map<String, Object> params = request.getParam();

        String paramsString = params2paramsStr(params);

        if (!"".equals(paramsString)) {
            if (urlStr.contains("?")) {
                urlStr += "&" + paramsString;
            } else {
                urlStr += "?" + paramsString;
            }
        }

        log.debug("发送请求: curl " + makeHeaderLogString(header) + " '" + urlStr + "'");

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setConnectTimeout(request.connectionTimeout);
            httpURLConnection.setReadTimeout(request.readTimeout);
            addHeaderToHttpURLConnection(header, httpURLConnection);

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                result = readAndCloseStream(httpURLConnection.getInputStream());
            } else {
                logHttpURLConnectionErrorStream(httpURLConnection);
                throw new StatusCodeNot200Exception(urlStr, params, responseCode);
            }

        } catch (IOException e) {
            logHttpURLConnectionErrorStream(httpURLConnection);
            e.printStackTrace();
            throw e;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        log.debug("请求返回: " + result);

        return result;
    }

    private static String processPost(Request request) throws IOException {

        String urlStr = request.getUrl();
        Map<String, String> header = request.getHeader();
        Map<String, Object> params = request.getParam();

        String paramsString;
        if (request.isRest) {
            paramsString = JSON.toJSONString(params);
        } else {
            paramsString = params2paramsStr(params);
        }

        if (request.isEncryption) {
            paramsString = AES256Cipher.encrypt(request.encryptPojo, paramsString);
        }

        log.debug("发送请求: curl '" + urlStr + "' " + makeHeaderLogString(header, request.isRest) + " -X POST -d '" + paramsString + "'");

        if (isPostFile(params)) {
            return sentFile(request);
        }

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setConnectTimeout(request.connectionTimeout);
            httpURLConnection.setReadTimeout(request.readTimeout);
            httpURLConnection.setDoOutput(true);

            addHeaderToHttpURLConnection(header, httpURLConnection);

            if (!"".equals(paramsString)) {
                if (request.isRest) {
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                } else {
                    httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                }
                byte[] data = paramsString.getBytes("UTF-8");
                httpURLConnection.setFixedLengthStreamingMode(data.length);

                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(httpURLConnection.getOutputStream());
                    out.write(data);
                    out.flush();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            out = null;
                        }
                    }
                }
            } else {
                httpURLConnection.setFixedLengthStreamingMode(0);
            }

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                result = readAndCloseStream(httpURLConnection.getInputStream());
            } else {
                logHttpURLConnectionErrorStream(httpURLConnection);
                throw new StatusCodeNot200Exception(urlStr, params, responseCode);
            }

        } catch (IOException e) {
            logHttpURLConnectionErrorStream(httpURLConnection);
            e.printStackTrace();
            throw e;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        log.debug("请求返回: " + result);

        return result;
    }

    private static String sentFile(Request request) throws IOException {

        String url = request.getUrl();
        Map<String, String> header = request.getHeader();
        Map<String, Object> params = request.getParam();

        String BOUNDARY = UUID.randomUUID().toString();
        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL uri = new URL(url);

            httpURLConnection = (HttpURLConnection) uri.openConnection();
            httpURLConnection.setConnectTimeout(request.connectionTimeout);
            httpURLConnection.setReadTimeout(request.readTimeout);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", CHARSET);
            httpURLConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

            addHeaderToHttpURLConnection(header, httpURLConnection);

            OutputStream out = null;
            try {
                out = new BufferedOutputStream(httpURLConnection.getOutputStream());

                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object param = entry.getValue();
                    if (param != null) {

                        if (param instanceof List && ((List) param).size() > 0 && allListItemIsFile((List) param)) {
                            for (Object file : ((List) param)) {
                                genPostFile(key, (File) file, out, BOUNDARY);
                            }
                        } else if (param instanceof File) {
                            genPostFile(key, (File) param, out, BOUNDARY);
                        } else {
                            String paramStr = param.toString();
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(PREFIX);
                            stringBuilder.append(BOUNDARY);
                            stringBuilder.append(LINE_END);
                            stringBuilder.append("Content-Disposition: form-data;name=\"" + key + "\"" + LINE_END);
                            stringBuilder.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                            stringBuilder.append(LINE_END);
                            stringBuilder.append(paramStr);
                            stringBuilder.append(LINE_END);

                            out.write(stringBuilder.toString().getBytes("UTF-8"));
                        }
                    }
                }

                out.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes("UTF-8"));
                out.flush();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        out = null;
                    }
                }
            }

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                result = readAndCloseStream(httpURLConnection.getInputStream());
            } else {
                logHttpURLConnectionErrorStream(httpURLConnection);
                throw new StatusCodeNot200Exception(url, params, responseCode);
            }
        } catch (IOException e) {
            logHttpURLConnectionErrorStream(httpURLConnection);
            e.printStackTrace();
            throw e;
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        log.debug("请求返回: " + result);

        return result;
    }

    private static void genPostFile(String key, File file, OutputStream out, String BOUNDARY) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PREFIX);
        stringBuilder.append(BOUNDARY);
        stringBuilder.append(LINE_END);
        stringBuilder.append("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
        stringBuilder.append("Content-Type: " + getContentTypeByFilename(file.getName()) + "; charset=" + CHARSET + LINE_END);
        stringBuilder.append(LINE_END);

        out.write(stringBuilder.toString().getBytes("UTF-8"));
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    fileInputStream = null;
                }
            }
        }
        out.write(LINE_END.getBytes("UTF-8"));
    }

    private static String readAndCloseStream(InputStream is) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toString("UTF-8");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    os = null;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String params2paramsStr(Map<String, Object> params) {
        StringBuffer paramsString = new StringBuffer();
        if (params != null && params.size() > 0) {

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof List) {
                    for (Object v : (List) value) {
                        genParamsStrItem(paramsString, key, v);
                    }
                } else {
                    genParamsStrItem(paramsString, key, value);
                }
            }
        }
        return paramsString.toString();
    }

    private static void genParamsStrItem(StringBuffer paramsString, String key, Object value) {
        if (paramsString.length() > 0) {
            paramsString.append("&");
        }
        try {
            paramsString.append(URLEncoder.encode(key, "UTF-8"));
            paramsString.append("=");
            paramsString.append(URLEncoder.encode(value.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Object> bean2Map(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();

        if (obj == null) {
            return map;
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!"class".equals(key)) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    if (value != null) {
                        map.put(key, value);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.debug("将bean转化为map时发生错误：" + obj);
            throw new RuntimeException("apiClient中将bean转化为map时发生错误");
        }

        return map;
    }

    private static void addHeaderToHttpURLConnection(Map<String, String> header, HttpURLConnection httpURLConnection) {
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private static boolean isPostFile(Map<String, Object> params) {
        boolean result = false;

        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof File) {
                    result = true;
                }
                if (entry.getValue() instanceof List && listItemHasFile((List) entry.getValue())) {
                    if (allListItemIsFile((List) entry.getValue())) {
                        result = true;
                    } else {
                        throw new RuntimeException("列表的项应该全为File");
                    }
                }
            }
        }

        return result;
    }

    private static boolean listItemHasFile(List list) {

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof File) {
                return true;
            }
        }

        return false;
    }

    private static boolean allListItemIsFile(List list) {

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (!(obj instanceof File)) {
                return false;
            }
        }

        return true;
    }

    private static String makeHeaderLogString(Map<String, String> header, boolean isRest) {
        if (header == null) {
            return "";
        } else {
            StringBuffer headerStr = new StringBuffer();
            for (Map.Entry<String, String> entry : header.entrySet()) {
                headerStr.append(" -H '");
                headerStr.append(entry.getKey());
                headerStr.append(": ");
                headerStr.append(entry.getValue());
                headerStr.append("' ");
            }

            if (isRest) {
                headerStr.append(" -H 'Content-Type:application/json' ");
            }

            return headerStr.toString();
        }
    }

    private static String makeHeaderLogString(Map<String, String> header) {
        return makeHeaderLogString(header, false);
    }

    private static void logHttpURLConnectionErrorStream(HttpURLConnection httpURLConnection) {
        try {
            if (httpURLConnection != null) {
                String errorStream = readAndCloseStream(httpURLConnection.getErrorStream());
                log.error("访问发生异常，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + errorStream);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getContentTypeByFilename(String filename) {

        int index = filename.lastIndexOf(".");

        if (index == -1) {
            return "application/octet-stream";
        }

        String suffix = filename.substring(index);

        if (".gif".equalsIgnoreCase(suffix)) {
            return "image/gif";
        } else if (".jpg".equalsIgnoreCase(suffix) || ".jpeg".equalsIgnoreCase(suffix)) {
            return "image/jpeg";
        } else if (".png".equalsIgnoreCase(suffix)) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }













}