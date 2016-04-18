package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import com.jtool.support.log.LogPojo;
import com.jtool.support.log.LogThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.jtool.apiclient.Util.*;
import static com.jtool.support.log.LogBuilder.buildLog;

public class ApiClient {

    private static Logger log = LoggerFactory.getLogger(ApiClient.class);

    public static Request Api(){
        return new Request();
    }

    public static class Request {

        private Request() {
        }

        private Map<String, String> header;
        private Map<String, Object> param;
        private boolean needLog = true;
        private String _logId;
        private String url;

        public Request header(Map<String, String> header){
            //header方法应该只调用一次
            if(this.header != null){
                throw new IllegalArgumentException("header方法应该只调用一次");
            }
            this.header = header;
            return this;
        }

        public Request param(Object param){
            //param方法应该只调用一次
            if(this.param != null){
                throw new IllegalArgumentException("param方法应该只调用一次");
            }

            if(param instanceof Map){
                this.param = (Map)param;
            } else {
                this.param = Util.bean2Map(param);
            }

            return this;
        }

        public Request needLog(boolean needLog){
            this.needLog = needLog;
            return this;
        }

        public Request logId(String _logId){
            this._logId = _logId;
            return this;
        }

        public String get(String url) throws IOException {
            this.url = url;
            Map<String, Object> params = this.getParam();

            if(params == null) {
                params = new HashMap<String, Object>();
            }

            if(this.needLog) {
                if(this._logId != null && !"".equals(this._logId)) {
                    params.put(LogPojo.logKey, this._logId);
                } else {
                    params.put(LogPojo.logKey, LogThreadLocal.getLogId());
                }
            }

            this.param = params;

            return processGet(this);
        }

        public String post(String url) throws IOException {
            this.url = url;
            Map<String, Object> params = this.getParam();

            if(params == null) {
                params = new HashMap<String, Object>();
            }

            if(this.needLog) {
                if(this._logId != null && !"".equals(this._logId)) {
                    params.put(LogPojo.logKey, this._logId);
                } else {
                    params.put(LogPojo.logKey, LogThreadLocal.getLogId());
                }
            }

            this.param = params;

            return processPost(this);
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

        String paramsString = Util.params2paramsStr(params);

        if(!"".equals(paramsString)) {
            urlStr += "?" + paramsString;
        }

        log.debug(buildLog("发送请求: curl " + makeHeaderLogString(header) +  " '" + urlStr + "'"));

        HttpURLConnection httpURLConnection = null;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            addHeaderToHttpURLConnection(header, httpURLConnection);

            try {
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == 200) {
                    result = Util.readAndCloseStream(httpURLConnection.getInputStream());
                } else if (300 < responseCode && responseCode < 400) {
                    throw new RedirectException(httpURLConnection.getHeaderField("Location"));
                } else {
                    logHttpURLConnectionErrorStream(httpURLConnection);
                    throw new StatusCodeNot200Exception(urlStr, params, responseCode);
                }
            } catch (RedirectException re) {
                return request.get(re.getUrl());
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

        log.debug(buildLog("请求返回: " + result));

        return result;
    }

    private static String processPost(Request request) throws IOException {

        String urlStr = request.getUrl();
        Map<String, String> header = request.getHeader();
        Map<String, Object> params = request.getParam();

        String paramsString = Util.params2paramsStr(params);

        log.debug(buildLog("发送请求: curl '" + urlStr + "' " + makeHeaderLogString(header) + " -X POST -d '" + paramsString + "'"));

        if(isPostFile(params)) {
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

            if(!"".equals(paramsString)) {
                httpURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded; charset=utf-8");
                byte[] data = paramsString.getBytes("UTF-8");
                httpURLConnection.setFixedLengthStreamingMode(data.length);

                OutputStream out = null;
                try {
                    out = new BufferedOutputStream(httpURLConnection.getOutputStream());
                    out.write(data);
                    out.flush();
                } finally {
                    if(out != null) {
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

            try {
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == 200) {
                    result = Util.readAndCloseStream(httpURLConnection.getInputStream());
                } else if (300 < responseCode && responseCode < 400) {
                    throw new RedirectException(httpURLConnection.getHeaderField("Location"));
                } else {
                    logHttpURLConnectionErrorStream(httpURLConnection);
                    throw new StatusCodeNot200Exception(urlStr, params, responseCode);
                }
            } catch (RedirectException re) {
                return request.post(re.getUrl());
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

        log.debug(buildLog("请求返回: " + result));

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

                for (String key : params.keySet()) {
                    Object param = params.get(key);
                    if (param != null) {
                        if (param instanceof File) {
                            File file = (File) param;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(PREFIX);
                            stringBuilder.append(BOUNDARY);
                            stringBuilder.append(LINE_END);
                            stringBuilder.append("Content-Disposition: form-data;name=\"" + key + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
                            stringBuilder.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
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

                out.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes());
                out.flush();
            } finally {
                if(out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        out = null;
                    }
                }
            }

            try {
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == 200) {
                    result = Util.readAndCloseStream(httpURLConnection.getInputStream());
                } else if (300 < responseCode && responseCode < 400) {
                    throw new RedirectException(httpURLConnection.getHeaderField("Location"));
                } else {
                    logHttpURLConnectionErrorStream(httpURLConnection);
                    throw new StatusCodeNot200Exception(url, params, responseCode);
                }
            } catch (RedirectException re) {
                return request.post(re.getUrl());
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

        log.debug(buildLog("请求返回: " + result));

        return result;
    }
}