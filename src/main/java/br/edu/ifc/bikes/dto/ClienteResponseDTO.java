package br.edu.ifc.bikes.dto;

public record ClienteResponseDTO(
        Long id,
        String nomeCompleto,
        String cpf,
        Long usuarioId,
        String username
) {}
