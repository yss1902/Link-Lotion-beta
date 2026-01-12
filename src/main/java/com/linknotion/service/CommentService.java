package com.linknotion.service;

import com.linknotion.entity.Comment;
import com.linknotion.entity.Document;
import com.linknotion.entity.User;
import com.linknotion.repository.CommentRepository;
import com.linknotion.repository.DocumentRepository;
import com.linknotion.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, DocumentRepository documentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    public List<Comment> getComments(Document document) {
        return commentRepository.findByDocumentOrderByCreatedAtDesc(document);
    }

    public void addComment(String uuid, String username, String content) {
        Document document = documentRepository.findByUuid(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(user);
        comment.setDocument(document);
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}
