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
        httpURLConnection.setInstanceFollowRedirects(request.isFollowRedirects());

        addHeaderToHttpURLConnection(request.getHeader(), httpURLConnection);

        if(request.isGzipResponse()) {
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");
        }

        return httpURLConnection;
    }

    abstract void processingParam();

    abstract HttpURLConnection doProcess(HttpURLConnection httpURLConnection) throws IOException;

    private String loadResponseString(HttpURLConnection httpURLConnection, boolean loadResponseString) throws IOException {

        try {
        	int responseCode = httpURLConnection.getResponseCode();

			if (is2XX(responseCode) || isRedirect(responseCode)) {
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

	private boolean isRedirect(int responseCode) {
		return responseCode >= 300 && responseCode <= 399 && !request.isFollowRedirects();
	}

	private boolean is2XX(int responseCode) {
		return responseCode >= 200 && responseCode <= 299;
	}

	private InputStream getInputStreamByConnection(HttpURLConnection httpURLConnection) throws IOException {
        if ("gzip".equals(httpURLConnection.getContentEncoding())) {
            return new GZIPInputStream(httpURLConnection.getInputStream());
        } else {
            return httpURLConnection.getInputStream();
        }
    }

    public ResponseWrapper process() throws IOException{
        return process(true);
    }

    public ResponseWrapper process(boolean loadResponseString) throws IOException{
		processingParam();
		final HttpURLConnection httpURLConnection = commonPreProcess();
		doProcess(httpURLConnection);
		return loadResponseWrapper(httpURLConnection, loadResponseString);
    }
    
    /**
     * 处理responseBody输入流
     * 
     * @param httpURLConnection
     * @param loadResponseString
     * @return
     * @throws IOException
     */
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
    
    /**
     * 处理response获取wrapper
     * 
     * @param httpURLConnection
     * @param loadResponseString
     * @return
     * @throws IOException
     */
    private ResponseWrapper loadResponseWrapper(HttpURLConnection httpURLConnection, boolean loadResponseString)
			throws IOException {
		ResponseWrapper wrapper = new ResponseWrapper();
		try {
			int responseCode = httpURLConnection.getResponseCode();
			wrapper.setResponseCode(responseCode);
			wrapper.setResponseHeader(httpURLConnection.getHeaderFields());

			if (is2XX(responseCode) || isRedirect(responseCode)) {
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


}
