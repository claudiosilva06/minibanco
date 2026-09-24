package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.CriarContaRequest;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.repository.ContaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaRepository contaRepository;

    public ContaController(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
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
}