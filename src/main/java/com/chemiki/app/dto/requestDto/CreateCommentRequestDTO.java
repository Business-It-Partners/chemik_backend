package com.chemiki.app.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequestDTO {
    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Comment content is required")
    private String content;
}