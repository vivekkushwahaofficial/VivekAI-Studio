package com.vivekai.studio.workspace.service;

import com.vivekai.studio.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.repository.UserRepository;
import com.vivekai.studio.workspace.dto.FolderRequest;
import com.vivekai.studio.workspace.dto.WorkspaceRequest;
import com.vivekai.studio.workspace.entity.Folder;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.FolderRepository;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public List<Workspace> listWorkspacesForUser(UUID userId) {
        log.info("Fetching workspaces list for user ID: {}", userId);
        return workspaceRepository.findByOwnerId(userId);
    }

    @Transactional
    public Workspace createWorkspace(WorkspaceRequest request, UUID userId) {
        log.info("Creating new workspace '{}' for user: {}", request.getName(), userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        return workspaceRepository.save(workspace);
    }

    @Transactional
    public Folder createFolder(UUID workspaceId, FolderRequest request, UUID userId) {
        log.info("Creating folder '{}' in workspace ID: {} for user: {}", request.getName(), workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        if (!workspace.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to access this workspace");
        }

        Folder folder = Folder.builder()
                .name(request.getName())
                .workspace(workspace)
                .build();

        return folderRepository.save(folder);
    }

    public List<Folder> listFolders(UUID workspaceId, UUID userId) {
        log.info("Fetching folders in workspace ID: {} for user: {}", workspaceId, userId);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with ID: " + workspaceId));

        if (!workspace.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("User is not authorized to access this workspace");
        }

        return folderRepository.findByWorkspaceId(workspaceId);
    }
}
