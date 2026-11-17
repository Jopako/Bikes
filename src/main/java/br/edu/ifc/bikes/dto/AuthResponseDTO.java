package br.edu.ifc.bikes.dto;

public record AuthResponseDTO(String token, String type, Long usuarioId, String username, String role) {}
