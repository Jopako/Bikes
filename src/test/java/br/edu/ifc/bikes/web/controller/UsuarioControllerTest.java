package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.AuthRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String login(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDTO(username, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private String loginAdmin() throws Exception {
        return login("admin@bikes.com", "123456");
    }

    private JsonNode criarUsuario(String username, String role) throws Exception {
        String body = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioRequestDTO(username, "123456", role))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    @Test
    @DisplayName("Criar usuário com sucesso retorna 201 e dados do usuário")
    void createUsuario_ComDadosValidos_Retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UsuarioRequestDTO("maria@ifc.edu.br", "123456", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("maria@ifc.edu.br"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Cadastro público nunca cria ADMIN, mesmo se o cliente informar o perfil")
    void createUsuario_ComRoleAdmin_SempreRetornaCliente() throws Exception {
        JsonNode criado = criarUsuario("admin-falso@ifc.edu.br", "ADMIN");
        org.assertj.core.api.Assertions.assertThat(criado.get("role").asText()).isEqualTo("CLIENTE");
        org.assertj.core.api.Assertions.assertThat(login("admin-falso@ifc.edu.br", "123456")).isNotBlank();
    }

    @Test
    @DisplayName("Criar usuário com dados inválidos retorna 422")
    void createUsuario_ComDadosInvalidos_Retorna422() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("email-invalido", "123", null);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Criar usuário duplicado retorna 409")
    void createUsuario_ComUsernameDuplicado_Retorna409() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("pedro@ifc.edu.br", "123456", null);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("Buscar usuário por id retorna 200")
    void getById_ComIdExistente_Retorna200() throws Exception {
        JsonNode criado = criarUsuario("ana@ifc.edu.br", null);
        long id = criado.get("id").asLong();

        mockMvc.perform(get("/api/v1/usuarios/{id}", id)
                .header("Authorization", "Bearer " + loginAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value("ana@ifc.edu.br"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Buscar usuário por id inexistente retorna 404")
    void getById_ComIdInexistente_Retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/999999")
                .header("Authorization", "Bearer " + loginAdmin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Atualizar senha com sucesso retorna 204")
    void updatePassword_ComSucesso_Retorna204() throws Exception {
        long id = criarUsuario("lucas@ifc.edu.br", null).get("id").asLong();
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "nova01", "nova01");

        mockMvc.perform(patch("/api/v1/usuarios/{id}", id)
                        .header("Authorization", "Bearer " + login("lucas@ifc.edu.br", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Atualizar senha quando confirmação não confere retorna 400")
    void updatePassword_ComConfirmacaoIncorreta_Retorna400() throws Exception {
        long id = criarUsuario("carlos@ifc.edu.br", null).get("id").asLong();
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "nova01", "outra1");

        mockMvc.perform(patch("/api/v1/usuarios/{id}", id)
                        .header("Authorization", "Bearer " + login("carlos@ifc.edu.br", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A nova senha não confere com a confirmação."));
    }

    @Test
    @DisplayName("Atualizar senha com senha atual incorreta retorna 400")
    void updatePassword_ComSenhaAtualIncorreta_Retorna400() throws Exception {
        long id = criarUsuario("julia@ifc.edu.br", null).get("id").asLong();
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("senhaErrada", "nova01", "nova01");

        mockMvc.perform(patch("/api/v1/usuarios/{id}", id)
                        .header("Authorization", "Bearer " + login("julia@ifc.edu.br", "123456"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A senha atual não confere."));
    }

    @Test
    @DisplayName("Atualizar senha de usuário inexistente retorna 404")
    void updatePassword_ComUsuarioInexistente_Retorna404() throws Exception {
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "nova01", "nova01");

        mockMvc.perform(patch("/api/v1/usuarios/999999")
                        .header("Authorization", "Bearer " + loginAdmin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Listar todos os usuários retorna 200 e lista")
    void getAll_RetornaListaDeUsuarios() throws Exception {
        criarUsuario("user1@ifc.edu.br", null);
        criarUsuario("user2@ifc.edu.br", null);

        mockMvc.perform(get("/api/v1/usuarios")
                .header("Authorization", "Bearer " + loginAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].username",
                        hasItems("user1@ifc.edu.br", "user2@ifc.edu.br")));
    }
}
