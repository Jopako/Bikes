package br.edu.ifc.bikes.dto;

public record BicicletaResponseDTO(
        Long id,
        String modelo,
        String marca,
        String cor,
        Integer anoFabricacao,
        String status
) {}
