package com.jtool.apiclient.demo.model;

import java.io.File;
import java.util.List;

public class People {

    private String name;
    private Integer age;
    private Double height;
    private List<File> imgs;
    private File avatar;
    private File gallery;
    private File article;

    public List<File> getImgs() {
        return imgs;
    }

    public void setImgs(List<File> imgs) {
        this.imgs = imgs;
    }

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
                ", imgs=" + imgs +
                ", avatar=" + avatar +
                ", gallery=" + gallery +
                ", article=" + article +
                '}';
    }
}