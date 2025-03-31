package com.nutrisync.nutrisync.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequest {

    private String goal;
    
    private String restrictions;
    
    private int age;
    
    private double height;
    
    private double weight;
    
    private String promptUsuario;
}

