package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.demo.model.People;
import com.jtool.apiclient.demo.model.PeopleRequest;
import com.jtool.apiclient.demo.model.ResponsePeople;
import com.jtool.support.log.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

@Controller
public class ApiController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/sentGet", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String sentGet(People people) {
        log.debug(people.toString());
        return JSON.toJSONString(people);
    }

    @ResponseBody
    @RequestMapping(value = "/sentPost", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String sentPost(PeopleRequest peopleRequest) throws IOException {

        log.debug(peopleRequest.toString());

        ResponsePeople responsePeople = new ResponsePeople();
        responsePeople.setName(peopleRequest.getName());
        responsePeople.setAge(peopleRequest.getAge());
        responsePeople.setHeight(peopleRequest.getHeight());

        if(peopleRequest.getImgs() != null) {
            List<String> imagesStr = new ArrayList<String>();
            for(MultipartFile multipartFile : peopleRequest.getImgs()) {
                imagesStr.add(Base64.getEncoder().encodeToString(multipartFile.getBytes()));
            }
            responsePeople.setImgs(imagesStr);
        }

        if(peopleRequest.getAvatar() != null) {
            responsePeople.setAvatar(Base64.getEncoder().encodeToString(peopleRequest.getAvatar().getBytes()));
        }
        if(peopleRequest.getGallery() != null) {
            responsePeople.setGallery(Base64.getEncoder().encodeToString(peopleRequest.getGallery().getBytes()));
        }
        if(peopleRequest.getArticle() != null) {
            responsePeople.setArticle(Base64.getEncoder().encodeToString(peopleRequest.getArticle().getBytes()));
        }

        return JSON.toJSONString(responsePeople);
    }

    @ResponseBody
    @RequestMapping(value = "/sentGet2", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String sentGet2(String[] name) {
        return JSON.toJSONString(name);
    }

    @ResponseBody
    @RequestMapping(value = "/sentGetWithHeader", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String sentGetWithHeader(HttpServletRequest request) {
        Map<String, String> header = new HashMap<String, String>();
        header.put("myHeader1", request.getHeader("myHeader1"));
        header.put("myHeader2", request.getHeader("myHeader2"));
        header.put(LogHelper.JTOOL_LOG_ID, request.getHeader(LogHelper.JTOOL_LOG_ID));
        return JSON.toJSONString(header);
    }

    @ResponseBody
    @RequestMapping(value = "/forTestNeedLog", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String forTestNeedLog(HttpServletRequest request) {
        String _logId = request.getParameter(LogHelper.JTOOL_LOG_ID);
        if(null != _logId && !"".equals(_logId)){
            return "true";
        } else {
            return "false";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/forTestSetLogId", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String forTestSetLogId(HttpServletRequest request) {
        return request.getHeader(LogHelper.JTOOL_LOG_ID);
    }

    @RequestMapping(value = "/forTestRedirect", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String forTestRedirect(HttpServletRequest request) {
        return "redirect:forTestRedirect2";
    }

    @ResponseBody
    @RequestMapping(value = "/forTestRedirect2", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String forTestRedirect2(People people) {
        return "ok";
    }

}
