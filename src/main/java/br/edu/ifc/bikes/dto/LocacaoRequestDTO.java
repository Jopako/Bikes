package br.edu.ifc.bikes.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LocacaoRequestDTO(
        String bicicletaCodigo,
        Long bicicletaId,
        @NotNull(message = "Data/hora prevista para devolução é obrigatória")
        @Future(message = "Data prevista deve ser futura")
        LocalDateTime dataPrevistaDevolucao,
        LocalDateTime dataRetirada,
        @NotBlank(message = "Dados do cartão de crédito são obrigatórios")
        String cartaoCredito
) {}
