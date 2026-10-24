package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.ContaResponse;
import br.com.claudio.minibanco.dto.CriarContaRequest;
import br.com.claudio.minibanco.dto.MovimentacaoResponse;
import br.com.claudio.minibanco.dto.ValorRequest;
import br.com.claudio.minibanco.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contas", description = "Abertura de conta, depósito, saque e extrato")
@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @Operation(summary = "Abre uma conta com saldo zero")
    @PostMapping
    public ResponseEntity<ContaResponse> criar(@RequestBody @Valid CriarContaRequest request) {
        ContaResponse conta = ContaResponse.de(contaService.criar(request.titular()));
        return ResponseEntity.status(HttpStatus.CREATED).body(conta);
    }

    @Operation(summary = "Consulta uma conta")
    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ContaResponse.de(contaService.buscar(id)));
    }

    @Operation(summary = "Deposita um valor")
    @PostMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(@PathVariable Long id,
                                                   @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(ContaResponse.de(contaService.depositar(id, request.valor())));
    }

    @Operation(summary = "Saca um valor (respeita o saldo e o limite diário)")
    @PostMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(@PathVariable Long id,
                                               @RequestBody @Valid ValorRequest request) {
        return ResponseEntity.ok(ContaResponse.de(contaService.sacar(id, request.valor())));
    }

    @Operation(summary = "Extrato paginado, do mais recente para o mais antigo")
    @GetMapping("/{id}/extrato")
    public ResponseEntity<PagedModel<MovimentacaoResponse>> extrato(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = {"dataHora", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(new PagedModel<>(contaService.extrato(id, pageable).map(MovimentacaoResponse::de)));
    }
}
