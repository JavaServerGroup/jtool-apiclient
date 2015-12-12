package com.jtool.apiclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonCommand {

	private static Logger log = LoggerFactory.getLogger(CommonCommand.class);

	public static String readRequestByMap(String host, String uri, Map<String, Object> map) throws IOException {

		if(log.isDebugEnabled()) {
			log.debug("发送请求: curl '" + host + uri + "?" + HttpUtil.params2paramsStr(map) + "'");
		}

		String content;

		int i = 0;

		do {
			content = ApiGet.sentByMap(host + uri, map);
			i++;
		} while ((content == null || content.equals("")) && i < 5);

		log.debug("请求返回: " + content);

		return content;
	}

	public static String writeRequestByMap(String host, String uri, Map<String, Object> map) throws IOException {

		if(log.isDebugEnabled()) {
			log.debug("发送请求: curl " + host + uri + " -X POST -d \"" + HttpUtil.params2paramsStr(map) + "\"");
		}

		String content = ApiPost.sentByMap(host + uri, map);

		log.debug("请求返回: " + content);

		return content;
	}

	public static String readRequestByBean(String host, String uri, Object param) throws IOException {
		return readRequestByMap(host, uri, HttpUtil.bean2Map(param));
	}

	public static String readRequest(String host, String uri) throws IOException {
		return readRequestByMap(host, uri, null);
	}

	public static String writeRequestByBean(String host, String uri, Object param) throws IOException {
		return writeRequestByMap(host, uri, HttpUtil.bean2Map(param));
	}

}
