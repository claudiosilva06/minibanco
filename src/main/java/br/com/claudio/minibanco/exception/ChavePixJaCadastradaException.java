package br.com.claudio.minibanco.exception;

public class ChavePixJaCadastradaException extends RuntimeException {

    public ChavePixJaCadastradaException(String chave) {
        super("A chave Pix " + chave + " já está cadastrada");
    }
}
