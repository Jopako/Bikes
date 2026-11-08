package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.jwt.JwtUtils;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.web.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/clientes/clientes-insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/clientes/clientes-delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ClienteIT {

    @LocalServerPort
    int port;

    @Autowired
    ClienteRepository clienteRepository;

    WebTestClient testClient;

    String tokenClienteSemCadastro;
    String tokenClienteComCadastro;
    String tokenAdmin;

    @BeforeEach
    void setUp() {
        this.testClient = WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        this.tokenClienteSemCadastro = JwtUtils.createToken("cliente1@gmail.com", "ROLE_CLIENTE").getToken();
        this.tokenClienteComCadastro = JwtUtils.createToken("cliente2@gmail.com", "ROLE_CLIENTE").getToken();
        this.tokenAdmin = JwtUtils.createToken("admin@gmail.com", "ROLE_ADMIN").getToken();
    }

    @Test
    public void criarCliente_UsuarioAutenticado_RetornaClienteCriado() {
        ClienteResponseDTO responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteSemCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("João Pereira", "98765432100"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isNotNull();
        assertThat(responseBody.nome()).isEqualTo("João Pereira");
        assertThat(responseBody.cpf()).isEqualTo("98765432100");

        Cliente cliente = clienteRepository.findByCpf("98765432100").orElseThrow();
        assertThat(cliente.getDataCriacao()).isNotNull();
        assertThat(cliente.getDataModificacao()).isNotNull();
        assertThat(cliente.getCriadoPor()).isEqualTo("cliente1@gmail.com");
        assertThat(cliente.getModificadoPor()).isEqualTo("cliente1@gmail.com");
    }

    @Test
    public void criarCliente_SemAutenticacao_RetornaAcessoNegado() {
        testClient
                .post()
                .uri("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("João Pereira", "98765432100"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void criarCliente_CpfJaCadastrado_RetornaErrorMessageStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteSemCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("João Pereira", "12345678901"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void criarCliente_UsuarioJaPossuiCliente_RetornaErrorMessageStatus409() {
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Maria Silva", "98765432100"))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(409);
    }

    @Test
    public void criarCliente_CamposInvalidos_RetornaErrorMessageStatus422() {
        ErrorMessage responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteSemCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("", ""))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);

        responseBody = testClient
                .post()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteSemCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("João Pereira", "123"))
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(422);
    }

    @Test
    public void buscarCliente_IdExistenteComoDono_RetornaCliente() {
        ClienteResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(100);
        assertThat(responseBody.nome()).isEqualTo("Maria Silva");
        assertThat(responseBody.cpf()).isEqualTo("12345678901");
    }

    @Test
    public void buscarCliente_IdExistenteComoAdmin_RetornaCliente() {
        ClienteResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(100);
    }

    @Test
    public void buscarCliente_OutroCliente_RetornaAcessoNegado() {
        testClient
                .get()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteSemCadastro)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void buscarCliente_IdInexistenteComoAdmin_RetornaErrorMessageStatus404() {
        ErrorMessage responseBody = testClient
                .get()
                .uri("/api/v1/clientes/999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ErrorMessage.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getStatus()).isEqualTo(404);
    }

    @Test
    public void buscarMeusDados_ClienteAutenticado_RetornaCliente() {
        ClienteResponseDTO responseBody = testClient
                .get()
                .uri("/api/v1/clientes/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(100);
        assertThat(responseBody.nome()).isEqualTo("Maria Silva");
    }

    @Test
    public void listarClientes_ComoAdmin_RetornaListaDeClientes() {
        List<ClienteResponseDTO> responseBody = testClient
                .get()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.getFirst().id()).isEqualTo(100);
    }

    @Test
    public void listarClientes_ComoCliente_RetornaAcessoNegado() {
        testClient
                .get()
                .uri("/api/v1/clientes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void atualizarCliente_ComoDono_RetornaClienteAtualizado() {
        ClienteResponseDTO responseBody = testClient
                .put()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Maria Souza", "12345678901"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClienteResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.id()).isEqualTo(100);
        assertThat(responseBody.nome()).isEqualTo("Maria Souza");
    }

    @Test
    public void removerCliente_ComoDono_RetornaNoContent() {
        testClient
                .delete()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteComCadastro)
                .exchange()
                .expectStatus().isNoContent();

        testClient
                .get()
                .uri("/api/v1/clientes/100")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isNotFound();
    }
}
