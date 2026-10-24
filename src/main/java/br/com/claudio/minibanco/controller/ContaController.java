package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.ContaResponse;
import br.com.claudio.minibanco.dto.CriarContaRequest;
import br.com.claudio.minibanco.dto.MovimentacaoResponse;
import br.com.claudio.minibanco.dto.ValorRequest;
import br.com.claudio.minibanco.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@RequestBody @Valid CriarContaRequest request) {
        ContaResponse conta = ContaResponse.de(contaService.criar(request.titular()));
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ContaResponse.de(contaService.buscar(id)));
    }

    @PostMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(@PathVariable Long id,
                                                   @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(ContaResponse.de(contaService.depositar(id, request.valor())));
    }

    @PostMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(@PathVariable Long id,
                                               @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(ContaResponse.de(contaService.sacar(id, request.valor())));
    }

    @GetMapping("/{id}/extrato")
    public ResponseEntity<PagedModel<MovimentacaoResponse>> extrato(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = {"dataHora", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(contaService.extrato(id, pageable).map(MovimentacaoResponse::de)));
    }
}
