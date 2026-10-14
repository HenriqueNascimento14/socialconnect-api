package br.com.socialconnect.api.exception;

public class EstoqueNegativoException extends RuntimeException {

    private final Integer estoque;

    public EstoqueNegativoException(Integer estoque) {
        super("Estoque não pode ser negativo: " + estoque);
        this.estoque = estoque;
    }

    public Integer getEstoque() {
        return estoque;
    }
}
