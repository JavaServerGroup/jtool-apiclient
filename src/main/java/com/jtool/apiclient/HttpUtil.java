package com.jtool.apiclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

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
                    paramsString += paramName + "=" + params.get(paramName).toString();
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
            logger.debug("将bean转化为map时发生错误：" + obj);
        }

        return map;
    }

}