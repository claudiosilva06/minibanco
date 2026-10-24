package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.CadastrarChavePixRequest;
import br.com.claudio.minibanco.dto.ChavePixResponse;
import br.com.claudio.minibanco.service.PixService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contas/{contaId}/chaves-pix")
public class ChavePixController {

    private final PixService pixService;

    public ChavePixController(PixService pixService) {
        this.pixService = pixService;
    }

    @PostMapping
    public ResponseEntity<ChavePixResponse> cadastrar(@PathVariable Long contaId,
                                                      @RequestBody @Valid CadastrarChavePixRequest request) {
        ChavePixResponse chave = ChavePixResponse.de(
                pixService.cadastrarChave(contaId, request.tipo(), request.chave()));
        return ResponseEntity.status(HttpStatus.CREATED).body(chave);
    }

    @GetMapping
    public ResponseEntity<List<ChavePixResponse>> listar(@PathVariable Long contaId) {
        return ResponseEntity.ok(pixService.listarChaves(contaId).stream().map(ChavePixResponse::de).toList());
    }

    @DeleteMapping("/{chave}")
    public ResponseEntity<Void> remover(@PathVariable Long contaId, @PathVariable String chave) {
        pixService.removerChave(contaId, chave);
        return ResponseEntity.noContent().build();
    }
}
