# Bikes API

---

##  Banco de Dados H2 (Como Usar)

O H2 está configurado em memória (`jdbc:h2:mem:bikes`). Os dados existem enquanto a aplicação estiver em execução e são resetados ao reiniciar.

- **URL de Acesso**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **Credenciais de Conexão**:
  - **Driver Class**: `org.h2.Driver`
  - **JDBC URL**: `jdbc:h2:mem:bikes` *(atenção: altere o padrão testdb para bikes)*
  - **User Name**: `sa`
  - **Password**: *(deixe totalmente em branco / vazio)*

---

## Alteração de Senha

### Como Funciona
A troca de senha é realizada através da rota:
`PATCH /api/v1/usuarios/{id}`

### Corpo da Requisição (JSON)
```json
{
  "senhaAtual": "123456",
  "novaSenha": "novaSenha123",
  "confirmaSenha": "novaSenha123"
}
```

### Códigos de Retorno
- **`204 No Content`**: Senha alterada com sucesso.
- **`400 Bad Request`**: A senha atual informada está errada ou a nova senha não confere com a confirmação.
- **`404 Not Found`**: Usuário com o ID especificado não foi encontrado.
- **`422 Unprocessable Content`**: Dados inválidos (campos vazios ou senha fora do tamanho de 6 a 20 caracteres).

---

## Arquivos Envolvidos na Troca de Senha

| Arquivo | Função no Fluxo de Troca de Senha |
|---|---|
| `web/controller/UsuarioController.java` | Recebe a requisição HTTP `PATCH /{id}`, executa a validação `@Valid` e chama o service. Retorna `204 No Content`. |
| `dto/UsuarioSenhaDTO.java` | Record que define os dados de entrada (`senhaAtual`, `novaSenha`, `confirmaSenha`) com anotações `@NotBlank` e `@Size`. |
| `service/UsuarioService.java` | Regra de negócio: verifica se `novaSenha` confere com `confirmaSenha`, busca o usuário pelo ID, confere se `senhaAtual` bate com a salva no banco e faz o update. |
| `entity/Usuario.java` | Entidade JPA que armazena a senha (`password`) e tem o valor modificado. |
| `exception/PasswordInvalidException.java` | Exceção customizada (`RuntimeException`) lançada caso a senha atual não confira ou a confirmação seja diferente. |
| `web/exception/ApiExceptionHandler.java` | Captura `PasswordInvalidException` e devolve resposta HTTP 400 formatada no objeto `ErrorMessage`. |

---

## Documentação Interativa (Swagger UI)

Com a aplicação rodando, a documentação e os testes visuais de todas as rotas estão disponíveis em:
- **Swagger UI**: [http://localhost:8080/docs-bikes.html](http://localhost:8080/docs-bikes.html)
- **OpenAPI JSON**: [http://localhost:8080/docs-bikes](http://localhost:8080/docs-bikes)
