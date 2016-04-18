package com.jtool.apiclient.demo.model;

import java.io.File;

public class People {

    private String name;
    private Integer age;
    private Double height;
    private File avatar;
    private File gallery;
    private File article;

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

    public File getAvatar() {
        return avatar;
    }

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    public File getGallery() {
        return gallery;
    }

    public void setGallery(File gallery) {
        this.gallery = gallery;
    }

    public File getArticle() {
        return article;
    }

    public void setArticle(File article) {
        this.article = article;
    }

    @Override
    public String toString() {
        return "People{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", avatar=" + avatar +
                ", gallery=" + gallery +
                ", article=" + article +
                '}';
    }
}