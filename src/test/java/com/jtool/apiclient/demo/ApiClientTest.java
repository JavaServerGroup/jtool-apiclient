package com.jtool.apiclient.demo;

import com.jtool.apiclient.demo.model.People;
import com.jtool.apiclient.demo.model.ResponsePeople;
import com.jtool.apiclient.model.ResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jtool.apiclient.ApiClient.Api;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Slf4j
@SpringBootApplication
public class ApiClientTest {

    private final String host = "http://localhost:8082";

    @BeforeClass
    public static void setup() {
        String[] args = new String[0];
        SpringApplication.run(ApiClientTest.class, args);
    }

    @Test
    public void getTest() throws Exception {
        assertEquals("{}", Api().get(host + "/sentGet"));

        ResponsePeople responsePeople = Api().get(host + "/sentGet?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void getURLEncoderTest() throws Exception {
        People people = new People();
        people.setName("1+1");

        ResponsePeople responsePeople = Api().param(people).get(host + "/sentGet", ResponsePeople.class);
        assertEquals("1+1", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void getTest2() throws Exception {
        People people = new People();
        people.setName("中文名");
        people.setAge(30);
        people.setHeight(1.73);

        ResponsePeople responsePeople = Api().param(people).get(host + "/sentGet", ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void getTest3() throws Exception {
        ResponsePeople responsePeople = Api().get(host + "/sentGet?name=" + URLEncoder.encode("中文名", "UTF-8") + "&age=22&height=1.66", ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(22), responsePeople.getAge());
        assertEquals(new Double(1.66), responsePeople.getHeight());
    }

    @Test
    public void getTest4() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "map中文名");
        params.put("age", 31);
        params.put("height", 1.74);

        ResponsePeople responsePeople = Api().param(params).get(host + "/sentGet", ResponsePeople.class);
        assertEquals("map中文名", responsePeople.getName());
        assertEquals(new Integer(31), responsePeople.getAge());
        assertEquals(new Double(1.74), responsePeople.getHeight());
    }

    @Test(expected = IOException.class)
    public void getIoException() throws Exception {
        Api().get("http://www");
    }

    @Test
    public void postWithNoParamTest() throws Exception {
        //发送没参数的post请求
        assertEquals("{}", Api().post(host + "/sentPost"));
        assertEquals("{}", Api().restPost(host + "/restPost"));
    }

    @Test
    public void postWithUrlParamTest() throws Exception {
        ResponsePeople responsePeople = Api().post(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void restPostWithUrlParamTest() throws Exception {
        ResponsePeople responsePeople = Api().restPost(host + "/restPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void postWithUrlAndBeanParamTest() throws Exception {
        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);

        List<String> tels = new ArrayList<>();
        tels.add("13800138000");
        tels.add("13450363825");

        people.setTels(tels);

        ResponsePeople responsePeople = Api().param(people).post(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
        assertEquals(tels, responsePeople.getTels());
    }

    @Test
    public void restPostWithUrlAndBeanParamTest() throws Exception {
        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);

        ResponsePeople responsePeople = Api().param(people).restPost(host + "/restPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void postWithUrlAndBeanParamTestWithURLEncode() throws Exception {
        People people = new People();
        people.setName("1+1");

        ResponsePeople responsePeople = Api().param(people).post(host + "/sentPost", ResponsePeople.class);
        assertEquals("1+1", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test
    public void restPostWithUrlAndBeanParamTestWithURLEncode() throws Exception {
        People people = new People();
        people.setName("中文名");

        ResponsePeople responsePeople = Api().param(people).restPost(host + "/restPost", ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test(expected = RuntimeException.class)
    public void restPostWithUrlAndBeanWithFileParamTest() throws Exception {
        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File("src/test/resources/media/g.gif"));
        Api().param(people).restPost(host + "/restPost?name=中文名", ResponsePeople.class);
    }

    @Test
    public void postWithUrlAndBeanWithFileParamTest() throws Exception {

        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File("src/test/resources/media/g.gif"));

        ResponsePeople responsePeople = Api().param(people).setReadTimeout(10000).filePost(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
    }

    @Test
    public void postWithMutiImages() throws Exception {

        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);

        List<File> images = new ArrayList<>();
        images.add(new File("src/test/resources/media/g.gif"));
        images.add(new File("src/test/resources/media/j.jpg"));
        people.setImgs(images);

        people.setAvatar(new File("src/test/resources/media/g.gif"));

        ResponsePeople responsePeople = Api().param(people).filePost(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());

        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getImgs().get(0))), responsePeople.getImgs().get(0));
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getImgs().get(1))), responsePeople.getImgs().get(1));
    }

    @Test
    public void postWithMutiImagesByMap() throws Exception {

        Map<String, Object> params = new HashMap<>();

        List<File> images = new ArrayList<>();
        File file1 = new File("src/test/resources/media/g.gif");
        File file2 = new File("src/test/resources/media/j.jpg");
        images.add(file1);
        images.add(file2);

        params.put("imgs", images);

        ResponsePeople responsePeople = Api().param(params).filePost(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());

        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(file1)), responsePeople.getImgs().get(0));
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(file2)), responsePeople.getImgs().get(1));
    }

    @Test
    public void mypostWithUrlAndBeanWithFileParamTest() throws Exception {

        People people = new People();
        people.setAvatar(new File("src/test/resources/media/g.gif"));

        ResponsePeople responsePeople = Api().param(people).filePost(host + "/sentPost", ResponsePeople.class);
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
    }

    @Test
    public void postWithUrlAndBeanWithTwoFileParamTest() throws Exception {

        People people = new People();
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File("src/test/resources/media/g.gif"));
        people.setGallery(new File("src/test/resources/media/j.jpg"));

        ResponsePeople responsePeople = Api().param(people).filePost(host + "/sentPost?name=" + URLEncoder.encode("中文名", "UTF-8"), ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getGallery())), responsePeople.getGallery());
        assertNull(responsePeople.getArticle());
    }

    @Test
    public void postWithUrlAndBeanWithThreeFileParamTest() throws Exception {

        People people = new People();
        people.setName("中文名");
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File("src/test/resources/media/g.gif"));
        people.setGallery(new File("src/test/resources/media/j.jpg"));
        people.setArticle(new File("src/test/resources/media/myarticle.txt"));

        ResponsePeople responsePeople = Api().param(people).filePost(host + "/sentPost", ResponsePeople.class);
        assertEquals("中文名", responsePeople.getName());
        assertEquals(new Integer(30), responsePeople.getAge());
        assertEquals(new Double(1.73), responsePeople.getHeight());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getAvatar())), responsePeople.getAvatar());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getGallery())), responsePeople.getGallery());
        assertEquals(Base64.encodeBase64String(FileUtils.readFileToByteArray(people.getArticle())), responsePeople.getArticle());
    }

