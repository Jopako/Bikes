package br.edu.ifc.bikes.web.controller;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioSenhaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Criar usuário com sucesso retorna 201 e dados do usuário")
    void createUsuario_ComDadosValidos_Retorna201() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("maria@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value("maria@ifc.edu.br"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Criar usuário com dados inválidos retorna 422")
    void createUsuario_ComDadosInvalidos_Retorna422() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("email-invalido", "123");

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
        UsuarioRequestDTO dto = new UsuarioRequestDTO("pedro@ifc.edu.br", "123456");

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
        UsuarioRequestDTO dto = new UsuarioRequestDTO("ana@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ana@ifc.edu.br"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("Buscar usuário por id inexistente retorna 404")
    void getById_ComIdInexistente_Retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Atualizar senha com sucesso retorna 204")
    void updatePassword_ComSucesso_Retorna204() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("lucas@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1");

        mockMvc.perform(patch("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Atualizar senha quando confirmação não confere retorna 400")
    void updatePassword_ComConfirmacaoIncorreta_Retorna400() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("carlos@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "novaSenha1", "outraSenha");

        mockMvc.perform(patch("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A nova senha não confere com a confirmação."));
    }

    @Test
    @DisplayName("Atualizar senha com senha atual incorreta retorna 400")
    void updatePassword_ComSenhaAtualIncorreta_Retorna400() throws Exception {
        UsuarioRequestDTO dto = new UsuarioRequestDTO("julia@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("senhaErrada", "novaSenha1", "novaSenha1");

        mockMvc.perform(patch("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A senha atual não confere."));
    }

    @Test
    @DisplayName("Atualizar senha de usuário inexistente retorna 404")
    void updatePassword_ComUsuarioInexistente_Retorna404() throws Exception {
        UsuarioSenhaDTO senhaDTO = new UsuarioSenhaDTO("123456", "novaSenha1", "novaSenha1");

        mockMvc.perform(patch("/api/v1/usuarios/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(senhaDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Listar todos os usuários retorna 200 e lista")
    void getAll_RetornaListaDeUsuarios() throws Exception {
        UsuarioRequestDTO dto1 = new UsuarioRequestDTO("user1@ifc.edu.br", "123456");
        UsuarioRequestDTO dto2 = new UsuarioRequestDTO("user2@ifc.edu.br", "123456");

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
