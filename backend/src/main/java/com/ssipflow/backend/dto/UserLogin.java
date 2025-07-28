package com.ssipflow.backend.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserLogin {
    private String username;
    private String password;
}
