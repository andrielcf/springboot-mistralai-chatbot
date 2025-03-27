package com.nutrisync.nutrisync.dto;

import lombok.Data;

@Data
public class UserRequest {
    private int age;
    private double height;
    private double weight;
    private String goal;
    private String restrictions;
    private String prompdousuario;
}
