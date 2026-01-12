package com.linknotion.controller;

import com.linknotion.entity.Document;
import com.linknotion.service.DocumentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("documents", documentService.getDocumentsByUser(userDetails.getUsername()));
        model.addAttribute("username", userDetails.getUsername());
        return "index";
    }

    @PostMapping("/docs/new")
    public String createDocument(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String title) {
        Document doc = documentService.createDocument(userDetails.getUsername(), title);
        return "redirect:/docs/" + doc.getId() + "/edit";
    }

    @GetMapping("/docs/{id}")
    public String viewDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Document document = documentService.getDocument(id, userDetails.getUsername());
        model.addAttribute("document", document);
        model.addAttribute("documents", documentService.getDocumentsByUser(userDetails.getUsername())); // Sidebar list
        model.addAttribute("username", userDetails.getUsername());
        return "view";
    }

    @GetMapping("/docs/{id}/edit")
    public String editDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        Document document = documentService.getDocument(id, userDetails.getUsername());
        model.addAttribute("document", document);
        model.addAttribute("documents", documentService.getDocumentsByUser(userDetails.getUsername())); // Sidebar list
        model.addAttribute("username", userDetails.getUsername());
        return "editor";
    }

    @PostMapping("/docs/{id}/save")
    public String saveDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String title, @RequestParam String content) {
        documentService.updateDocument(id, userDetails.getUsername(), title, content);
        return "redirect:/docs/" + id;
    }

    @PostMapping("/docs/{id}/delete")
    public String deleteDocument(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        documentService.deleteDocument(id, userDetails.getUsername());
        return "redirect:/";
    }

    @PostMapping("/docs/{id}/share")
    public String toggleShare(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam boolean isShared) {
        documentService.toggleShare(id, userDetails.getUsername(), isShared);
        return "redirect:/docs/" + id;
    }
}
