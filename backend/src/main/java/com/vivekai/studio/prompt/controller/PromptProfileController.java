package com.vivekai.studio.prompt.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.prompt.dto.PromptProfileRequest;
import com.vivekai.studio.prompt.dto.PromptProfileResponse;
import com.vivekai.studio.prompt.dto.PromptProfileVersionResponse;
import com.vivekai.studio.prompt.dto.PromptVersionRequest;
import com.vivekai.studio.prompt.service.PromptProfileService;
import com.vivekai.studio.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
@Slf4j
public class PromptProfileController {

    private final PromptProfileService promptService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromptProfileResponse>> createPrompt(
            @Valid @RequestBody PromptProfileRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to create prompt profile: '{}' from user: {}", request.getName(), userDetails.getUsername());
        PromptProfileResponse response = promptService.createProfile(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Prompt profile created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromptProfileResponse>> getPromptDetails(@PathVariable UUID id) {
        log.info("Received request for prompt details: {}", id);
        PromptProfileResponse response = promptService.getProfileDetails(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Prompt profile details retrieved"));
    }

    @PostMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<PromptProfileVersionResponse>> createVersion(
            @PathVariable UUID id,
            @Valid @RequestBody PromptVersionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to create version for profile: {} from user: {}", id, userDetails.getUsername());
        PromptProfileVersionResponse response = promptService.createVersion(id, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "New prompt version published successfully"));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to favorite profile {} from user: {}", id, userDetails.getUsername());
        promptService.addFavorite(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Prompt profile added to favorites"));
    }

    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to unfavorite profile {} from user: {}", id, userDetails.getUsername());
        promptService.removeFavorite(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Prompt profile removed from favorites"));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<PromptProfileResponse>>> getFavorites(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to fetch favorite profiles from user: {}", userDetails.getUsername());
        List<PromptProfileResponse> favorites = promptService.listFavorites(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PromptProfileResponse>>> searchPrompts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String visibility,
            @RequestParam(required = false) String tag
    ) {
        log.info("Search prompt profiles query parameters received");
        List<PromptProfileResponse> responses = promptService.searchProfiles(name, category, visibility, tag);
        return ResponseEntity.ok(ApiResponse.success(responses, "Search completed successfully"));
    }
}
