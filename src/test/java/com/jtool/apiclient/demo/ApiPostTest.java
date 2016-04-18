package com.jtool.apiclient.demo;

import com.alibaba.fastjson.JSON;
import com.jtool.apiclient.demo.model.People;
import com.jtool.apiclient.demo.model.ResponsePeople;
import com.jtool.apiclient.exception.StatusCodeNot200Exception;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static com.jtool.apiclient.ApiClient.Api;

public class ApiPostTest {

    private static Logger logger = LoggerFactory.getLogger(ApiPostTest.class);

    @Test
    public void postWithNoParamTest() throws Exception {
        //发送没参数的post请求
        Assert.assertEquals("{}", Api().post("http://chat.palm-chat.cn/TestServer/sentPost"));
    }

    @Test
    public void postWithUrlParamTest() throws Exception {
        ResponsePeople responsePeople = JSON.parseObject(Api().post("http://chat.palm-chat.cn/TestServer/sentPost?name=中文名"), ResponsePeople.class);
        Assert.assertEquals("中文名", responsePeople.getName());
        Assert.assertNull(responsePeople.getAge());
        Assert.assertNull(responsePeople.getGallery());
        Assert.assertNull(responsePeople.getHeight());
        Assert.assertNull(responsePeople.getArticle());
        Assert.assertNull(responsePeople.getAvatar());
    }

    @Test
    public void postWithUrlAndBeanParamTest() throws Exception {
        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost?name=中文名"), ResponsePeople.class);
        Assert.assertEquals("中文名", responsePeople.getName());
        Assert.assertEquals(new Integer(30), responsePeople.getAge());
        Assert.assertEquals(new Double(1.73), responsePeople.getHeight());
        Assert.assertNull(responsePeople.getGallery());
        Assert.assertNull(responsePeople.getArticle());
        Assert.assertNull(responsePeople.getAvatar());
    }

    @Test
    public void postWithUrlAndBeanParamTestWithURLEncode() throws Exception {
        People people = new People();
        people.setName("1+1");

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost"), ResponsePeople.class);
        Assert.assertEquals("1+1", responsePeople.getName());
        Assert.assertNull(responsePeople.getAge());
        Assert.assertNull(responsePeople.getGallery());
        Assert.assertNull(responsePeople.getHeight());
        Assert.assertNull(responsePeople.getArticle());
        Assert.assertNull(responsePeople.getAvatar());
    }

    @Test
    public void postWithUrlAndBeanWithFileParamTest() throws Exception {

        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File(ApiPostTest.class.getResource("/media/g.gif").getFile()));

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost?name=中文名"), ResponsePeople.class);
        Assert.assertEquals("中文名", responsePeople.getName());
        Assert.assertEquals(new Integer(30), responsePeople.getAge());
        Assert.assertEquals(new Double(1.73), responsePeople.getHeight());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        Assert.assertNull(responsePeople.getGallery());
        Assert.assertNull(responsePeople.getArticle());
    }

    @Test
    public void mypostWithUrlAndBeanWithFileParamTest() throws Exception {

        People people = new People();
//        people.setAge(30);
//        people.setHeight(1.73);
        people.setAvatar(new File(ApiPostTest.class.getResource("/media/g.gif").getFile()));

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost"), ResponsePeople.class);
//        Assert.assertEquals("中文名", responsePeople.getName());
//        Assert.assertEquals(new Integer(30), responsePeople.getAge());
//        Assert.assertEquals(new Double(1.73), responsePeople.getHeight());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        Assert.assertNull(responsePeople.getGallery());
        Assert.assertNull(responsePeople.getArticle());
    }

    @Test
    public void postWithUrlAndBeanWithTwoFileParamTest() throws Exception {

        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File(ApiPostTest.class.getResource("/media/g.gif").getFile()));
        people.setGallery(new File(ApiPostTest.class.getResource("/media/j.jpg").getFile()));

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost?name=中文名"), ResponsePeople.class);
        Assert.assertEquals("中文名", responsePeople.getName());
        Assert.assertEquals(new Integer(30), responsePeople.getAge());
        Assert.assertEquals(new Double(1.73), responsePeople.getHeight());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getGallery())), responsePeople.getGallery());
        Assert.assertNull(responsePeople.getArticle());
    }

    @Test
    public void postWithUrlAndBeanWithThreeFileParamTest() throws Exception {

        People people = new People();
        people.setName("中文名");
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File(ApiPostTest.class.getResource("/media/g.gif").getFile()));
        people.setGallery(new File(ApiPostTest.class.getResource("/media/j.jpg").getFile()));
        people.setArticle(new File(ApiPostTest.class.getResource("/media/myarticle.txt").getFile()));

        ResponsePeople responsePeople = JSON.parseObject(Api().param(people).post("http://chat.palm-chat.cn/TestServer/sentPost"), ResponsePeople.class);
        Assert.assertEquals("中文名", responsePeople.getName());
        Assert.assertEquals(new Integer(30), responsePeople.getAge());
        Assert.assertEquals(new Double(1.73), responsePeople.getHeight());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getGallery())), responsePeople.getGallery());
        Assert.assertEquals(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(people.getArticle())), responsePeople.getArticle());
    }

    @Test
    public void get404() throws Exception {
        try {
            Api().needLog(false).post("http://chat.palm-chat.cn/TestServer/404");
        } catch (StatusCodeNot200Exception e) {
            logger.debug(e.toString());
            Assert.assertEquals(404, e.getStatusCode());
            Assert.assertEquals("http://chat.palm-chat.cn/TestServer/404", e.getUrl());
            Assert.assertTrue(e.getParams() == null || e.getParams().size() == 0);
        }
    }

    @Test(expected= IOException.class)
    public void postIoExceptionWithFile() throws Exception {

        People people = new People();
        people.setName("中文名");
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File(ApiPostTest.class.getResource("/media/g.gif").getFile()));

        Api().param(people).post("http://xxx.abc");
    }

    @Test(expected= IOException.class)
    public void postIoException() throws Exception {
        Api().post("http://xxx.abc");
    }
}
