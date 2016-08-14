package com.jtool.apiclient.demo.model;

import java.util.List;

public class ResponsePeople {
    private String name;
    private Integer age;
    private Double height;
    private String avatar;
    private String gallery;
    private String article;
    private List<String> imgs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGallery() {
        return gallery;
    }

    public void setGallery(String gallery) {
        this.gallery = gallery;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    @Override
    public String toString() {
        return "ResponsePeople{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", avatar='" + avatar + '\'' +
                ", gallery='" + gallery + '\'' +
                ", article='" + article + '\'' +
                ", imgs=" + imgs +
                '}';
    }
}
