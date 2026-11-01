package com.ldpst.controller;

import org.springframework.web.bind.annotation.RestController;

import com.ldpst.service.TicketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ldpst.controller.api.ApiMapper;
import com.ldpst.controller.api.ApiModel.*;
import com.ldpst.model.domain.Ticket;
import com.ldpst.model.dto.Command.*;



@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final ApiMapper mapper;

    public TicketController(TicketService ticketService, ApiMapper mapper) {
        this.ticketService = ticketService;
        this.mapper = mapper;
    }
    
    @GetMapping 
    public PageDto<TicketDto> list(@RequestParam(defaultValue="0") @Min(0) int page,
       @RequestParam(defaultValue="20") @Min(1) @Max(100) int size,
       @RequestParam(defaultValue="id") String sort, @RequestParam(defaultValue="asc") String direction,
       @RequestParam(required=false) String filterField, @RequestParam(required=false) String filter) {

        PageResult<Ticket> result = ticketService.findAll(new TicketSearch(page, size, sort, !"desc".equalsIgnoreCase(direction), filterField, filter));

        List<TicketDto> list = result.items().stream().map(mapper::ticket).toList();
        
        return new PageDto<TicketDto>(list, result.totalItems(), result.totalPages(), result.page(), result.size());
    }

    @PostMapping 
    public ResponseEntity<TicketDto> create(@Valid @RequestBody TicketRequest body) {
        TicketCommand command = mapper.command(body);
        Ticket ticket = ticketService.create(command);
        TicketDto ticketDto = mapper.ticket(ticket);

        return ResponseEntity.status(201).body(ticketDto);
    }

    @PutMapping("/{id}")
    public TicketDto update(@PathVariable int id, @Valid @RequestBody TicketRequest body) {
        TicketCommand command = mapper.command(body);
        Ticket ticket = ticketService.update(id, command, body.version());
        TicketDto ticketDto = mapper.ticket(ticket);
        return ticketDto;
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id, @RequestParam @Min(0) long version) {
        ticketService.delete(id, version);
    }
}
