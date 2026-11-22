# Bikes API

API REST para gerenciamento de uma locadora de bicicletas por tempo temporário, desenvolvida com Spring Boot e autenticação via **JWT**. O projeto é somente back-end: a documentação Swagger existe para apoiar a equipe de front-end.

## Visão geral

Recursos disponíveis:

- **Autenticação**: login e emissão de token JWT.
- **Usuários**: cadastro público (sempre perfil `CLIENTE`), consulta por id, listagem (ADMIN) e alteração de senha.
- **Clientes**: cadastro vinculado ao usuário, consulta do próprio dados, consulta por id e listagem (ADMIN).
- **Bicicletas**: cadastro, consulta por id/código, listagem, atualização e remoção (ADMIN).
- **Locações**: solicitação de aluguel, devolução com cálculo do valor a debitar, histórico do cliente e relatório (ADMIN).

## Tecnologias

- Java 25
- Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation)
- H2 (padrão, em memória) e MySQL (perfil alternativo)
- JWT (jjwt 0.12.6)
- Lombok + MapStruct
- springdoc-openapi 3.1.1 (Swagger UI)
- Maven Wrapper

## Pré-requisitos

- JDK 25
- Maven (opcional: o projeto inclui `./mvnw`)

Para o perfil `mysql`, é necessário um servidor MySQL local.

## Como executar

```bash
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`

A aplicação sobe em `http://localhost:8080`.

### ADMIN inicial

O cadastro público (`POST /api/v1/usuarios`) **sempre cria usuários com perfil `CLIENTE`**, mesmo que o campo `role` seja enviado no JSON. Isso impede que qualquer anônimo se cadastre como administrador e anule as regras "somente ADMIN" dos demais recursos.

O administrador é criado na inicialização a partir de:

```properties
app.admin.username=admin@bikes.com
app.admin.password=123456
```

Em produção, altere esses valores. A senha é armazenada com BCrypt.

## Perfis de execução

O perfil padrão é `h2`, definido em `src/main/resources/application.properties`.

### Perfil `h2` (padrão)

- URL: `jdbc:h2:mem:bikes`
- Schema recriado a cada inicialização (`create-drop`)
- Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) — JDBC URL `jdbc:h2:mem:bikes`, usuário `sa`, senha vazia

### Perfil `mysql`

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Configuração padrão: `jdbc:mysql://localhost:3306/bikes`, usuário/senha `root`, DDL `update`.

## Documentação da API

