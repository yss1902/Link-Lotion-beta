package com.linknotion.repository;

import com.linknotion.entity.Document;
import com.linknotion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByOwnerOrderByCreatedAtDesc(User owner);
    List<Document> findAllByOwnerAndParentIsNullOrderByCreatedAtDesc(User owner);
    Optional<Document> findByUuid(String uuid);
}
