package com.tht.ifdatamigrator.dao.domain;

import lombok.Data;

@Data
public class User {
    private String emailAddress;
    private Integer roleID;
    private Integer userCredentialId;
}
