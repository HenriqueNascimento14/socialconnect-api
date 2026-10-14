package br.com.socialconnect.api.produtos.repository;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import org.springframework.data.jpa.domain.Specification;

public final class ProdutoSpecifications {

    private ProdutoSpecifications() {
    }

    public static Specification<Produto> filtrar(String nome, CategoriaProduto categoria) {
        return (root, query, cb) -> {
            var predicado = cb.conjunction();
            if (nome != null && !nome.isBlank()) {
                predicado = cb.and(predicado,
                        cb.like(cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%"));
            }
            if (categoria != null) {
                predicado = cb.and(predicado, cb.equal(root.get("categoria"), categoria));
            }
            return predicado;
        };
    }
}
