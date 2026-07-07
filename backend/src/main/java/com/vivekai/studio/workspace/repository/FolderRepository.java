package com.vivekai.studio.workspace.repository;

import com.vivekai.studio.workspace.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    List<Folder> findByWorkspaceId(UUID workspaceId);
}
