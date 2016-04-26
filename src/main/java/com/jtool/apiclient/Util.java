package com.jtool.apiclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.jtool.support.log.LogBuilder.buildLog;

class Util {

    private static Logger log = LoggerFactory.getLogger(Util.class);

    static String readAndCloseStream(InputStream is) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            return os.toString();
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
                    is = null;
                }
            }
        }
    }

    static String params2paramsStr(Map<String, Object> params) {
        String paramsString = "";
        if(params != null && params.size() > 0) {
            for(String paramName : params.keySet()) {
                if(params.get(paramName) != null) {
                    if (!"".equals(paramsString)) {
                        paramsString += "&";
                    }
                    try {
                        paramsString += URLEncoder.encode(paramName, "UTF-8") + "=" + URLEncoder.encode(params.get(paramName).toString(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return paramsString;
    }

    static Map<String, Object> bean2Map(Object obj) {
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
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);

                    if(value != null) {
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.debug(buildLog("将bean转化为map时发生错误：" + obj));
        }

        return map;
    }

    static void addHeaderToHttpURLConnection(Map<String, String> header, HttpURLConnection httpURLConnection) {
        if(header != null) {
            for (String key : header.keySet()) {
                httpURLConnection.setRequestProperty(key, header.get(key));
            }
        }
    }

    static boolean isPostFile(Map<String, Object> params) {
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

    static String makeHeaderLogString(Map<String, String> header) {
        String headerStr = "";
        if(header != null) {
            for(String headerKey : header.keySet()) {
                headerStr += " -H '" + headerKey + ": " + header.get(headerKey) + "' ";
            }
        }
        return headerStr;
    }

    static void logHttpURLConnectionErrorStream(HttpURLConnection httpURLConnection) {
        try {
            if(httpURLConnection != null) {
                String errorStream = Util.readAndCloseStream(httpURLConnection.getErrorStream());
                log.error(buildLog("访问发生异常，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + errorStream));
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    static String getContentTypeByFilename(String filename) {

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