package com.vivekai.studio.workspace.controller;

import com.vivekai.studio.common.dto.ApiResponse;
import com.vivekai.studio.security.service.UserDetailsImpl;
import com.vivekai.studio.workspace.dto.FolderRequest;
import com.vivekai.studio.workspace.dto.WorkspaceRequest;
import com.vivekai.studio.workspace.entity.Folder;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Workspace>>> getWorkspaces(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Fetching workspaces list request from user: {}", userDetails.getUsername());
        List<Workspace> workspaces = workspaceService.listWorkspacesForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(workspaces, "Workspaces retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Workspace>> createWorkspace(
            @Valid @RequestBody WorkspaceRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to create workspace: {} from user: {}", request.getName(), userDetails.getUsername());
        Workspace workspace = workspaceService.createWorkspace(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(workspace, "Workspace created successfully"));
    }

    @PostMapping("/{workspaceId}/folders")
    public ResponseEntity<ApiResponse<Folder>> createFolder(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody FolderRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to create folder in workspace: {} from user: {}", workspaceId, userDetails.getUsername());
        Folder folder = workspaceService.createFolder(workspaceId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(folder, "Folder created successfully"));
    }

    @GetMapping("/{workspaceId}/folders")
    public ResponseEntity<ApiResponse<List<Folder>>> listFolders(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        log.info("Received request to list folders for workspace: {} from user: {}", workspaceId, userDetails.getUsername());
        List<Folder> folders = workspaceService.listFolders(workspaceId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(folders, "Folders retrieved successfully"));
    }
}
