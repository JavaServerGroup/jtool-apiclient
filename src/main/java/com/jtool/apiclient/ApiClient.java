package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class.getName());

    public static Request Api() {
        return new Request();
    }

    public static class Request {

        private Map<String, String> header;
        private Map<String, Object> param;
        private String _logId;
        private String url;

        private Request() {
        }

        public Request header(Map<String, String> header) {
            //header方法应该只调用一次
            if (this.header != null) {
                throw new IllegalArgumentException("header方法应该只调用一次");
            }
            this.header = header;
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

        private void addLogSeed() {
            Map<String, String> header = this.header == null ? new HashMap<String, String>() : this.header;
            if (this._logId != null && !"".equals(this._logId)) {
                header.put(LogHelper.JTOOL_LOG_ID, this._logId);
            } else if(LogHelper.getLogId() != null){
                header.put(LogHelper.JTOOL_LOG_ID, LogHelper.getLogId());
            }
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

        String paramsString = params2paramsStr(params);

        log.debug("发送请求: curl '" + urlStr + "' " + makeHeaderLogString(header) + " -X POST -d '" + paramsString + "'");

        if (isPostFile(params)) {
            return sentFile(request);
        }

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            addHeaderToHttpURLConnection(header, httpURLConnection);

            if (!"".equals(paramsString)) {
                httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");
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
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL uri = new URL(url);

            httpURLConnection = (HttpURLConnection) uri.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Charset", CHARSET);
            httpURLConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

            addHeaderToHttpURLConnection(header, httpURLConnection);

            OutputStream out = null;
            try {
                out = new BufferedOutputStream(httpURLConnection.getOutputStream());

                for(Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    Object param = entry.getValue();
                    if (param != null) {
                        if (param instanceof File) {
                            File file = (File) param;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(PREFIX);
                            stringBuilder.append(BOUNDARY);
                            stringBuilder.append(LINE_END);
                            stringBuilder.append("Content-Disposition: form-data;name=\"" + key + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
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
            if(os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    os = null;
                }
            }
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String params2paramsStr(Map<String, Object> params) {
        StringBuffer paramsString = new StringBuffer();
        if(params != null && params.size() > 0) {

            for(Map.Entry<String, Object> entry : params.entrySet()){
                String key = entry.getKey();
                Object value = entry.getValue();
                if(paramsString.length() > 0) {
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
        }
        return paramsString.toString();
    }

    private static Map<String, Object> bean2Map(Object obj) {
        Map<String, Object> map = new HashMap<String, Object>();

        if(obj == null){
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

                    if(value != null) {
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
        if(header != null) {
            for(Map.Entry<String, String> entry : header.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private static boolean isPostFile(Map<String, Object> params) {
        if(params == null) {
            return false;
        }
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(entry.getValue() instanceof File) {
                return true;
            }
        }
        return false;
    }

    private static String makeHeaderLogString(Map<String, String> header) {
        if(header == null) {
            return "";
        } else {
            StringBuffer headerStr = new StringBuffer();
            for(Map.Entry<String, String> entry : header.entrySet()) {
                headerStr.append(" -H '");
                headerStr.append(entry.getKey());
                headerStr.append(": ");
                headerStr.append(entry.getValue());
                headerStr.append("' ");
            }
            return headerStr.toString();
        }
    }

    private static void logHttpURLConnectionErrorStream(HttpURLConnection httpURLConnection) {
        try {
            if(httpURLConnection != null) {
                String errorStream = readAndCloseStream(httpURLConnection.getErrorStream());
                log.error("访问发生异常，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + errorStream);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String getContentTypeByFilename(String filename) {

        int index = filename.lastIndexOf(".");

        if(index == -1) {
            return "application/octet-stream";
        }

        String suffix = filename.substring(index);

        if(".gif".equalsIgnoreCase(suffix)) {
            return "image/gif";
        } else if(".jpg".equalsIgnoreCase(suffix) || ".jpeg".equalsIgnoreCase(suffix)){
            return "image/jpeg";
        } else if(".png".equalsIgnoreCase(suffix)) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}