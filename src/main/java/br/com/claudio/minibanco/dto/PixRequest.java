package br.com.claudio.minibanco.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PixRequest(
        @NotNull(message = "A conta de origem é obrigatória")
        Long origemId,

        @NotBlank(message = "A chave Pix é obrigatória")
        String chave,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo 2 casas decimais")
        BigDecimal valor
) {
}
