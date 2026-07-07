package com.vivekai.studio.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderRequest {

    @NotBlank(message = "Folder name is required")
    @Size(max = 100, message = "Folder name must be under 100 characters")
    private String name;
}
