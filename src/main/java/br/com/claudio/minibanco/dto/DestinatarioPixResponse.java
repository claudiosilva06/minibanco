package br.com.claudio.minibanco.dto;

import br.com.claudio.minibanco.model.ChavePix;
import br.com.claudio.minibanco.model.TipoChavePix;

/** O que quem vai pagar vê antes de confirmar o Pix: só o nome do recebedor, sem saldo nem id da conta. */
public record DestinatarioPixResponse(String chave, TipoChavePix tipo, String titular) {

    public static DestinatarioPixResponse de(ChavePix chavePix) {
        return new DestinatarioPixResponse(
                chavePix.getChave(), chavePix.getTipo(), chavePix.getConta().getTitular());
    }
}
