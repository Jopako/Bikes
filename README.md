# Bikes API

API REST para gerenciamento de usuários e bicicletas, desenvolvida com Spring Boot.

## Visão geral

O projeto expõe endpoints para:
- **Usuários**: cadastro, consulta por ID, listagem e alteração de senha.
- **Bicicletas**: cadastro, consulta por ID, listagem, atualização e remoção.

A documentação interativa da API está disponível via Swagger/OpenAPI quando a aplicação está em execução.

## Tecnologias

- **Java 25**
- **Spring Boot 4.1.1**
- **Spring Web MVC**Q
- **Spring Data JPA**
- **Bean Validation (Jakarta Validation)**
- **H2 Database** (perfil padrão, em memória)
- **MySQL** (perfil alternativo)
- **Lombok**
- **MapStruct**
- **springdoc-openapi (Swagger UI)**
- **Maven**

## Pré-requisitos

- **JDK 25** (conforme `pom.xml`)
- **Maven** (opcional, o projeto inclui Maven Wrapper: `./mvnw`)

Para usar o perfil `mysql`, é necessário ter um servidor MySQL em execução com o banco configurado.

## Como executar

### Com Maven Wrapper (recomendado)

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A aplicação inicia por padrão em: `http://localhost:8080`

### Gerar o pacote

```bash
./mvnw clean package
```

O artefato será gerado em `target/`.

## Perfis de execução

O perfil ativo padrão é `h2` (definido em `src/main/resources/application.properties`).

### Perfil `h2` (padrão)

- Banco em memória: `jdbc:h2:mem:bikes`
- Schema recriado a cada inicialização (`spring.jpa.hibernate.ddl-auto=create-drop`)
- Console H2 habilitado

**Acesso ao H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:bikes` |
| User Name | `sa` |
| Password | *(vazio)* |

> Os dados existem apenas enquanto a aplicação estiver em execução.

### Perfil `mysql`

Configuração padrão (ajuste conforme seu ambiente):

- URL: `jdbc:mysql://localhost:3306/bikes`
- Usuário: `root`
- Senha: `root`
- DDL: `update`

Para executar com MySQL:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Ou definindo `spring.profiles.active=mysql`.

## Documentação da API (Swagger)

Com a aplicação em execução:

- **Swagger UI:** [http://localhost:8080/docs-bikes.html](http://localhost:8080/docs-bikes.html)
- **OpenAPI JSON:** [http://localhost:8080/docs-bikes](http://localhost:8080/docs-bikes)

## Endpoints

Base URL: `/api/v1`

### Usuários (`/api/v1/usuarios`)

| Método | Endpoint | Descrição | Sucesso | Principais erros |
|---|---|---|---|---|
| `POST` | `/api/v1/usuarios` | Cria um novo usuário | `201 Created` | `409` username já cadastrado, `422` dados inválidos |
| `GET` | `/api/v1/usuarios/{id}` | Busca usuário por ID | `200 OK` | `404` não encontrado |
| `GET` | `/api/v1/usuarios` | Lista todos os usuários | `200 OK` | - |
| `PATCH` | `/api/v1/usuarios/{id}` | Atualiza a senha do usuário | `204 No Content` | `400` senha atual/confirmação inválida, `404` não encontrado, `422` dados inválidos |

#### Exemplo: criar usuário

```json
POST /api/v1/usuarios
{
  "username": "usuario@email.com",
  "password": "123456"
}
```

- `username`: obrigatório, deve ser um e-mail válido e único.
- `password`: obrigatória, entre 6 e 20 caracteres.

#### Exemplo: alterar senha

```json
PATCH /api/v1/usuarios/{id}
{
  "senhaAtual": "123456",
  "novaSenha": "novaSenha123",
  "confirmaSenha": "novaSenha123"
}
```

### Bicicletas (`/api/v1/bicicletas`)

| Método | Endpoint | Descrição | Sucesso | Principais erros |
|---|---|---|---|---|
| `POST` | `/api/v1/bicicletas` | Cria uma nova bicicleta | `201 Created` | `422` dados inválidos |
| `GET` | `/api/v1/bicicletas/{id}` | Busca bicicleta por ID | `200 OK` | `404` não encontrada |
| `GET` | `/api/v1/bicicletas` | Lista todas as bicicletas | `200 OK` | - |
| `PUT` | `/api/v1/bicicletas/{id}` | Atualiza uma bicicleta | `200 OK` | `404` não encontrada, `422` dados inválidos |
| `DELETE` | `/api/v1/bicicletas/{id}` | Remove uma bicicleta | `204 No Content` | `404` não encontrada |

#### Exemplo: criar/atualizar bicicleta

```json
{
  "modelo": "Caloi Elite",
  "marca": "Caloi",
  "cor": "Preta",
  "anoFabricacao": 2024,
  "status": "DISPONIVEL"
}
```

- `modelo`: obrigatório (máx. 100 caracteres).
- `marca`: obrigatória (máx. 100 caracteres).
- `cor`: opcional (máx. 50 caracteres).
- `anoFabricacao`: opcional.
- `status`: opcional; valores aceitos: `DISPONIVEL`, `ALUGADA`, `MANUTENCAO` (padrão: `DISPONIVEL`).

## Testes

Para executar os testes:

```bash
./mvnw test
```

## Estrutura do projeto (resumo)

```
src/main/java/br/edu/ifc/bikes/
├── config/        # Configurações (OpenAPI/Swagger, timezone)
├── dto/           # DTOs de requisição/resposta e mappers (MapStruct)
├── entity/        # Entidades JPA (Usuario, Bicicleta)
├── exception/     # Exceções de domínio
├── repository/    # Repositórios Spring Data JPA
├── service/       # Regras de negócio
└── web/
    ├── controller/  # Controllers REST
    └── exception/   # Tratamento global de exceções e mensagens de erro
```

## Observações

- As senhas são armazenadas e comparadas em texto simples no estado atual do projeto (sem criptografia/hash).
- A aplicação utiliza locale fixo `pt_BR`.
