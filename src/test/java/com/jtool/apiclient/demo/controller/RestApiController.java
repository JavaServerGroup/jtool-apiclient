package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.demo.model.People;
import com.jtool.support.encrypt.AES256Cipher;
import com.jtool.support.encrypt.EncryptPojo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
public class RestApiController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public final static Map<String, EncryptPojo> encryptMap = new HashMap<>();

    @RequestMapping(value = "/restPost", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost(@RequestBody(required = false) People people) {
        log.debug("REST POST:" + JSON.toJSONString(people));
        if(people == null){
            people = new People();
        }
        return JSON.toJSONString(people);
    }

    @RequestMapping(value = "/restPost2", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost2(InputStream inputStream, HttpServletRequest request) throws IOException {

        assert request.getHeader("encryptId") != null;

        String encryptId = request.getHeader("encryptId");

        log.debug("encryptId", encryptId);

        String theString = IOUtils.toString(inputStream, "UTF-8");
        People people = JSON.parseObject(AES256Cipher.decrypt(encryptMap.get(encryptId), theString), People.class);

        return JSON.toJSONString(people);
    }
}
