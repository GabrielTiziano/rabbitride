# rabbitride
Sistema de aluguel de carros event-driven com Spring Boot, microsserviços e RabbitMQ

## Subindo a infraestrutura local

```bash
docker compose -f infra/docker-compose.yml up -d
```

Serviços e portas:

| Serviço   | Porta(s)       | UI / acesso                     |
|-----------|----------------|---------------------------------|
| Postgres  | 5432           | `psql -h localhost -U rabbitride` |
| Redis     | 6379           | `redis-cli`                     |
| RabbitMQ  | 5672, 15672    | http://localhost:15672          |
| MailHog   | 1025, 8025     | http://localhost:8025           |

Credenciais default em dev: `rabbitride` / `rabbitride`.

Para parar: `docker compose -f infra/docker-compose.yml down`
Para limpar tudo (apaga dados): `docker compose -f infra/docker-compose.yml down -v`
