package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.demo.model.People;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApiController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/restPost", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json")
    public String restPost(@RequestBody(required = false) People people) {
        log.debug("REST POST:" + JSON.toJSONString(people));
        if(people == null){
            people = new People();
        }
        return JSON.toJSONString(people);
    }
}
