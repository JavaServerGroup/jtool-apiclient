package com.jtool.apiclient.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static com.jtool.apiclient.processor.MultipartPostProcessor.CHARSET;
import static com.jtool.apiclient.processor.MultipartPostProcessor.LINE_END;

/**
 * Created by jialechan on 2017/2/22.
 */
public class MultipartFileItem extends MultipartItem {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private File file;

    @Override
    public String genContentDispositionStr() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(file.getName()).append("\"").append(LINE_END);
        return stringBuilder.toString();
    }

    @Override
    public String genContentType() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Content-Type: ").append(getContentTypeByFilename(file.getName())).append("; charset=").append(CHARSET).append(LINE_END);
        return stringBuilder.toString();
    }

    @Override
    public void genBody(OutputStream outputStream) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            log.error("文件不存在", e);
        } catch (IOException e) {
            log.error("写入multipart的body遇到错误", e);
        }
    }

    public void setFile(File file) {
        this.file = file;
    }

    private String getContentTypeByFilename(String filename) {

        int index = filename.lastIndexOf('.');

        if (index == -1) {
            return "application/octet-stream";
        }

        String suffix = filename.substring(index);

        if (".gif".equalsIgnoreCase(suffix)) {
            return "image/gif";
        } else if (".jpg".equalsIgnoreCase(suffix) || ".jpeg".equalsIgnoreCase(suffix)) {
            return "image/jpeg";
        } else if (".png".equalsIgnoreCase(suffix)) {
            return "image/png";
        } else {
            return "application/octet-stream";
        }
    }
}
