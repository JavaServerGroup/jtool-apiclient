package com.jtool.apiclient.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCodeNot200Exception extends RuntimeException {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final String url;
	private final int statusCode;
	private final Object params;
	
	public StatusCodeNot200Exception(String url, Object params, int statusCode) {
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

	public Object getParams() {
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
