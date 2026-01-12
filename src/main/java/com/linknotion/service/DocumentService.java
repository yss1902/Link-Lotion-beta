package com.linknotion.service;

import com.linknotion.entity.Document;
import com.linknotion.entity.User;
import com.linknotion.repository.DocumentRepository;
import com.linknotion.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public List<Document> getDocumentsByUser(String username) {
        User user = getUser(username);
        return documentRepository.findAllByOwnerOrderByCreatedAtDesc(user);
    }

    public Document createDocument(String username, String title) {
        User user = getUser(username);
        Document document = new Document();
        document.setTitle(title);
        document.setOwner(user);
        document.setContent(""); // Start empty
        document.setUuid(UUID.randomUUID().toString()); // Generate UUID
        return documentRepository.save(document);
    }

    public Document getDocument(Long id, String username) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (!document.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Access denied");
        }
        return document;
    }

    public void updateDocument(Long id, String username, String title, String content) {
        Document document = getDocument(id, username);
        document.setTitle(title);
        document.setContent(content);
        documentRepository.save(document);
    }

    public void deleteDocument(Long id, String username) {
        Document document = getDocument(id, username);
        documentRepository.delete(document);
    }

    public Document toggleShare(Long id, String username, boolean isShared) {
        Document document = getDocument(id, username);
        document.setShared(isShared);
        // Ensure UUID exists if sharing is turned on (though it's created on init now, good to be safe)
        if (isShared && document.getUuid() == null) {
            document.setUuid(UUID.randomUUID().toString());
        }
        return documentRepository.save(document);
    }

    public Document getSharedDocument(String uuid) {
        Document document = documentRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (!document.isShared()) {
            throw new SecurityException("Document is not shared");
        }
        return document;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
