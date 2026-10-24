package br.com.claudio.minibanco.exception;

public class LimiteDeChavesPixException extends RuntimeException {

    public LimiteDeChavesPixException(int limite) {
        super("Cada conta pode ter no máximo " + limite + " chaves Pix");
    }
}
