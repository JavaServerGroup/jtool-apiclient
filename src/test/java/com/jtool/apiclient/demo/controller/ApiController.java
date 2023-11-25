package com.jtool.apiclient.demo.controller;

import com.alibaba.fastjson2.JSON;
import com.jtool.apiclient.demo.model.People;
import com.jtool.apiclient.demo.model.PeopleRequest;
import com.jtool.apiclient.demo.model.ResponsePeople;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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

        if (peopleRequest.getImgs() != null) {
            List<String> imagesStr = new ArrayList<>();
            for (MultipartFile multipartFile : peopleRequest.getImgs()) {
                imagesStr.add(org.apache.commons.codec.binary.Base64.encodeBase64String(multipartFile.getBytes()));
            }
            responsePeople.setImgs(imagesStr);
        }

        if (peopleRequest.getAvatar() != null) {
            responsePeople.setAvatar(org.apache.commons.codec.binary.Base64.encodeBase64String(peopleRequest.getAvatar().getBytes()));
        }
        if (peopleRequest.getGallery() != null) {
            responsePeople.setGallery(org.apache.commons.codec.binary.Base64.encodeBase64String(peopleRequest.getGallery().getBytes()));
        }
        if (peopleRequest.getArticle() != null) {
            responsePeople.setArticle(org.apache.commons.codec.binary.Base64.encodeBase64String(peopleRequest.getArticle().getBytes()));
        }

        responsePeople.setTels(peopleRequest.getTels());

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
        return JSON.toJSONString(header);
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

    @ResponseBody
    @RequestMapping(value = "/getGzip", method = RequestMethod.GET)
    public void getGzip(HttpServletResponse response) throws IOException {

        response.setHeader("Content-Encoding", "gzip");

        String str = "get Gzip controller";

        response.getOutputStream().write(zip(str));
    }

    byte[] zip(final String str) {
        if ((str == null) || (str.length() == 0)) {
            throw new IllegalArgumentException("Cannot zip null or empty string");
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to zip content", e);
        }
    }

    @RequestMapping(value = "/redirect", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String redirect() {
        return "redirect:http://www.baidu.com";
    }

}
