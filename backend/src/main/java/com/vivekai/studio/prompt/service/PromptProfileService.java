package com.vivekai.studio.prompt.service;

import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.prompt.dto.PromptProfileRequest;
import com.vivekai.studio.prompt.dto.PromptProfileResponse;
import com.vivekai.studio.prompt.dto.PromptProfileVersionResponse;
import com.vivekai.studio.prompt.dto.PromptVariableResponse;
import com.vivekai.studio.prompt.dto.PromptVersionRequest;
import com.vivekai.studio.prompt.entity.PromptCategory;
import com.vivekai.studio.prompt.entity.PromptProfile;
import com.vivekai.studio.prompt.entity.PromptProfileVersion;
import com.vivekai.studio.prompt.entity.PromptTag;
import com.vivekai.studio.prompt.entity.PromptVariable;
import com.vivekai.studio.prompt.mapper.PromptProfileMapper;
import com.vivekai.studio.prompt.repository.PromptCategoryRepository;
import com.vivekai.studio.prompt.repository.PromptProfileRepository;
import com.vivekai.studio.prompt.repository.PromptProfileVersionRepository;
import com.vivekai.studio.prompt.repository.PromptTagRepository;
import com.vivekai.studio.prompt.repository.PromptVariableRepository;
import com.vivekai.studio.provider.entity.AIProvider;
import com.vivekai.studio.provider.repository.AIProviderRepository;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptProfileService {

    private final PromptProfileRepository profileRepository;
    private final PromptProfileVersionRepository versionRepository;
    private final PromptVariableRepository variableRepository;
    private final PromptCategoryRepository categoryRepository;
    private final PromptTagRepository tagRepository;
    private final AIProviderRepository providerRepository;
    private final UserRepository userRepository;
    
    private final PromptProfileMapper mapper;

    @Transactional
    public PromptProfileResponse createProfile(PromptProfileRequest request, UUID creatorId) {
        log.info("Creating new prompt profile: {}", request.getName());
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found with ID: " + creatorId));
        AIProvider provider = providerRepository.findByName(request.getProviderCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + request.getProviderCode()));

        PromptCategory category = null;
        if (request.getCategoryName() != null) {
            category = categoryRepository.findByName(request.getCategoryName().toUpperCase())
                    .orElse(null);
        }

        // Map and save Tags
        Set<PromptTag> tags = new HashSet<>();
        for (String tagName : request.getTags()) {
            PromptTag tag = tagRepository.findByName(tagName.toLowerCase())
                    .orElseGet(() -> tagRepository.save(PromptTag.builder().name(tagName.toLowerCase()).build()));
            tags.add(tag);
        }

        PromptProfile profile = PromptProfile.builder()
                .name(request.getName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .provider(provider)
                .modelName(request.getModelName())
                .isDefault(false)
                .creator(creator)
                .category(category)
                .visibility(request.getVisibility().toUpperCase())
                .tags(tags)
                .build();

        PromptProfile savedProfile = profileRepository.save(profile);

        // Build version 1
        PromptProfileVersion version = PromptProfileVersion.builder()
                .profile(savedProfile)
                .versionNumber(1)
                .systemPrompt(request.getSystemPrompt())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .topP(request.getTopP())
                .presencePenalty(request.getPresencePenalty())
                .frequencyPenalty(request.getFrequencyPenalty())
                .responseFormat(request.getResponseFormat())
                .creator(creator)
                .build();
        PromptProfileVersion savedVersion = versionRepository.save(version);

        // Map and save Variables
        List<PromptVariable> variables = request.getVariables().stream()
                .map(varReq -> PromptVariable.builder()
                        .profile(savedProfile)
                        .name(varReq.getName())
                        .description(varReq.getDescription())
                        .isRequired(varReq.isRequired())
                        .defaultValue(varReq.getDefaultValue())
                        .type(varReq.getType())
                        .build())
                .collect(Collectors.toList());
        variableRepository.saveAll(variables);

        return getProfileDetails(savedProfile.getId());
    }

    @Transactional
    public PromptProfileVersionResponse createVersion(UUID profileId, PromptVersionRequest request, UUID creatorId) {
        log.info("Creating new version for prompt profile ID: {}", profileId);
        PromptProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt profile not found with ID: " + profileId));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found with ID: " + creatorId));

        Integer latestVerNumber = versionRepository.findFirstByProfileIdOrderByVersionNumberDesc(profileId)
                .map(PromptProfileVersion::getVersionNumber)
                .orElse(0);

        PromptProfileVersion version = PromptProfileVersion.builder()
                .profile(profile)
                .versionNumber(latestVerNumber + 1)
                .systemPrompt(request.getSystemPrompt())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .topP(request.getTopP())
                .presencePenalty(request.getPresencePenalty())
                .frequencyPenalty(request.getFrequencyPenalty())
                .responseFormat(request.getResponseFormat())
                .creator(creator)
                .build();

        PromptProfileVersion saved = versionRepository.save(version);
        return mapper.toVersionResponse(saved);
    }

    @Transactional(readOnly = true)
    public PromptProfileResponse getProfileDetails(UUID profileId) {
        PromptProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt profile not found with ID: " + profileId));

        PromptProfileResponse response = mapper.toResponse(profile);

        // Load latest version
        versionRepository.findFirstByProfileIdOrderByVersionNumberDesc(profileId)
                .ifPresent(version -> response.setLatestVersion(mapper.toVersionResponse(version)));

        // Load variables
        List<PromptVariable> variables = variableRepository.findByProfileId(profileId);
        List<PromptVariableResponse> varResponses = variables.stream()
                .map(mapper::toVariableResponse)
                .collect(Collectors.toList());
        response.setVariables(varResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public List<PromptProfileResponse> searchProfiles(String name, String categoryName, String visibility, String tag) {
        log.info("Searching prompt profiles: name={}, category={}, visibility={}, tag={}", name, categoryName, visibility, tag);
        List<PromptProfile> profiles = profileRepository.searchProfiles(name, categoryName, visibility, tag);
        return profiles.stream()
                .map(p -> getProfileDetails(p.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addFavorite(UUID userId, UUID profileId) {
        log.info("Adding prompt profile {} to user favorites: {}", profileId, userId);
        profileRepository.addFavorite(userId, profileId);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID profileId) {
        log.info("Removing prompt profile {} from user favorites: {}", profileId, userId);
        profileRepository.removeFavorite(userId, profileId);
    }

    @Transactional(readOnly = true)
    public List<PromptProfileResponse> listFavorites(UUID userId) {
        log.info("Fetching favorite prompt profiles list for user ID: {}", userId);
        List<PromptProfile> profiles = profileRepository.findFavoritesForUser(userId);
        return profiles.stream()
                .map(p -> getProfileDetails(p.getId()))
                .collect(Collectors.toList());
    }
}
