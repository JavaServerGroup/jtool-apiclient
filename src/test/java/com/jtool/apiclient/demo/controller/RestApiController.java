package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson2.JSON;
import com.jtool.apiclient.demo.model.People;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class RestApiController {

    @RequestMapping(value = "/restPost", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost(@RequestBody(required = false) People people) {
        log.debug("REST POST:" + JSON.toJSONString(people));
        if (people == null) {
            people = new People();
        }
        return JSON.toJSONString(people);
    }

//    @RequestMapping(value = "/restPost2", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
//    public String restPost2(InputStream inputStream, HttpServletRequest request) throws IOException {
//
//        assert request.getHeader("encryptId") != null;
//
//        String encryptId = request.getHeader("encryptId");
//
//        log.debug("encryptId", encryptId);
//
//        String theString = IOUtils.toString(inputStream, "UTF-8");
//        People people = JSON.parseObject(AES256Cipher.decrypt(encryptMap.get(encryptId), theString), People.class);
//
//        return JSON.toJSONString(people);
//    }
}
