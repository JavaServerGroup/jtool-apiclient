package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.aes.AES256Cipher;
import com.jtool.apiclient.demo.model.People;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class RestApiController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public final static String key = AES256Cipher.genKey();
    public final static String iv = AES256Cipher.genIv();

    @RequestMapping(value = "/restPost", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost(@RequestBody(required = false) People people) {
        log.debug("REST POST:" + JSON.toJSONString(people));
        if(people == null){
            people = new People();
        }
        return JSON.toJSONString(people);
    }

    @RequestMapping(value = "/restPost2", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost2(InputStream inputStream) throws IOException {
        String theString = IOUtils.toString(inputStream, "UTF-8");
        People people = JSON.parseObject(AES256Cipher.decrypt(iv, key, theString), People.class);

        return JSON.toJSONString(people);
    }
}
