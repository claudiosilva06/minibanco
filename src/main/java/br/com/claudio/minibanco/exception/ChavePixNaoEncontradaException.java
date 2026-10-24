package br.com.claudio.minibanco.exception;

public class ChavePixNaoEncontradaException extends RuntimeException {

    public ChavePixNaoEncontradaException(String chave) {
        super("Chave Pix " + chave + " não encontrada");
    }
}
