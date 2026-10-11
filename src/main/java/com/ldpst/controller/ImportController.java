package com.ldpst.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ldpst.controller.api.ApiMapper;
import com.ldpst.controller.api.ApiModel.ImportOperationDto;
import com.ldpst.controller.api.ApiModel.ImportResultDto;
import com.ldpst.service.importing.ImportHistoryService;
import com.ldpst.service.importing.TicketCsvParser;
import com.ldpst.service.importing.TicketImportService;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final TicketImportService importService;
    private final ImportHistoryService historyService;
    private final ApiMapper mapper;

    public ImportController(TicketImportService importService, ImportHistoryService historyService,
            ApiMapper mapper) {
        this.importService = importService;
        this.historyService = historyService;
        this.mapper = mapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDto> importTickets(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        long operationId = importService.importCsv(file, authentication.getName());
        return ResponseEntity.status(201).body(new ImportResultDto(operationId));
    }

    @GetMapping
    public List<ImportOperationDto> history(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return historyService.history(authentication.getName(), admin).stream()
                .map(mapper::importOperation)
                .toList();
    }

    @GetMapping(value = "/template", produces = "text/csv")
    public ResponseEntity<byte[]> template() {
        String example = TicketCsvParser.HEADER + "\n"
                + "Билет на концерт,10,20,1500.00,USUAL,10,25,GREEN,BROWN,59.9,30.5,Москва,75,"
                + "AB123456,RUSSIA,Концерт,12,Описание концерта,Главная арена,5000,Ленина 1,1234567\n";
        byte[] content = example.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets-template.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content);
    }
}
