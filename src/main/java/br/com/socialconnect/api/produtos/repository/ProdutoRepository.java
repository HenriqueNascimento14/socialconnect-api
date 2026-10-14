package br.com.socialconnect.api.produtos.repository;

import br.com.socialconnect.api.produtos.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

// JpaSpecificationExecutor permite combinar filtros opcionais com paginação
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // Verificação de unicidade do nome (usado no POST/PUT)
    boolean existsByNomeIgnoreCase(String nome);

    // No PUT, ignora o próprio produto na checagem de unicidade
    boolean existsByNomeIgnoreCaseAndIdProdutoNot(String nome, Long idProduto);
}
