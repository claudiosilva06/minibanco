package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.CriarContaRequest;
import br.com.claudio.minibanco.dto.ValorRequest;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.repository.ContaRepository;
import br.com.claudio.minibanco.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaRepository contaRepository;
    private final ContaService contaService;

    public ContaController(ContaRepository contaRepository, ContaService contaService) {
        this.contaRepository = contaRepository;
        this.contaService = contaService;
    }

    @PostMapping
    public ResponseEntity<Conta> criar(@RequestBody @Valid CriarContaRequest request) {
        Conta conta = new Conta(request.titular());
        Conta salva = contaRepository.save(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conta> buscar(@PathVariable Long id) {
        return contaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deposito")
    public ResponseEntity<Conta> depositar(@PathVariable Long id,
                                           @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(contaService.depositar(id, request.valor()));
    }

    @PostMapping("/{id}/saque")
    public ResponseEntity<Conta> sacar(@PathVariable Long id,
                                       @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(contaService.sacar(id, request.valor()));
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<List<Movimentacao>> extrato(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.extrato(id));
    }
}