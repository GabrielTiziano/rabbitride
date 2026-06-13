package com.gghiaroni.rabbitride.notificationservice.email;

public class EmailEnvioException extends RuntimeException {

    public EmailEnvioException(String destinatario, Throwable cause) {
        super("Falha ao enviar e-mail para " + destinatario, cause);
    }
}
