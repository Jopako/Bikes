package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "O email deve ser válido")
        String username,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, max = 6, message = "A senha deve ter 6 caracteres")
        String password,
        @Schema(description = "Ignorado no cadastro público: o perfil atribuído é sempre CLIENTE",
                accessMode = Schema.AccessMode.READ_ONLY, example = "CLIENTE")
        String role
) {}
