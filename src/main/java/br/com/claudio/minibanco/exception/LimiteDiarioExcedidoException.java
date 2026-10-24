package br.com.claudio.minibanco.exception;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LimiteDiarioExcedidoException extends RuntimeException {

    public LimiteDiarioExcedidoException(BigDecimal disponivel) {
        super("Limite diário de saídas excedido. Disponível hoje: R$ " + disponivel.setScale(2, RoundingMode.HALF_EVEN));
    }
}
