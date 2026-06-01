package com.gghiaroni.rabbitride.userservice.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email){
        super("E-mail " + email + " já cadastrado.");
    }
}
