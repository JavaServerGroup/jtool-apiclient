package com.jtool.apiclient;

import com.jtool.apiclient.exception.Pojo2MapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.ApiClient.getCharsetName;

public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class.getClass());

    private Util() {
    }

    public static String params2paramsStr(Object paramsObj) {

        Map<String, Object> params;

        if(paramsObj == null) {
            return "";
        } else {
            if(paramsObj instanceof Map) {
                params = (Map<String, Object>)paramsObj;
            } else {
                params = obj2Map(paramsObj);
            }
        }

        if (params.size() > 0) {

            List<String> paramsStrItemList = new ArrayList<>();

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof List) {
                    paramsStrItemList.addAll(genParamsStrItemList(entry.getKey(), (List)entry.getValue()));
                } else {
                    paramsStrItemList.add(genParamsStrItem(entry.getKey(), entry.getValue()));
                }
            }

            return joinParams(paramsStrItemList);
        } else {
            return "";
        }

    }

    private static List<String> genParamsStrItemList(String key, List list) {

        List<String> result = new ArrayList<>();

        if(list == null || list.isEmpty()) {
            return result;
        }

        for (Object v : list) {
            result.add(genParamsStrItem(key, v));
        }
        return result;
    }

    private static String joinParams(List<String> paramsList) {
        StringBuilder stringBuilder = new StringBuilder();
        if(paramsList == null || paramsList.isEmpty()) {
            return stringBuilder.toString();
        } else {
            for(int i = 0; i < paramsList.size(); i++) {
                if(i != 0) {
                    stringBuilder.append("&");
                    stringBuilder.append(paramsList.get(i));
                } else {
                    stringBuilder.append(paramsList.get(i));
                }
            }
            return stringBuilder.toString();
        }
    }

    private static String genParamsStrItem(String key, Object value) {
        if(key == null || value == null) {
            return "";
        } else {
            StringBuilder paramsString = new StringBuilder();
            try {
                paramsString.append(URLEncoder.encode(key, getCharsetName()));
                paramsString.append("=");
                paramsString.append(URLEncoder.encode(value.toString(), getCharsetName()));
            } catch (UnsupportedEncodingException e) {
                log.error("不支持字符编码", e);
            }
            return paramsString.toString();
        }
    }

    public static String makeHeaderLogString(Map<String, String> header) {
        return makeHeaderLogString(header, false);
    }

    public static String makeHeaderLogString(Map<String, String> header, boolean isRest) {
        if (header == null) {
            return "";
        } else {
            StringBuilder headerStr = new StringBuilder();
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

    public static void addHeaderToHttpURLConnection(Map<String, String> header, HttpURLConnection httpURLConnection) {
        for (Map.Entry<String, String> entry : header.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    public static String readAndCloseStream(InputStream is) throws IOException {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); BufferedInputStream bufferedInputStream = new BufferedInputStream(is)){

            byte[] buffer = new byte[1024];
            int len;

            while ((len = bufferedInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }

            return os.toString("UTF-8");
        }
    }

    public static void writeAndCloseStream(OutputStream out, byte[] data) throws IOException {
        try (OutputStream outputStream = new BufferedOutputStream(out)){
            outputStream.write(data);
            outputStream.flush();
        }
    }

    public static void logHttpURLConnectionErrorStream(HttpURLConnection httpURLConnection) {
        try {
            if (httpURLConnection != null) {
                String errorStream = readAndCloseStream(httpURLConnection.getErrorStream());
                if(log.isErrorEnabled()) {
                    log.error("访问发生异常，错误码是：" + httpURLConnection.getResponseCode() + "\t错误流信息为：" + errorStream);
                }
            }
        } catch (IOException ex) {
            log.error("读取HttpURLConnection的error流时出错", ex);
        }
    }

    public static Map<String, Object> obj2Map(Object obj) {
        Map<String, Object> map = new HashMap<>();

        if (obj == null) {
            return map;
        }

        if(obj instanceof Map) {
            for(Object key : ((Map)obj).keySet()) {
                map.put(key.toString(), ((Map)obj).get(key));
            }
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
        } catch (Exception e) {
            log.error("将bean转化为map时发生错误：" + obj, e);
            throw new Pojo2MapException(e);
        }

        return map;
    }
}
