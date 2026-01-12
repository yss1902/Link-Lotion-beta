package com.linknotion.repository;

import com.linknotion.entity.Comment;
import com.linknotion.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentOrderByCreatedAtDesc(Document document);
}
