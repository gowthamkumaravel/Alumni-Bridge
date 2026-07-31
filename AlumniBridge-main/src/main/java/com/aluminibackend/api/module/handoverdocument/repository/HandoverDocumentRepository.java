package com.aluminibackend.api.module.handoverdocument.repository;

import com.aluminibackend.api.module.handoverdocument.entity.HandoverDocument;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentStatus;
import com.aluminibackend.api.module.handoverdocument.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HandoverDocumentRepository
        extends JpaRepository<HandoverDocument, Long> {

    List<HandoverDocument> findByHandover_HandoverId(Long handoverId);

    List<HandoverDocument> findByDocumentType(DocumentType documentType);

    List<HandoverDocument> findByStatus(DocumentStatus status);
}