package org.apache.airavata.application.mapper;

import org.apache.airavata.application.dto.template.ApplicationTemplateInputDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateOutputDto;
import org.apache.airavata.application.dto.template.ApplicationTemplateResponseDto;
import org.apache.airavata.application.model.template.ApplicationTemplateEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateInputEntity;
import org.apache.airavata.application.model.template.ApplicationTemplateOutputEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Entity/DTO conversions for the application template aggregate. The back-reference to
 * the owning template is deliberately unmapped on the child entities — the service wires
 * it up so both sides of the association stay consistent.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplicationTemplateMapper {

    ApplicationTemplateResponseDto toResponseDto(ApplicationTemplateEntity entity);

    ApplicationTemplateInputDto toInputDto(ApplicationTemplateInputEntity entity);

    ApplicationTemplateOutputDto toOutputDto(ApplicationTemplateOutputEntity entity);

    @Mapping(target = "inputId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    ApplicationTemplateInputEntity toInputEntity(ApplicationTemplateInputDto dto);

    @Mapping(target = "outputId", ignore = true)
    @Mapping(target = "applicationTemplate", ignore = true)
    ApplicationTemplateOutputEntity toOutputEntity(ApplicationTemplateOutputDto dto);
}
