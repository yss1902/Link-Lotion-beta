package com.linknotion.controller;

import com.linknotion.entity.Document;
import com.linknotion.service.CommentService;
import com.linknotion.service.DocumentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ShareController {

    private final DocumentService documentService;
    private final CommentService commentService;

    public ShareController(DocumentService documentService, CommentService commentService) {
        this.documentService = documentService;
        this.commentService = commentService;
    }

    @GetMapping("/share/{uuid}")
    public String viewSharedDocument(@PathVariable String uuid, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Document document = documentService.getSharedDocument(uuid);
            model.addAttribute("document", document);
            model.addAttribute("comments", commentService.getComments(document));
            if (userDetails != null) {
                model.addAttribute("username", userDetails.getUsername());
            }
            return "shared";
        } catch (SecurityException | IllegalArgumentException e) {
            return "error/404";
        }
    }

    @PostMapping("/share/{uuid}/comment")
    public String addComment(@PathVariable String uuid, @RequestParam String content, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            commentService.addComment(uuid, userDetails.getUsername(), content);
        }
        return "redirect:/share/" + uuid;
    }

    @PostMapping("/share/{uuid}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable String uuid, @PathVariable Long commentId, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            commentService.deleteComment(commentId, userDetails.getUsername());
        }
        return "redirect:/share/" + uuid;
    }
}
