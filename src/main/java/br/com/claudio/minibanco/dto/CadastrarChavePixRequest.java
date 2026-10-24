package br.com.claudio.minibanco.dto;

import br.com.claudio.minibanco.model.TipoChavePix;
import jakarta.validation.constraints.NotNull;

public record CadastrarChavePixRequest(
        @NotNull(message = "O tipo da chave é obrigatório")
        TipoChavePix tipo,

        // Não é informada quando o tipo é ALEATORIA: o banco gera a chave.
        String chave
) {
}
