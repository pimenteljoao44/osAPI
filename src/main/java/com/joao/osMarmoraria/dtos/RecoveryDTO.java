package com.joao.osMarmoraria.dtos;

import java.io.Serializable;

public class RecoveryDTO implements Serializable {
    private String email;

    private String senhaTemp;

    public RecoveryDTO(String email, String senhaTemp) {
        this.email = email;
        this.senhaTemp = senhaTemp;
    }

    public RecoveryDTO() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaTemp() {
        return senhaTemp;
    }

    public void setSenhaTemp(String senhaTemp) {
        this.senhaTemp = senhaTemp;
    }
}