- **Swagger UI:** [http://localhost:8080/docs-bikes.html](http://localhost:8080/docs-bikes.html)
- **OpenAPI JSON:** [http://localhost:8080/docs-bikes](http://localhost:8080/docs-bikes)

No Swagger, use o botão **Authorize** e informe o token no formato `Bearer <token>`.

## Autenticação

```http
POST /api/v1/auth/login
{
  "username": "admin@bikes.com",
  "password": "123456"
}
```

Resposta:

```json
{
  "token": "eyJ...",
  "type": "Bearer",
  "usuarioId": 1,
  "username": "admin@bikes.com",
  "role": "ADMIN"
}
```

Envie o token nas requisições protegidas como `Authorization: Bearer eyJ...`.

## Permissões

| Recurso | Permissão |
|---|---|
| `POST /api/v1/usuarios`, `POST /api/v1/auth/login` | público |
| `GET /api/v1/usuarios/{id}`, `PATCH /api/v1/usuarios/{id}` | autenticado |
| `GET /api/v1/usuarios` | ADMIN |
| `POST /api/v1/clientes`, `GET /api/v1/clientes/me` | CLIENTE |
| `GET /api/v1/clientes/{id}`, `GET /api/v1/clientes` | ADMIN |
| Todos os endpoints de bicicleta | ADMIN |
| `POST /api/v1/locacoes`, `POST /api/v1/locacoes/{id}/devolucao`, `GET /api/v1/locacoes/minhas` | CLIENTE |
| `GET /api/v1/locacoes/relatorio` | ADMIN |

## Endpoints

Base URL: `/api/v1`

### Usuários

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/usuarios` | Cria usuário `CLIENTE` | `201` | `409` e-mail duplicado, `422` dados inválidos |
| `GET` | `/usuarios/{id}` | Busca por id | `200` | `401`, `404` |
| `GET` | `/usuarios` | Lista todos (ADMIN) | `200` | `401`, `403` |
| `PATCH` | `/usuarios/{id}` | Altera a senha | `204` | `400` senha atual/confirmação, `404`, `422` |

```json
// POST /api/v1/usuarios
{ "username": "cliente@email.com", "password": "123456" }

// PATCH /api/v1/usuarios/{id}
{ "senhaAtual": "123456", "novaSenha": "nova01", "confirmaSenha": "nova01" }
```

- `username`: e-mail válido e único.
- `password`: **exatamente 6 caracteres** (tanto no cadastro quanto na alteração).

### Clientes

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/clientes` | Cadastra cliente (CLIENTE autenticado) | `201` | `409` CPF duplicado/usuário já cadastrado, `422` |
| `GET` | `/clientes/me` | Dados do cliente autenticado | `200` | `404` |
| `GET` | `/clientes/{id}` | Busca por id (ADMIN) | `200` | `404` |
| `GET` | `/clientes` | Lista todos (ADMIN) | `200` | `403` |

```json
// POST /api/v1/clientes
{ "nomeCompleto": "Maria Silva", "cpf": "12345678909" }
```

- O cliente é vinculado automaticamente ao usuário autenticado (relacionamento 1:1).
- CPF: exatamente 11 dígitos e único.

### Bicicletas (somente ADMIN)

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/bicicletas` | Cadastra bicicleta | `201` | `409` código duplicado, `422` |
| `GET` | `/bicicletas/{id}` | Busca por id | `200` | `404` |
| `GET` | `/bicicletas/codigo/{codigo}` | Busca pelo código | `200` | `404` |
| `GET` | `/bicicletas` | Lista todas | `200` | - |
| `PUT` | `/bicicletas/{id}` | Atualiza | `200` | `404`, `422` |
| `DELETE` | `/bicicletas/{id}` | Remove | `204` | `404` |

```json
// POST /api/v1/bicicletas
{
  "codigo": "BIKE-001",
  "modelo": "Caloi Elite",
  "marca": "Caloi",
  "cor": "Preta",
  "anoFabricacao": 2024,
  "status": "LIVRE"
}
```

- `codigo`: único e diferente do id.
- `status`: `LIVRE`, `OCUPADA` ou `MANUTENCAO` (padrão `LIVRE`).

### Locações e devoluções

| Método | Endpoint | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| `POST` | `/locacoes` | Solicita aluguel (CLIENTE) | `201` | `400` bike indisponível, `422` |
| `POST` | `/locacoes/{id}/devolucao` | Registra devolução (CLIENTE) | `200` | `400` já finalizada, `404` |
| `GET` | `/locacoes/minhas` | Histórico do cliente | `200` | - |
| `GET` | `/locacoes/relatorio` | Relatório completo (ADMIN) | `200` | `403` |

```json
// POST /api/v1/locacoes
{
  "bicicletaCodigo": "BIKE-001",
  "dataPrevistaDevolucao": "2026-11-22T10:00:00",
  "cartaoCredito": "4111111111111111"
}

// POST /api/v1/locacoes/{id}/devolucao  (corpo opcional)
{ "problemaManutencao": false }
```

Regras:

- A bicicleta é bloqueada com **lock pessimista** durante a solicitação: duas locações simultâneas para a mesma bike não são possíveis.
- Ao alugar, o status da bike passa a `OCUPADA`.
- A devolução grava a data/hora efetiva, calcula o valor (`R$ 5,00/hora`, mínimo de 1 minuto) e devolve a bike para `LIVRE`, ou para `MANUTENCAO` quando `problemaManutencao: true`.
- O relatório traz nome do cliente, código da bike, retirada, devolução, duração em minutos e status da locação.
- O valor é **calculado e apresentado**; não há integração com gateway de pagamento (fora do escopo da versão).

## Auditoria

Todas as entidades registram `dataCriacao`, `dataModificacao`, `criadoPor` e `modificadoPor` (`Auditable` + Spring Data Auditing). Requisições autenticadas registram o e-mail do usuário; cadastros públicos registram `system`.

