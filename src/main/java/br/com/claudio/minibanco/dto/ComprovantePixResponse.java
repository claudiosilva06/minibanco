package br.com.claudio.minibanco.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ComprovantePixResponse(
        String chave,
        String recebedor,
        BigDecimal valor,
        LocalDateTime dataHora
) {
}
