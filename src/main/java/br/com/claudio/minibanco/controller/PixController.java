package br.com.claudio.minibanco.controller;

import br.com.claudio.minibanco.dto.ComprovantePixResponse;
import br.com.claudio.minibanco.dto.DestinatarioPixResponse;
import br.com.claudio.minibanco.dto.PixRequest;
import br.com.claudio.minibanco.service.PixService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Pix", description = "Pagamento por chave Pix")
@RestController
@RequestMapping("/pix")
public class PixController {

    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @Operation(summary = "Mostra o nome do recebedor antes de pagar")
    @GetMapping("/chaves/{chave}")
    public ResponseEntity<DestinatarioPixResponse> consultar(@PathVariable String chave) {
        return ResponseEntity.ok(DestinatarioPixResponse.de(pixService.consultar(chave)));
    }

    @Operation(summary = "Faz um Pix e devolve o comprovante")
    @PostMapping
    public ResponseEntity<ComprovantePixResponse> pagar(@RequestBody @Valid PixRequest request) {
        return ResponseEntity.ok(pixService.pagar(request.origemId(), request.chave(), request.valor()));
    }
}
