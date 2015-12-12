package com.jtool.apiclient;

import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

class ApiPost {

    private static Logger logger = LoggerFactory.getLogger(ApiPost.class);

    public static String sent(String urlStr) throws IOException {
        return sentByMap(urlStr, null);
    }

    public static String sentByBean(String urlStr,Object bean) throws IOException {
        return sentByMap(urlStr, HttpUtil.bean2Map(bean));
    }

    public static String sentByMap(String urlStr, Map<String, Object> params) throws IOException {

        if(isPostFile(params)) {
            return sentFile(urlStr, params);
        }

        String paramsString = HttpUtil.params2paramsStr(params);

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
                logger.debug("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + urlStr);
                throw new StatusCodeNot200Exception(urlStr, params, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                logger.debug("访问发生IO错误，错误流信息为：" + result);
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
                logger.debug("访问请求返回的不是200码:" + responseCode + "\t" + "url:" + url);
                throw new StatusCodeNot200Exception(url, params, responseCode);
            }
        } catch (IOException e) {
            try {
                if(httpURLConnection != null) {
                    result = HttpUtil.readAndCloseStream(httpURLConnection.getErrorStream());
                }
                logger.debug("访问发生IO错误，错误流信息为：" + result);
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

        return result;
    }


}
