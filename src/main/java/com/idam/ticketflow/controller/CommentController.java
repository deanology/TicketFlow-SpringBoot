package com.idam.ticketflow.controller;

import com.idam.ticketflow.dto.request.CommentCreateRequest;
import com.idam.ticketflow.dto.response.CommentResponse;
import com.idam.ticketflow.security.EmployeeUserDetails;
import com.idam.ticketflow.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal EmployeeUserDetails principal
    ) {
        return commentService.addComment(ticketId, request, principal.getEmployee());
    }
}
