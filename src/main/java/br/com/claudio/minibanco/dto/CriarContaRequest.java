package br.com.claudio.minibanco.dto;

import jakarta.validation.constraints.NotBlank;

public record CriarContaRequest(
        @NotBlank(message = "O nome do titular é obrigatório")
        String titular
) {
}
