package com.gghiaroni.rabbitride.userservice.user.exception;

public class CpfAlreadyExistsException extends RuntimeException {
    public CpfAlreadyExistsException(String cpf){
        super("CPF " + cpf + " já cadastrado.");
    }
}
