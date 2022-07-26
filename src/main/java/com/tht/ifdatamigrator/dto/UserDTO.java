package com.tht.ifdatamigrator.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer id;
    private String email;
    private String password;
    private String previousPassword;
    private String role;
}
