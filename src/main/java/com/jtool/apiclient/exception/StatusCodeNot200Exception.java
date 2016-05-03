package com.jtool.apiclient.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class StatusCodeNot200Exception extends RuntimeException {

	private static final long serialVersionUID = 2977215131256852771L;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private String url;
	private int statusCode;
	private Map<String, Object> params;
	
	public StatusCodeNot200Exception(String url, Map<String, Object> params, int statusCode) {
		this.url = url;
		this.params = params;
		this.statusCode = statusCode;
		logger.error(this.getStatusCode() + "\t" + this.getUrl() + "\t" + this.getParams(), this);
	}

	public String getUrl() {
		return url;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	@Override
	public String toString() {
		return "StatusCodeNot200Exception{" +
				"url='" + url + '\'' +
				", statusCode=" + statusCode +
				", params=" + params +
				'}';
	}
}
