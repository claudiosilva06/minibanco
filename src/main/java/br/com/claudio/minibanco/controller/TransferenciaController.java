package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.TransferenciaRequest;
import br.com.claudio.minibanco.service.TransferenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @PostMapping
    public ResponseEntity<Void> transferir(@RequestBody @Valid TransferenciaRequest request) {
        transferenciaService.transferir(request.origemId(), request.destinoId(), request.valor());
        return ResponseEntity.noContent().build();
    }
}