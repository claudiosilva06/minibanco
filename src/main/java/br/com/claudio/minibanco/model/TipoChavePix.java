package br.com.claudio.minibanco.model;

import java.util.UUID;

public enum TipoChavePix {

    CPF {
        @Override
        public String normalizar(String chave) {
            String digitos = obrigatoria(chave).replaceAll("[.\\-]", "");
            if (!cpfValido(digitos)) {
                throw new IllegalArgumentException("CPF inválido");
            }
            return digitos;
        }
    },

    EMAIL {
        @Override
        public String normalizar(String chave) {
            String email = obrigatoria(chave).trim().toLowerCase();
            if (email.length() > 77 || !email.matches("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("E-mail inválido");
            }
            return email;
        }
    },

    TELEFONE {
        @Override
        public String normalizar(String chave) {
            String telefone = obrigatoria(chave).replaceAll("[\\s()\\-]", "");
            if (!telefone.matches("^\\+55\\d{10,11}$")) {
                throw new IllegalArgumentException("Telefone inválido. Use o formato +55DDDNÚMERO");
            }
            return telefone;
        }
    },

    ALEATORIA {
        @Override
        public String normalizar(String chave) {
            if (chave != null && !chave.isBlank()) {
                throw new IllegalArgumentException("A chave aleatória é gerada pelo banco e não deve ser informada");
            }
            return UUID.randomUUID().toString();
        }
    };

    /**
     * Valida a chave informada e devolve no formato em que ela é guardada.
     * Para a chave aleatória, gera uma nova.
     */
    public abstract String normalizar(String chave);

    private static String obrigatoria(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("A chave é obrigatória");
        }
        return chave;
    }

    private static boolean cpfValido(String cpf) {
        if (!cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(cpf, 9) == cpf.charAt(9) - '0'
                && digitoVerificador(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int digitoVerificador(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
