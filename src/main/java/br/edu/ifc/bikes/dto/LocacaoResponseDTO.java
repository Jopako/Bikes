package br.edu.ifc.bikes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LocacaoResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        Long bicicletaId,
        String bicicletaCodigo,
        LocalDateTime dataRetirada,
        LocalDateTime dataPrevistaDevolucao,
        LocalDateTime dataEfetivaDevolucao,
        String status,
        BigDecimal valorDebito
) {}
