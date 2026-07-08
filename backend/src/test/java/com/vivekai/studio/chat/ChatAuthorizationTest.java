package com.vivekai.studio.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivekai.studio.chat.dto.PromptRequest;
import com.vivekai.studio.chat.service.ChatService;
import com.vivekai.studio.security.service.UserDetailsImpl;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.workspace.entity.Workspace;
import com.vivekai.studio.workspace.repository.WorkspaceRepository;
import com.vivekai.studio.user.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChatAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WorkspaceRepository workspaceRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ChatService chatService;

    @Test
    public void sendPrompt_InOthersWorkspace_Forbidden() throws Exception {
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

        User attacker = User.builder().username("attacker").build();
        attacker.setId(attackerId);

        Mockito.when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        Mockito.when(userRepository.findById(attackerId)).thenReturn(Optional.of(attacker));

        PromptRequest request = PromptRequest.builder()
                .prompt("Hello AI")
                .providerCode("GEMINI")
                .modelName("gemini-1.5-flash")
                .build();

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

        mockMvc.perform(post("/api/v1/chat/" + workspaceId + "/send")
                        .with(SecurityMockMvcRequestPostProcessors.user(attackerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User is not authorized to access this workspace"));
    }
}
