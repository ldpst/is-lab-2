package com.ldpst.service.importing;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ldpst.service.util.BusinessRuleException;

@Service
public class TicketImportService {
    private final ImportHistoryService historyService;
    private final TicketImportTransactionService transactionService;

    public TicketImportService(ImportHistoryService historyService,
            TicketImportTransactionService transactionService) {
        this.historyService = historyService;
        this.transactionService = transactionService;
    }

    public long importCsv(MultipartFile file, String username) {
        String fileName = safeFileName(file.getOriginalFilename());
        long operationId = historyService.start(username, fileName);

        try {
            if (file.isEmpty()) {
                throw new BusinessRuleException("Выбранный CSV-файл пуст");
            }
            int count = transactionService.importTickets(file.getBytes());
            historyService.completeSuccessfully(operationId, count);
            return operationId;
        } catch (IOException exception) {
            historyService.completeWithError(operationId, "Не удалось прочитать загруженный файл");
            throw new BusinessRuleException("Не удалось прочитать загруженный файл");
        } catch (RuntimeException exception) {
            historyService.completeWithError(operationId, rootMessage(exception));
            if (exception instanceof BusinessRuleException businessRuleException) {
                throw businessRuleException;
            }
            throw new BusinessRuleException("Импорт отменён: " + rootMessage(exception));
        }
    }

    private String safeFileName(String fileName) {
        String value = fileName == null || fileName.isBlank() ? "tickets.csv" : fileName;
        value = value.replace('\\', '/');
        value = value.substring(value.lastIndexOf('/') + 1);
        return value.length() <= 255 ? value : value.substring(0, 255);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }
}
