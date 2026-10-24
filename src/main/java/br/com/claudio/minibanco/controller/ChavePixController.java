package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.CadastrarChavePixRequest;
import br.com.claudio.minibanco.dto.ChavePixResponse;
import br.com.claudio.minibanco.service.PixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Chaves Pix", description = "Cadastro das chaves Pix de uma conta")
@RestController
@RequestMapping("/contas/{contaId}/chaves-pix")
public class ChavePixController {

    private final PixService pixService;

    public ChavePixController(PixService pixService) {
        this.pixService = pixService;
    }

    @Operation(summary = "Cadastra uma chave (CPF, EMAIL, TELEFONE ou ALEATORIA)")
    @PostMapping
    public ResponseEntity<ChavePixResponse> cadastrar(@PathVariable Long contaId,
                                                      @RequestBody @Valid CadastrarChavePixRequest request) {
        ChavePixResponse chave = ChavePixResponse.de(
                pixService.cadastrarChave(contaId, request.tipo(), request.chave()));
        return ResponseEntity.status(HttpStatus.CREATED).body(chave);
    }

    @Operation(summary = "Lista as chaves da conta")
    @GetMapping
    public ResponseEntity<List<ChavePixResponse>> listar(@PathVariable Long contaId) {
        return ResponseEntity.ok(pixService.listarChaves(contaId).stream().map(ChavePixResponse::de).toList());
    }

    @Operation(summary = "Remove uma chave da conta")
    @DeleteMapping("/{chave}")
    public ResponseEntity<Void> remover(@PathVariable Long contaId, @PathVariable String chave) {
        pixService.removerChave(contaId, chave);
        return ResponseEntity.noContent().build();
    }
}
