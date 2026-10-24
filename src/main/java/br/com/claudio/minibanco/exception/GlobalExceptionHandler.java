package br.com.claudio.minibanco.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, String>> tratarSaldoInsuficiente(SaldoInsuficienteException e) {
        return ResponseEntity.status(422).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(LimiteDiarioExcedidoException.class)
    public ResponseEntity<Map<String, String>> tratarLimiteDiario(LimiteDiarioExcedidoException e) {
        return ResponseEntity.status(422).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> tratarValorInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarCorpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Corpo da requisição inválido"));
    }

    @ExceptionHandler({ContaNaoEncontradaException.class, ChavePixNaoEncontradaException.class})
    public ResponseEntity<Map<String, String>> tratarNaoEncontrado(RuntimeException e) {
        return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ChavePixJaCadastradaException.class)
    public ResponseEntity<Map<String, String>> tratarChaveDuplicada(ChavePixJaCadastradaException e) {
        return ResponseEntity.status(409).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(LimiteDeChavesPixException.class)
    public ResponseEntity<Map<String, String>> tratarLimiteDeChaves(LimiteDeChavesPixException e) {
        return ResponseEntity.status(422).body(Map.of("erro", e.getMessage()));
    }

    // Duas requisições cadastrando a mesma chave ao mesmo tempo: a segunda esbarra na restrição unique.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> tratarConflito(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Map.of("erro", "O registro conflita com um dado já existente"));
    }

}
