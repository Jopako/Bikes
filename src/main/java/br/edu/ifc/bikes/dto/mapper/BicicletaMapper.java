package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.entity.Bicicleta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BicicletaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataModificacao", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    Bicicleta toBicicleta(BicicletaRequestDTO dto);

    BicicletaResponseDTO toResponse(Bicicleta bicicleta);

    List<BicicletaResponseDTO> toResponse(List<Bicicleta> bicicletas);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataModificacao", ignore = true)
    @Mapping(target = "criadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateFromDto(BicicletaRequestDTO dto, @MappingTarget Bicicleta bicicleta);
}
