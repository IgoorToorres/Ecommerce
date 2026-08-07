# Ecommerce API

API REST de ecommerce desenvolvida em Java e Spring Boot, com foco em arquitetura modular, autenticação com JWT, controle de acesso por perfil, persistência com PostgreSQL e validação automatizada com testes e CI.

O projeto simula fluxos comuns de um backend de mercado, como cadastro de produtos, criação de pedidos, baixa de estoque, autenticação de usuários e proteção de rotas.

## Tecnologias

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT
- BCrypt
- Bucket4j
- Maven
- Docker Compose
- Swagger/OpenAPI
- JUnit 5
- AssertJ
- Mockito
- GitHub Actions

## Arquitetura

O projeto é dividido em módulos Maven para separar responsabilidades:

- `ecommerce-domain`: entidades, enums e regras de negócio.
- `ecommerce-application`: casos de uso, commands, responses, mappers e interfaces.
- `ecommerce-infrastructure`: persistência com JPA, repositories, Flyway e segurança.
- `ecommerce-api`: controllers, requests, tratamento de erros, Swagger e configuração HTTP/security.

Essa estrutura ajuda a manter as regras de negócio separadas de detalhes técnicos como banco, HTTP e autenticação.

## Funcionalidades

- CRUD de produtos.
- Delete lógico de produtos.
- Criação de pedidos.
- Baixa automática de estoque ao criar pedido.
- Listagem de pedidos com paginação e filtros.
- Busca de pedido por ID.
- Cadastro de usuários.
- Login com JWT.
- Proteção de rotas autenticadas.
- Autorização por perfil `ADMIN` e `CUSTOMER`.
- Rate limit por origem da requisição.
- Tratamento padronizado de erros.
- Documentação com Swagger/OpenAPI.
- Pipeline de CI com GitHub Actions.

## Segurança

A API utiliza autenticação JWT. Para acessar rotas protegidas, envie o token no header:

```http
Authorization: Bearer <token>
```

Rotas públicas:

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/products
GET  /api/products/{id}
GET  /docs
GET  /v3/api-docs
```

Rotas protegidas:

```text
/api/orders/**
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

Regras principais:

- `CUSTOMER` pode criar pedidos e consultar apenas os próprios pedidos.
- `ADMIN` pode gerenciar produtos.
- Requisições sem token retornam `401 Unauthorized`.
- Requisições sem permissão retornam `403 Forbidden`.

## Rate Limit

A API possui rate limit em memória por origem da requisição. Por padrão, cada origem pode fazer 60 requisições por minuto.

Quando o limite é excedido, a API retorna:

```http
429 Too Many Requests
```

As rotas de documentação e health check são ignoradas pelo rate limit:

```text
/docs
/swagger-ui/**
/v3/api-docs/**
/actuator/health/**
```

## Tratamento De Erros

As respostas de erro seguem um formato único:

```json
{
  "timestamp": "2026-08-06T16:34:24.556386Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token de autenticação não informado ou inválido.",
  "path": "/api/orders"
}
```

Erros tratados:

- validação de request
- recurso não encontrado
- regra de negócio
- token ausente ou inválido
- acesso negado
- excesso de requisições
- erro inesperado

## Como Rodar

Crie um arquivo `.env` na raiz do projeto com as variáveis:

```env
POSTGRES_DB=ecommerce
POSTGRES_USER=ecommerce
POSTGRES_PASSWORD=change_me
POSTGRES_PORT=5432
API_PORT=8080
JWT_SECRET=change_me_with_at_least_32_characters
JWT_EXPIRATION_SECONDS=3600
RATE_LIMIT_ENABLED=true
RATE_LIMIT_CAPACITY=60
RATE_LIMIT_REFILL_TOKENS=60
RATE_LIMIT_REFILL_DURATION_SECONDS=60
```

Suba banco, instale os módulos e rode a API:

```bash
make dev
```

A API ficará disponível em:

```text
http://localhost:8080
```

Comandos úteis:

```bash
make db-up
make db-down
make docker-up
make docker-down
make test
make build
```

## Rodando Com Docker Compose

Para subir a API e o PostgreSQL juntos em containers:

```bash
make docker-up
```

Ou diretamente:

```bash
docker compose up -d --build
```

Para parar os containers:

```bash
make docker-down
```

Nesse modo, a API é construída pelo `Dockerfile` e usa o serviço `postgres` como host do banco dentro da rede do Docker Compose.

O container da API possui healthcheck apontando para:

```text
GET /actuator/health/readiness
```

Para rodar somente o banco em container e a API localmente via Maven:

```bash
make dev
```

## Documentação Da API

Swagger UI:

```text
http://localhost:8080/docs
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Para testar rotas protegidas no Swagger:

1. Faça login em `POST /api/auth/login`.
2. Copie o valor de `accessToken`.
3. Clique em `Authorize`.
4. Informe `Bearer <accessToken>`.

## Health Check

A aplicação expõe health check público com Spring Boot Actuator:

```text
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

Essas rotas podem ser usadas por Docker, Kubernetes, pipelines ou serviços de monitoramento.

## Como Testar

Rodar todos os testes:

```bash
./mvnw test
```

Validar build completo:

```bash
./mvnw verify
```

Os testes cobrem:

- regras de domínio
- casos de uso da aplicação
- criação e validação de JWT
- autenticação e autorização na API
- acesso público ao Swagger
- acesso público ao health check
- bloqueio por excesso de requisições
- permissões por perfil

## CI/CD

O projeto possui pipeline com GitHub Actions em:

```text
.github/workflows/ci.yml
```

A cada `push` ou `pull_request` para `main` ou `master`, o pipeline executa:

```bash
./mvnw test
./mvnw verify
```

O pipeline configura variáveis de ambiente próprias para CI e sobe um PostgreSQL de teste como serviço, então ele não depende do arquivo `.env` local.

## Estrutura De Pastas

```text
.
├── .github/workflows
├── Dockerfile
├── .dockerignore
├── ecommerce-api
├── ecommerce-application
├── ecommerce-domain
├── ecommerce-infrastructure
├── docker-compose.yml
├── Makefile
└── pom.xml
```

## Próximos Passos

- Fluxo de pagamento de pedido.
- Cancelamento de pedido.
- Avanço de status do pedido: pago, enviado e entregue.
- Testes com Testcontainers.
- Collection Postman/Insomnia.