//    @Test
//    public void get404() throws Exception {
//        String url404 = "http://www.example.org/404";
//        try {
//            Api().post(url404);
//        } catch (Exception e) {
//            Assert.assertEquals(404, e.getStatusCode());
//            Assert.assertEquals(url404, e.getUrl());
//        }
//    }

    @Test(expected = IOException.class)
    public void postIoExceptionWithFile() throws Exception {

        People people = new People();
        people.setName("中文名");
        people.setAge(30);
        people.setHeight(1.73);
        people.setAvatar(new File("src/test/resources/media/g.gif"));

        Api().param(people).post("http://www");
    }

    @Test(expected = IOException.class)
    public void postIoException() throws Exception {
        Api().post("http://www");
    }

    @Test
    public void testHeader() throws IOException {
        Map<String, String> header = new HashMap<>();
        header.put("myHeader1", "myHeader1");
        header.put("myHeader2", "myHeader2");
        assertEquals(header, Api().header(header).get(host + "/sentGetWithHeader", HashMap.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCallParamTwice() {
        Api().param(new Object()).param(new Object());
    }

    @Test
    public void testRedirect() throws IOException {
        assertEquals("ok", Api().get(host + "/forTestRedirect"));
    }

    @Test
    public void testHead() throws IOException {
        Map<String, List<String>> responseHeader = Api().head(host + "/forTestRedirect");
        assertEquals("2", responseHeader.get("Content-Length").get(0));
    }

    @Test
    public void testZip() throws IOException {
        assertEquals("get Gzip controller", Api().get(host + "/getGzip"));
    }

    @Test
    public void testResponseWrapper() throws Exception {
        People people = new People();
        people.setName("1+1");
        ResponsePeople responsePeople = Api().param(people).getResponseWrapper(host + "/sentGet").getResponseBody(ResponsePeople.class);
        assertEquals("1+1", responsePeople.getName());
        assertNull(responsePeople.getAge());
        assertNull(responsePeople.getGallery());
        assertNull(responsePeople.getHeight());
        assertNull(responsePeople.getArticle());
        assertNull(responsePeople.getAvatar());
    }

    @Test(expected = IOException.class)
    public void getResponseWrapperException() throws Exception {
        Api().getResponseWrapper("http://www");
    }

//    @Test(expected = StatusCodeNot200Exception.class)
//    public void getResponseWrapperGet404() throws Exception {
//        Api().get(host + "/404");
//    }

    @Test()
    public void redirect() throws Exception {
        ResponseWrapper responseWrapper = Api().getResponseWrapper(host + "/redirect");
        assertEquals(200, responseWrapper.getResponseCode());

        responseWrapper = Api().setFollowRedirects(false).getResponseWrapper(host + "/redirect");
        assertEquals(302, responseWrapper.getResponseCode());
        assertEquals("http://www.baidu.com", responseWrapper.getResponseHeader().get("Location").get(0));
    }

}
