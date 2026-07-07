package com.vivekai.studio.prompt.mapper;

import com.vivekai.studio.prompt.dto.PromptProfileResponse;
import com.vivekai.studio.prompt.dto.PromptProfileVersionResponse;
import com.vivekai.studio.prompt.dto.PromptVariableResponse;
import com.vivekai.studio.prompt.entity.PromptProfile;
import com.vivekai.studio.prompt.entity.PromptProfileVersion;
import com.vivekai.studio.prompt.entity.PromptTag;
import com.vivekai.studio.prompt.entity.PromptVariable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromptProfileMapper {

    @Mapping(target = "type", expression = "java(variable.getType() != null ? variable.getType().name() : null)")
    PromptVariableResponse toVariableResponse(PromptVariable variable);

    @Mapping(target = "createdByUsername", expression = "java(version.getCreator() != null ? version.getCreator().getUsername() : null)")
    PromptProfileVersionResponse toVersionResponse(PromptProfileVersion version);

    @Mapping(target = "providerName", expression = "java(profile.getProvider() != null ? profile.getProvider().getName() : null)")
    @Mapping(target = "categoryName", expression = "java(profile.getCategory() != null ? profile.getCategory().getName() : null)")
    @Mapping(target = "creatorUsername", expression = "java(profile.getCreator() != null ? profile.getCreator().getUsername() : null)")
    @Mapping(target = "tags", expression = "java(mapTags(profile.getTags()))")
    @Mapping(target = "variables", ignore = true)
    @Mapping(target = "latestVersion", ignore = true)
    PromptProfileResponse toResponse(PromptProfile profile);

    default Set<String> mapTags(Set<PromptTag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(PromptTag::getName)
                .collect(Collectors.toSet());
    }
}
