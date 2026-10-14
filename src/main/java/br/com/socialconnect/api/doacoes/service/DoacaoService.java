package br.com.socialconnect.api.doacoes.service;

import br.com.socialconnect.api.doacoes.dto.DoacaoPatchDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoRequestDTO;
import br.com.socialconnect.api.doacoes.dto.DoacaoResponseDTO;
import br.com.socialconnect.api.doacoes.model.Doacao;
import br.com.socialconnect.api.doacoes.model.TipoDoacao;
import br.com.socialconnect.api.doacoes.repository.DoacaoRepository;
import br.com.socialconnect.api.doacoes.repository.DoacaoSpecifications;
import br.com.socialconnect.api.doadores.model.Doador;
import br.com.socialconnect.api.doadores.repository.DoadorRepository;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final DoadorRepository doadorRepository;

    public DoacaoService(DoacaoRepository doacaoRepository, DoadorRepository doadorRepository) {
        this.doacaoRepository = doacaoRepository;
        this.doadorRepository = doadorRepository;
    }

    @Transactional(readOnly = true)
    public Page<DoacaoResponseDTO> listar(LocalDate dataInicio, LocalDate dataFim, TipoDoacao tipo, Pageable pageable) {
        return doacaoRepository
                .findAll(DoacaoSpecifications.filtrar(dataInicio, dataFim, tipo), pageable)
                .map(this::montarResposta);
    }

    @Transactional(readOnly = true)
    public DoacaoResponseDTO buscarPorId(Long idDoacao) {
        return montarResposta(obterCadastro(idDoacao));
    }

    @Transactional
    public DoacaoResponseDTO criar(DoacaoRequestDTO dados) {
        Doador doador = obterDoador(dados.idDoador());
        Doacao doacao = Doacao.builder()
                .doador(doador)
                .dataDoacao(dados.dataDoacao())
                .valor(dados.valor())
                .tipo(dados.tipo())
                .descricao(dados.descricao())
                .build();
        return montarResposta(doacaoRepository.save(doacao));
    }

    @Transactional
    public DoacaoResponseDTO atualizar(Long idDoacao, DoacaoRequestDTO dados) {
        Doacao doacao = obterCadastro(idDoacao);
        doacao.setDoador(obterDoador(dados.idDoador()));
        doacao.setDataDoacao(dados.dataDoacao());
        doacao.setValor(dados.valor());
        doacao.setTipo(dados.tipo());
        doacao.setDescricao(dados.descricao());
        return montarResposta(doacaoRepository.save(doacao));
    }

    @Transactional
    public DoacaoResponseDTO atualizarParcial(Long idDoacao, DoacaoPatchDTO dados) {
        Doacao doacao = obterCadastro(idDoacao);
        if (dados.dataDoacao() != null)
            doacao.setDataDoacao(dados.dataDoacao());
        if (dados.valor() != null)
            doacao.setValor(dados.valor());
        if (dados.tipo() != null)
            doacao.setTipo(dados.tipo());
        if (dados.descricao() != null)
            doacao.setDescricao(dados.descricao());
        return montarResposta(doacaoRepository.save(doacao));
    }

    @Transactional
    public void deletar(Long idDoacao) {
        if (!doacaoRepository.existsById(idDoacao)) {
            throw new RecursoNaoEncontradoException("Doação não encontrada: " + idDoacao);
        }
        doacaoRepository.deleteById(idDoacao);
    }

    private Doacao obterCadastro(Long idDoacao) {
        return doacaoRepository.findById(idDoacao)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doação não encontrada: " + idDoacao));
    }

    private Doador obterDoador(Long idDoador) {
        return doadorRepository.findById(idDoador)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Doador não encontrado: " + idDoador));
    }

    private DoacaoResponseDTO montarResposta(Doacao doacao) {
        Doador doador = doacao.getDoador();
        return new DoacaoResponseDTO(
                doacao.getIdDoacao(),
                doador != null ? doador.getIdDoador() : null,
                doador != null ? doador.getNome() : null,
                doacao.getDataDoacao(),
                doacao.getValor(),
                doacao.getTipo(),
                doacao.getDescricao());
    }
}
