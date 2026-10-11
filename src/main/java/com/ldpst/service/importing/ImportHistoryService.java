package com.ldpst.service.importing;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ldpst.model.domain.ImportOperation;
import com.ldpst.model.domain.ImportStatus;
import com.ldpst.repository.ImportOperationRepository;
import com.ldpst.service.util.NotFoundException;

@Service
public class ImportHistoryService {
    private final ImportOperationRepository repository;

    public ImportHistoryService(ImportOperationRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long start(String username, String fileName) {
        ImportOperation operation = new ImportOperation();
        operation.setStatus(ImportStatus.IN_PROGRESS);
        operation.setUsername(username);
        operation.setFileName(fileName);
        return repository.save(operation).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeSuccessfully(long id, int count) {
        ImportOperation operation = get(id);
        operation.setStatus(ImportStatus.SUCCESS);
        operation.setImportedCount(count);
        operation.setCompletedAt(Instant.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeWithError(long id, String message) {
        ImportOperation operation = get(id);
        operation.setStatus(ImportStatus.FAILED);
        operation.setImportedCount(null);
        operation.setErrorMessage(limit(message));
        operation.setCompletedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ImportOperation> history(String username, boolean admin) {
        return admin
                ? repository.findAllByOrderByIdDesc()
                : repository.findByUsernameIgnoreCaseOrderByIdDesc(username);
    }

    private ImportOperation get(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Операция импорта не найдена"));
    }

    private String limit(String message) {
        String value = message == null ? "Неизвестная ошибка" : message;
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
