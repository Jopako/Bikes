package br.edu.ifc.bikes.dto;

import java.time.LocalDateTime;

public record RelatorioLocacaoDTO(
        String nomeCliente,
        String codigoBicicleta,
        LocalDateTime dataRetirada,
        LocalDateTime dataDevolucao,
        Long duracaoMinutos,
        String statusLocacao
) {}
