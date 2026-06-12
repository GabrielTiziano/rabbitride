CREATE TABLE blacklist
(
    cpf       VARCHAR(11) PRIMARY KEY,
    motivo    VARCHAR(500)             NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT
ON TABLE blacklist IS 'CPFs bloqueados — análise retorna REJECTED';
COMMENT
ON COLUMN blacklist.cpf IS 'CPF normalizado (11 dígitos, sem pontuação)';
COMMENT
ON COLUMN blacklist.motivo IS 'Razão do bloqueio (exibida no e-mail de rejeição)';

--seed
INSERT INTO blacklist (cpf, motivo)
VALUES ('15350946056', 'Cliente com dívida em aberto há mais de 30 dias'),
       ('11144477735', 'Suspeita de tentativa de fraude registrada em 2025-10-15'),
       ('47393545820', 'Cliente bloqueado por uso indevido de veículo anterior');
