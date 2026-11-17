package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.AuthRequestDTO;
import br.edu.ifc.bikes.dto.AuthResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UsuarioIT {

    private static final String URI_USUARIOS = "/api/v1/usuarios";
    private static final String URI_USUARIO = "/api/v1/usuarios/{id}";
    private static final String URI_LOGIN = "/api/v1/auth/login";

    @LocalServerPort
    int port;

    @Autowired
    org.springframework.test.web.reactive.server.WebTestClient.Builder builder;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private UsuarioResponseDTO criarUsuario(String username, String password, String role) {
        UsuarioRequestDTO req = new UsuarioRequestDTO(username, password, role);
        return client().post().uri(URI_USUARIOS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsuarioResponseDTO.class)
                .returnResult().getResponseBody();
    }

    private String login(String username, String password) {
        AuthResponseDTO resp = client().post().uri(URI_LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequestDTO(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .returnResult().getResponseBody();
        assertThat(resp).isNotNull();
        return resp.token();
    }

    @Test
    @DisplayName("Alterar senha com dados válidos retorna 204 e a nova senha passa a valer")
    public void alterarSenha_DadosValidos_Retorna204() {
        UsuarioResponseDTO u = criarUsuario("jose@gmail.com", "123456", "ADMIN");
        String token = login("jose@gmail.com", "123456");

        patchSenha(u.id(), new UsuarioSenhaDTO("123456", "nova01", "nova01"), token)
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        String tokenNovo = login("jose@gmail.com", "nova01");
        patchSenha(u.id(), new UsuarioSenhaDTO("nova01", "nova02", "nova02"), tokenNovo)
                .expectStatus().isNoContent();

        String tokenNovo2 = login("jose@gmail.com", "nova02");
        ErrorMessage responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("123456", "nova03", "nova03"), tokenNovo2)
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
        UsuarioResponseDTO u1 = criarUsuario("maria@gmail.com", "123456", "CLIENTE");
        UsuarioResponseDTO u2 = criarUsuario("joao@gmail.com", "123456", "CLIENTE");
        String t1 = login("maria@gmail.com", "123456");
        String t2 = login("joao@gmail.com", "123456");

        patchSenha(u1.id(), new UsuarioSenhaDTO("123456", "nova01", "nova01"), t1)
                .expectStatus().isNoContent();

        patchSenha(u2.id(), new UsuarioSenhaDTO("123456", "nova02", "nova02"), t2)
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Alterar senha com senha atual incorreta retorna 400")
    public void alterarSenha_SenhaAtualIncorreta_RetornaErrorMessageStatus400() {
        UsuarioResponseDTO u = criarUsuario("jose2@gmail.com", "123456", "ADMIN");
        String token = login("jose2@gmail.com", "123456");

        ErrorMessage responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("senhaE", "nova01", "nova01"), token)
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
        UsuarioResponseDTO u = criarUsuario("jose3@gmail.com", "123456", "ADMIN");
        String token = login("jose3@gmail.com", "123456");

        ErrorMessage responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("123456", "nova01", "outra1"), token)
                .expectStatus().isBadRequest()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(400);
        assertThat(responseBody.getMessage()).isEqualTo("A nova senha não confere com a confirmação.");

        patchSenha(u.id(), new UsuarioSenhaDTO("123456", "nova01", "nova01"), token)
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Alterar senha de usuário inexistente (id 110) retorna 404")
    public void alterarSenha_IdInexistente_RetornaErrorMessageStatus404() {
        criarUsuario("admin404@gmail.com", "123456", "ADMIN");
        String token = login("admin404@gmail.com", "123456");

        ErrorMessage responseBody = patchSenha(110, new UsuarioSenhaDTO("123456", "nova01", "nova01"), token)
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
        UsuarioResponseDTO u = criarUsuario("jose4@gmail.com", "123456", "ADMIN");
        String token = login("jose4@gmail.com", "123456");

        ErrorMessage responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("", "nova01", "nova01"), token)
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("senhaAtual");
        assertThat(responseBody.getErrors().get("senhaAtual")).isEqualTo("A senha atual é obrigatória");

        responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("123456", "123", "123"), token)
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("novaSenha");
        assertThat(responseBody.getErrors().get("novaSenha")).isEqualTo("A senha deve ter entre 6 e 20 caracteres");

        String senhaLonga = "a".repeat(21);
        responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("123456", senhaLonga, senhaLonga), token)
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
        UsuarioResponseDTO u = criarUsuario("jose5@gmail.com", "123456", "ADMIN");
        String token = login("jose5@gmail.com", "123456");

        ErrorMessage responseBody = patchSenha(u.id(), new UsuarioSenhaDTO("", "123", ""), token)
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
        assertThat(responseBody.getErrors()).containsOnlyKeys("senhaAtual", "novaSenha", "confirmaSenha");
        assertThat(responseBody.getErrors().get("confirmaSenha")).isEqualTo("A confirmação da senha é obrigatória");
    }

    private WebTestClient.ResponseSpec patchSenha(long id, UsuarioSenhaDTO dto, String token) {
        return client()
                .patch()
                .uri(URI_USUARIO, id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange();
    }
}
