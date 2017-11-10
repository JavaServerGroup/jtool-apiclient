package com.jtool.apiclient.processor;

import com.jtool.apiclient.Request;
import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import com.jtool.apiclient.model.ResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import static com.jtool.apiclient.Util.*;

public abstract class Processor {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	protected Request request;

	HttpURLConnection commonPreProcess() throws IOException {
		URL mURL = new URL(request.getUrl());

		HttpURLConnection httpURLConnection = (HttpURLConnection) mURL.openConnection();
		httpURLConnection.setRequestProperty("Charset", "UTF-8");
		httpURLConnection.setConnectTimeout(request.getConnectionTimeout());
		httpURLConnection.setReadTimeout(request.getReadTimeout());

		addHeaderToHttpURLConnection(request.getHeader(), httpURLConnection);

		if (request.isGzipResponse()) {
			httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
		}

		return httpURLConnection;
	}

	abstract void processingParam();

	abstract HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException;

	private String loadResponseString(HttpURLConnection httpURLConnection, boolean loadResponseString)
			throws IOException {

		try {
			int responseCode = httpURLConnection.getResponseCode();

			if (responseCode >= 200 && responseCode <= 299) {
				return handleResponseString(httpURLConnection, loadResponseString);
			} else {
				logHttpURLConnectionErrorStream(httpURLConnection);
				throw new StatusCodeNot200Exception(request.getUrl(), request.getParam(), responseCode);
			}

		} catch (IOException e) {
			logHttpURLConnectionErrorStream(httpURLConnection);
			log.error("请求返回时发生IOException", e);
			throw e;
		} finally {
			httpURLConnection.disconnect();
		}
	}

	private ResponseWrapper loadResponseWrapper(HttpURLConnection httpURLConnection, boolean loadResponseString)
			throws IOException {
		ResponseWrapper wrapper = new ResponseWrapper();
		try {
			int responseCode = httpURLConnection.getResponseCode();
			wrapper.setResponseCode(responseCode);
			wrapper.setResponseHeader(httpURLConnection.getHeaderFields());

			if (responseCode >= 200 && responseCode <= 299) {
				wrapper.setResponseBody(handleResponseString(httpURLConnection, loadResponseString));
				return wrapper;
			} else {
				logHttpURLConnectionErrorStream(httpURLConnection);
				throw new StatusCodeNot200Exception(request.getUrl(), request.getParam(), responseCode);
			}

		} catch (IOException e) {
			logHttpURLConnectionErrorStream(httpURLConnection);
			log.error("请求返回时发生IOException", e);
			throw e;
		} finally {
			httpURLConnection.disconnect();
		}
	}

	private String handleResponseString(HttpURLConnection httpURLConnection, boolean loadResponseString)
			throws IOException {
		try (InputStream is = getInputStreamByConnection(httpURLConnection)) {
			if (loadResponseString) {
				
				final String result = readAndCloseStream(is);
				log.debug("返回: {}", result);
				return result;
			} else {
				return "";
			}
		}
	}

	private InputStream getInputStreamByConnection(HttpURLConnection httpURLConnection) throws IOException {
		if ("gzip".equals(httpURLConnection.getContentEncoding())) {
			return new GZIPInputStream(httpURLConnection.getInputStream());
		} else {
			return httpURLConnection.getInputStream();
		}
	}

	public String process() throws IOException {
		return process(true);
	}

	public String process(boolean loadResponseString) throws IOException {
		final HttpURLConnection httpURLConnection = preprocess();
		return loadResponseString(httpURLConnection, loadResponseString);
	}

	/**
	 * 扩展处理
	 * 
	 * @return
	 * @throws IOException
	 */
	public ResponseWrapper processExt() throws IOException {
		final HttpURLConnection httpURLConnection = preprocess();
		return loadResponseWrapper(httpURLConnection, true);
	}

	/**
	 * 预处理
	 * 
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection preprocess() throws IOException {
		processingParam();
		final HttpURLConnection httpURLConnection = commonPreProcess();
		doProcess(httpURLConnection);
		return httpURLConnection;
	}
}
