package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
        statements = {
                "insert into usuarios (id, username, password, role) values (100, 'jose@gmail.com', '123456', 'ADMIN')",
                "insert into usuarios (id, username, password, role) values (101, 'maria@gmail.com', '123456', 'CLIENTE')",
                "insert into usuarios (id, username, password, role) values (102, 'joao@gmail.com', '123456', 'CLIENTE')"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(
        statements = "delete from usuarios",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UsuarioIT {

    private static final String URI_USUARIO = "/api/v1/usuarios/{id}";

    @LocalServerPort
    int port;

    WebTestClient testClient;

    @BeforeEach
    void setUp() {
        this.testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

  

    @Test
    @DisplayName("Alterar senha com dados válidos retorna 204 e a nova senha passa a valer")
    public void alterarSenha_DadosValidos_Retorna204() {
        patchSenha(100, new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1"))
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        patchSenha(100, new UsuarioSenhaDTO("novaSenha1", "outraSenha2", "outraSenha2"))
                .expectStatus().isNoContent();

        ErrorMessage responseBody = patchSenha(100, new UsuarioSenhaDTO("123456", "qualquer1", "qualquer1"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(400);
        assertThat(responseBody.getMessage()).isEqualTo("A senha atual não confere.");
    }

    @Test
    @DisplayName("Alterar senha de um usuário não afeta os demais usuários")
    public void alterarSenha_DadosValidos_NaoAlteraOutrosUsuarios() {
        patchSenha(101, new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1"))
                .expectStatus().isNoContent();

        patchSenha(102, new UsuarioSenhaDTO("123456", "novaSenha2", "novaSenha2"))
                .expectStatus().isNoContent();
    }


    @Test
    @DisplayName("Alterar senha com senha atual incorreta retorna 400")
    public void alterarSenha_SenhaAtualIncorreta_RetornaErrorMessageStatus400() {
        ErrorMessage responseBody = patchSenha(100, new UsuarioSenhaDTO("senhaErrada", "novaSenha1", "novaSenha1"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(400);
        assertThat(responseBody.getMessage()).isEqualTo("A senha atual não confere.");
    }

    @Test
    @DisplayName("Alterar senha com confirmação diferente da nova senha retorna 400")
    public void alterarSenha_ConfirmacaoNaoConfere_RetornaErrorMessageStatus400() {
        ErrorMessage responseBody = patchSenha(100, new UsuarioSenhaDTO("123456", "novaSenha1", "outraSenha"))
                .expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(400);
        assertThat(responseBody.getMessage()).isEqualTo("A nova senha não confere com a confirmação.");

        patchSenha(100, new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1"))
                .expectStatus().isNoContent();
    }

  

    @Test
    @DisplayName("Alterar senha de usuário inexistente (id 110) retorna 404")
    public void alterarSenha_IdInexistente_RetornaErrorMessageStatus404() {
        ErrorMessage responseBody = patchSenha(110, new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1"))
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
        assertThat(responseBody.getMessage()).isEqualTo("Usuário com id=110 não encontrado");
    }



    @Test 
    @DisplayName("Alterar senha com campos inválidos retorna 422")
    public void alterarSenha_CamposInvalidos_RetornaErrorMessageStatus422() {
        ErrorMessage responseBody = patchSenha(100, new UsuarioSenhaDTO("", "novaSenha1", "novaSenha1"))
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("senhaAtual");
        assertThat(responseBody.getErrors().get("senhaAtual")).isEqualTo("A senha atual é obrigatória");

        responseBody = patchSenha(100, new UsuarioSenhaDTO("123456", "123", "123"))
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("novaSenha");
        assertThat(responseBody.getErrors().get("novaSenha")).isEqualTo("A senha deve ter entre 6 e 20 caracteres");

        String senhaLonga = "a".repeat(21);
        responseBody = patchSenha(100, new UsuarioSenhaDTO("123456", senhaLonga, senhaLonga))
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("novaSenha");
    }

    @Test
    @DisplayName("Alterar senha com vários campos inválidos retorna 422 listando cada campo")
    public void alterarSenha_VariosCamposInvalidos_RetornaErrosDeCadaCampo() {
        ErrorMessage responseBody = patchSenha(100, new UsuarioSenhaDTO("", "123", ""))
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("senhaAtual", "novaSenha", "confirmaSenha");
        assertThat(responseBody.getErrors().get("confirmaSenha")).isEqualTo("A confirmação da senha é obrigatória");
    }


    private WebTestClient.ResponseSpec patchSenha(long id, UsuarioSenhaDTO dto) {
        return testClient
                .patch()
                .uri(URI_USUARIO, id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange();
    }
}
