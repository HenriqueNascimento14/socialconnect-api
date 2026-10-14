package br.com.socialconnect.api.produtos.service;

import br.com.socialconnect.api.exception.EstoqueNegativoException;
import br.com.socialconnect.api.exception.NomeProdutoDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import br.com.socialconnect.api.produtos.dto.ProdutoRequestDTO;
import br.com.socialconnect.api.produtos.dto.ProdutoResponseDTO;
import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import br.com.socialconnect.api.produtos.model.Produto;
import br.com.socialconnect.api.produtos.repository.ProdutoRepository;
import br.com.socialconnect.api.produtos.repository.ProdutoSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProdutoServiceImpl implements ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoServiceImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listar(String nome, CategoriaProduto categoria, Pageable pageable) {
        return produtoRepository
                .findAll(ProdutoSpecifications.filtrar(nome, categoria), pageable)
                .map(this::montarResposta);
    }

    @Override
    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return montarResposta(obterCadastro(id));
    }

    @Override
    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dados) {
        verificarEstoque(dados.estoqueAtual());
        String nome = dados.nome().trim();
        if (produtoRepository.existsByNomeIgnoreCase(nome)) {
            throw new NomeProdutoDuplicadoException(nome);
        }
        Produto produto = Produto.builder()
                .nome(nome)
                .categoria(dados.categoria())
                .estoqueAtual(dados.estoqueAtual())
                .estoqueMinimo(dados.estoqueMinimo())
                .unidadeMedida(dados.unidadeMedida())
                .dataCadastro(LocalDate.now())
                .build();
        return montarResposta(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoRequestDTO dados) {
        Produto produto = obterCadastro(id);
        verificarEstoque(dados.estoqueAtual());
        String nome = dados.nome().trim();
        if (produtoRepository.existsByNomeIgnoreCaseAndIdProdutoNot(nome, id)) {
            throw new NomeProdutoDuplicadoException(nome);
        }
        produto.setNome(nome);
        produto.setCategoria(dados.categoria());
        produto.setEstoqueAtual(dados.estoqueAtual());
        produto.setEstoqueMinimo(dados.estoqueMinimo());
        produto.setUnidadeMedida(dados.unidadeMedida());
        return montarResposta(produtoRepository.save(produto));
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Produto não encontrado: " + id);
        }
        produtoRepository.deleteById(id);
    }

    private Produto obterCadastro(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: " + id));
    }

    private void verificarEstoque(Integer estoqueAtual) {
        if (estoqueAtual != null && estoqueAtual < 0) {
            throw new EstoqueNegativoException(estoqueAtual);
        }
    }

    private ProdutoResponseDTO montarResposta(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getIdProduto(), produto.getNome(), produto.getCategoria(),
                produto.getEstoqueAtual(), produto.getEstoqueMinimo(), produto.getUnidadeMedida(),
                produto.getDataCadastro(),
                produto.getEstoqueAtual() < produto.getEstoqueMinimo()
        );
    }
}
