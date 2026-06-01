-- Cria um database isolado para cada microsserviço.
-- O usuário 'rabbitride' (definido nas envs do container) é dono automático.
-- Isso roda APENAS na primeira inicialização (volume vazio).
-- Para forçar re-execução: docker compose down -v && docker compose up -d

CREATE DATABASE user_db;
CREATE DATABASE car_db;
CREATE DATABASE rental_db;
CREATE DATABASE analysis_db;
CREATE DATABASE notification_db;
