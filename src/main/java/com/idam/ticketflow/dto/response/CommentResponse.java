package com.idam.ticketflow.dto.response;

import com.idam.ticketflow.domain.Comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        EmployeeResponse author,
        LocalDateTime createdAt
) {

    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                EmployeeResponse.fromEntity(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }
}
