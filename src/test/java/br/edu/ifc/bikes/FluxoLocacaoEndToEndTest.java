package br.edu.ifc.bikes;

import br.edu.ifc.bikes.dto.AuthRequestDTO;
import br.edu.ifc.bikes.dto.AuthResponseDTO;
import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.DevolucaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoRequestDTO;
import br.edu.ifc.bikes.dto.LocacaoResponseDTO;
import br.edu.ifc.bikes.dto.RelatorioLocacaoDTO;
import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste ponto a ponto (HTTP real) cobrindo Cliente, Bicicleta,
 * Locacao, Devolucao e Relatorio - requisitos 4 a 8 do projeto.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FluxoLocacaoEndToEndTest {

    @LocalServerPort
    int port;

    @Value("${app.admin.username}")
    String adminUsername;

    @Value("${app.admin.password}")
    String adminPassword;

    private WebTestClient client() {
        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    private String login(String username, String password) {
        AuthResponseDTO resp = client().post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new AuthRequestDTO(username, password))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponseDTO.class)
                .returnResult().getResponseBody();
        assertThat(resp).isNotNull();
        return resp.token();
    }

    private String criarCliente() {
        String username = "cliente-" + UUID.randomUUID() + "@ifc.edu.br";
        client().post().uri("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UsuarioRequestDTO(username, "123456", "CLIENTE"))
                .exchange()
                .expectStatus().isCreated();

        String token = login(username, "123456");
        String cpf = UUID.randomUUID().toString().replaceAll("\\D", "").substring(0, 11);
        client().post().uri("/api/v1/clientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ClienteRequestDTO("Cliente Teste", cpf))
                .exchange()
                .expectStatus().isCreated();
        return token;
    }

    private String criarBicicleta(String tokenAdmin) {
        String codigo = "BIKE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        client().post().uri("/api/v1/bicicletas")
                .header("Authorization", "Bearer " + tokenAdmin)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BicicletaRequestDTO(codigo, "Caloi Elite", "Caloi", "Preta", 2024, "LIVRE"))
                .exchange()
                .expectStatus().isCreated();
        return codigo;
    }

    private LocacaoResponseDTO solicitar(String tokenCliente, String codigo) {
        return client().post().uri("/api/v1/locacoes")
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LocacaoRequestDTO(
                        codigo, null, LocalDateTime.now().plusHours(2), null, "4111111111111111"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();
    }

    private void verificarStatusBicicleta(String tokenAdmin, String codigo, String status) {
        client().get().uri("/api/v1/bicicletas/codigo/{codigo}", codigo)
                .header("Authorization", "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BicicletaResponseDTO.class)
                .value(b -> assertThat(b.status()).isEqualTo(status));
    }

    @Test
    @DisplayName("Fluxo completo: cadastro, locação, devolução e relatório")
    void fluxoCompletoDeLocacao() {
        String tokenAdmin = login(adminUsername, adminPassword);
        String tokenCliente = criarCliente();
        String codigo = criarBicicleta(tokenAdmin);

        LocacaoResponseDTO locacao = solicitar(tokenCliente, codigo);
        assertThat(locacao.status()).isEqualTo("ATIVA");
        verificarStatusBicicleta(tokenAdmin, codigo, "OCUPADA");

        // Uma bicicleta não pode participar de duas locações simultâneas.
        client().post().uri("/api/v1/locacoes")
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LocacaoRequestDTO(
                        codigo, null, LocalDateTime.now().plusHours(2), null, "4111111111111111"))
                .exchange()
                .expectStatus().isBadRequest();

        LocacaoResponseDTO devolucao = client().post()
                .uri("/api/v1/locacoes/{id}/devolucao", locacao.id())
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LocacaoResponseDTO.class)
                .returnResult().getResponseBody();

        assertThat(devolucao.status()).isEqualTo("FINALIZADA");
        assertThat(devolucao.dataEfetivaDevolucao()).isNotNull();
        assertThat(devolucao.valorDebito()).isGreaterThan(BigDecimal.ZERO);
        verificarStatusBicicleta(tokenAdmin, codigo, "LIVRE");

        // A devolução não pode ser registrada duas vezes.
        client().post().uri("/api/v1/locacoes/{id}/devolucao", locacao.id())
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DevolucaoRequestDTO(false))
                .exchange()
                .expectStatus().isBadRequest();

        client().get().uri("/api/v1/locacoes/relatorio")
                .header("Authorization", "Bearer " + tokenAdmin)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RelatorioLocacaoDTO.class)
                .value(itens -> assertThat(itens).anySatisfy(item -> {
                    assertThat(item.nomeCliente()).isEqualTo("Cliente Teste");
                    assertThat(item.codigoBicicleta()).isEqualTo(codigo);
                    assertThat(item.statusLocacao()).isEqualTo("FINALIZADA");
                    assertThat(item.duracaoMinutos()).isNotNull();
                }));
    }

    @Test
    @DisplayName("Devolução com problema envia a bicicleta para manutenção")
    void devolucaoComProblema_StatusManutencao() {
        String tokenAdmin = login(adminUsername, adminPassword);
        String tokenCliente = criarCliente();
        String codigo = criarBicicleta(tokenAdmin);

        LocacaoResponseDTO locacao = solicitar(tokenCliente, codigo);

        client().post().uri("/api/v1/locacoes/{id}/devolucao", locacao.id())
                .header("Authorization", "Bearer " + tokenCliente)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new DevolucaoRequestDTO(true))
                .exchange()
                .expectStatus().isOk();

        verificarStatusBicicleta(tokenAdmin, codigo, "MANUTENCAO");
    }
}
