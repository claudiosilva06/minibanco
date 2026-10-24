package br.com.claudio.minibanco.dto;

import br.com.claudio.minibanco.model.Conta;

import java.math.BigDecimal;

public record ContaResponse(Long id, String titular, BigDecimal saldo) {

    public static ContaResponse de(Conta conta) {
        return new ContaResponse(conta.getId(), conta.getTitular(), conta.getSaldo());
    }
}
