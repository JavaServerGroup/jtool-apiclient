package com.jtool.apiclient.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static com.jtool.apiclient.processor.MultipartPostProcessor.LINE_END;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by jialechan on 2017/2/22.
 */
public class MultipartTextItem extends MultipartItem {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String value;

    @Override
    public String genContentDispositionStr() {
        return "Content-Disposition: form-data;name=\"" + key + "\"" + LINE_END;
    }

    @Override
    public String genContentType() {
        return "Content-Type: text/plain; charset=" + UTF_8 + LINE_END;
    }

    @Override
    public void genBody(OutputStream out) throws UnsupportedEncodingException {
        try {
            out.write(value.getBytes(UTF_8));
        } catch (IOException e) {
            log.error("写入multipart的body遇到错误", e);
        }
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
