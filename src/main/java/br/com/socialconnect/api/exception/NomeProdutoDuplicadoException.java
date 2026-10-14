package br.com.socialconnect.api.exception;

public class NomeProdutoDuplicadoException extends RuntimeException {

    private final String nome;

    public NomeProdutoDuplicadoException(String nome) {
        super("Produto já cadastrado: " + nome);
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
