package com.vivekai.studio.workspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.security.service.UserDetailsImpl;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.workspace.dto.FolderRequest;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import com.vivekai.studio.workspace.service.WorkspaceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WorkspaceAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Test
    public void createFolder_InOthersWorkspace_Forbidden() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        User owner = User.builder().username("owner").build();
        owner.setId(ownerId);

        Workspace workspace = Workspace.builder()
                .name("Owner's Workspace")
                .owner(owner)
                .build();
        workspace.setId(workspaceId);

        Mockito.when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        FolderRequest request = new FolderRequest();
        request.setName("Attacker Folder");

        // Mock authenticated user details (the attacker)
        UserDetailsImpl attackerDetails = new UserDetailsImpl(
                attackerId,
                "attacker",
                "attacker@vivekai.com",
                "password",
                com.vivekai.studio.user.entity.UserStatus.ACTIVE,
                false,
                false,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(post("/api/v1/workspaces/" + workspaceId + "/folders")
                        .with(SecurityMockMvcRequestPostProcessors.user(attackerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized to access this workspace"));
    }

    @Test
    public void listFolders_InOthersWorkspace_Forbidden() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID workspaceId = UUID.randomUUID();

        User owner = User.builder().username("owner").build();
        owner.setId(ownerId);

        Workspace workspace = Workspace.builder()
                .name("Owner's Workspace")
                .owner(owner)
                .build();
        workspace.setId(workspaceId);

        Mockito.when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));

        UserDetailsImpl attackerDetails = new UserDetailsImpl(
                attackerId,
                "attacker",
                "attacker@vivekai.com",
                "password",
                com.vivekai.studio.user.entity.UserStatus.ACTIVE,
                false,
                false,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(get("/api/v1/workspaces/" + workspaceId + "/folders")
                        .with(SecurityMockMvcRequestPostProcessors.user(attackerDetails)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized to access this workspace"));
    }
}
