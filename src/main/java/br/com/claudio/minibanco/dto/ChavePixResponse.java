package br.com.claudio.minibanco.dto;

import br.com.claudio.minibanco.model.ChavePix;
import br.com.claudio.minibanco.model.TipoChavePix;

public record ChavePixResponse(String chave, TipoChavePix tipo) {

    public static ChavePixResponse de(ChavePix chavePix) {
        return new ChavePixResponse(chavePix.getChave(), chavePix.getTipo());
    }
}
