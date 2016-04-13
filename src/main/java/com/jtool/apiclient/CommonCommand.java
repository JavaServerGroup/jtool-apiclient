package com.jtool.apiclient;

import com.jtool.support.log.LogPojo;
import com.jtool.support.log.LogThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static com.jtool.support.log.LogBuilder.buildLog;

public class CommonCommand {

	private static Logger log = LoggerFactory.getLogger(com.jtool.apiclient.CommonCommand.class);

	public static String readRequestByMap(String host, String uri, Map<String, Object> map) throws IOException {

		addLogSeed(map);

		if(log.isDebugEnabled()) {
			log.debug(buildLog("发送请求: curl '" + host + uri + "?" + HttpUtil.params2paramsStr(map) + "'"));
		}

		String content = ApiGet.sentByMap(host + uri, map);

		log.debug(buildLog("请求返回: " + content));

		return content;
	}

	//为了日志可往后跟踪，增加一个请求参数
	private static void addLogSeed(Map<String, Object> map) {
		LogPojo logPojo = LogThreadLocal.get();

		if(logPojo != null) {
			map.put("_comJtoolLogUuid", logPojo.getComJtoolLogUUID());
		}
	}

	public static String writeRequestByMap(String host, String uri, Map<String, Object> map) throws IOException {

		addLogSeed(map);

		if(log.isDebugEnabled()) {
			log.debug(buildLog("发送请求: curl " + host + uri + " -X POST -d '" + HttpUtil.params2paramsStr(map) + "'"));
		}

		String content = ApiPost.sentByMap(host + uri, map);

		log.debug(buildLog("请求返回: " + content));

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
