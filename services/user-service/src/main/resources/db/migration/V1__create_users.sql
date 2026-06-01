CREATE TABLE users
(
    id        UUID PRIMARY KEY,
    nome      VARCHAR(150)             NOT NULL,
    email     VARCHAR(255)             NOT NULL UNIQUE,
    senha     VARCHAR(255)             NOT NULL,
    cpf       VARCHAR(11)              NOT NULL UNIQUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_cpf ON users (cpf);
