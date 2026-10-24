package br.com.claudio.minibanco.dto;

import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoResponse(Long id, TipoMovimentacao tipo, BigDecimal valor, LocalDateTime dataHora) {

    public static MovimentacaoResponse de(Movimentacao movimentacao) {
        return new MovimentacaoResponse(
                movimentacao.getId(),
                movimentacao.getTipo(),
                movimentacao.getValor(),
                movimentacao.getDataHora());
    }
}
