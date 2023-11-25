package com.jtool.apiclient.demo.model;

import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class People {
    private String name;
    private Integer age;
    private Double height;
    private List<File> imgs;
    private File avatar;
    private File gallery;
    private File article;
    private List<String> tels;
}