package org.example.userauthenticationservice_sept2024.models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class Session extends BaseModel {

    SessionState sessionState;
    String token;
//    User user;
}
