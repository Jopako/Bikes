package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BicicletaRequestDTO(
        @NotBlank(message = "O código é obrigatório")
        @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
        String codigo,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve ter no máximo 100 caracteres")
        String modelo,

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve ter no máximo 100 caracteres")
        String marca,

        @Size(max = 50, message = "A cor deve ter no máximo 50 caracteres")
        String cor,

        Integer anoFabricacao,

        @Pattern(regexp = "LIVRE|OCUPADA|MANUTENCAO", message = "Status deve ser LIVRE, OCUPADA ou MANUTENCAO")
        String status
) {}
