package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static com.jtool.support.log.LogBuilder.buildLog;

class ApiPost {

    private static Logger log = LoggerFactory.getLogger(ApiPost.class);

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

        if(needLog) {
            params = HttpUtil.addLogSeed(params);
        }

        if(isPostFile(params)) {
            return sentFile(urlStr, params);
        }

        String paramsString = HttpUtil.params2paramsStr(params);

        log.debug(buildLog("发送请求: curl " + urlStr + " -X POST -d '" + paramsString + "'"));

        HttpURLConnection httpURLConnection = null;
        InputStream is;
        String result = null;

        try {
            URL mURL = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) mURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
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

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                is = new BufferedInputStream(httpURLConnection.getInputStream());
                result = HttpUtil.readAndCloseStream(is);
            } else if(300 < responseCode && responseCode < 400) {
                return sent(httpURLConnection.getHeaderField("Location"));
            } else {
                log.debug(buildLog("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + urlStr));
                throw new StatusCodeNot200Exception(urlStr, params, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                log.debug(buildLog("访问发生IO错误，错误流信息为：" + result));
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

    private static boolean isPostFile(Map<String, Object> params) {
        if(params == null) {
            return false;
        }
        for (String key : params.keySet()) {
            Object param = params.get(key);
            if (param instanceof File) {
                return true;
            }
        }
        return false;
    }

    private static String sentFile(String url, Map<String, Object> params) throws IOException {

        String BOUNDARY = UUID.randomUUID().toString();
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";

        HttpURLConnection httpURLConnection = null;
        BufferedOutputStream bufferedOutputStream = null;
        String result = null;

        try {
            URL uri = new URL(url);

            httpURLConnection = (HttpURLConnection) uri.openConnection();
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Charset", CHARSET);
            httpURLConnection.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

            bufferedOutputStream = new BufferedOutputStream(httpURLConnection.getOutputStream());

            for (String key : params.keySet()) {
                Object param = params.get(key);
                if(param != null) {
                    if (param instanceof File) {
                        File file = (File) param;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(PREFIX);
                        stringBuilder.append(BOUNDARY);
                        stringBuilder.append(LINE_END);
                        stringBuilder.append("Content-Disposition: form-data;name=\"" + key + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
                        stringBuilder.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END);
                        stringBuilder.append(LINE_END);

                        bufferedOutputStream.write(stringBuilder.toString().getBytes("UTF-8"));
                        FileInputStream fileInputStream = null;

                        try {
                            fileInputStream = new FileInputStream(file);
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while ((len = fileInputStream.read(buffer)) != -1) {
                                bufferedOutputStream.write(buffer, 0, len);
                            }
                        } finally {
                            if(fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e) {
                                    fileInputStream = null;
                                }
                            }
                        }
                        bufferedOutputStream.write(LINE_END.getBytes("UTF-8"));
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

                        bufferedOutputStream.write(stringBuilder.toString().getBytes("UTF-8"));
                    }
                }
            }

            bufferedOutputStream.write((PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes());
            bufferedOutputStream.flush();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                result = HttpUtil.readAndCloseStream(httpURLConnection.getInputStream());
            } else if(300 < responseCode && responseCode < 400) {
                return sent(httpURLConnection.getHeaderField("Location"));
            } else {
                log.debug(buildLog("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + url));
                throw new StatusCodeNot200Exception(url, params, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                log.debug(buildLog("访问发生IO错误，错误流信息为：" + result));
            } catch(IOException ex) {
                e.printStackTrace();
            }
            e.printStackTrace();
            throw new IOException();
        } finally {
            if(bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    bufferedOutputStream = null;
                }
            }

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        log.debug(buildLog("请求返回: " + result));

        return result;
    }


}
