package com.ldpst.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ldpst.model.domain.ImportOperation;

public interface ImportOperationRepository extends JpaRepository<ImportOperation, Long> {
    List<ImportOperation> findAllByOrderByIdDesc();

    List<ImportOperation> findByUsernameIgnoreCaseOrderByIdDesc(String username);
}
