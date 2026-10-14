package br.com.socialconnect.api.beneficiarios.service;

import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioPatchDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioRequestDTO;
import br.com.socialconnect.api.beneficiarios.dto.BeneficiarioResponseDTO;
import br.com.socialconnect.api.beneficiarios.model.Beneficiario;
import br.com.socialconnect.api.beneficiarios.repository.BeneficiarioRepository;
import br.com.socialconnect.api.exception.CpfDuplicadoException;
import br.com.socialconnect.api.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BeneficiarioService {

    private final BeneficiarioRepository beneficiarioRepository;

    public BeneficiarioService(BeneficiarioRepository beneficiarioRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
    }

    public Page<BeneficiarioResponseDTO> listar(String nome, String cpf, Pageable pageable) {
        Page<Beneficiario> resultado;
        if (cpf != null && !cpf.isBlank()) {
            resultado = beneficiarioRepository.findByCpf(extrairNumerosCpf(cpf), pageable);
        } else if (nome != null && !nome.isBlank()) {
            resultado = beneficiarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else {
            resultado = beneficiarioRepository.findAll(pageable);
        }
        return resultado.map(this::montarResposta);
    }

    public BeneficiarioResponseDTO buscarPorId(Long idBeneficiario) {
        return montarResposta(obterCadastro(idBeneficiario));
    }

    // Cadastro de beneficiário
    public BeneficiarioResponseDTO criar(BeneficiarioRequestDTO dados) {
        String cpf = extrairNumerosCpf(dados.cpf());
        if (beneficiarioRepository.existsByCpf(cpf)) {
            throw new CpfDuplicadoException(cpf);
        }
        Beneficiario beneficiario = Beneficiario.builder()
                .nome(dados.nome())
                .cpf(cpf)
                .telefone(dados.telefone())
                .endereco(dados.endereco())
                .situacaoVulnerabilidade(dados.situacaoVulnerabilidade())
                .dataCadastro(LocalDate.now())
                .build();
        return montarResposta(beneficiarioRepository.save(beneficiario));
    }

    // Atualização completa dos dados
    public BeneficiarioResponseDTO atualizar(Long idBeneficiario, BeneficiarioRequestDTO dados) {
        Beneficiario beneficiario = obterCadastro(idBeneficiario);
        String cpf = extrairNumerosCpf(dados.cpf());
        if (!beneficiario.getCpf().equals(cpf) && beneficiarioRepository.existsByCpf(cpf)) {
            throw new CpfDuplicadoException(cpf);
        }
        beneficiario.setNome(dados.nome());
        beneficiario.setCpf(cpf);
        beneficiario.setTelefone(dados.telefone());
        beneficiario.setEndereco(dados.endereco());
        beneficiario.setSituacaoVulnerabilidade(dados.situacaoVulnerabilidade());
        return montarResposta(beneficiarioRepository.save(beneficiario));
    }

    // Atualiza somente os campos informados
    public BeneficiarioResponseDTO atualizarParcial(Long idBeneficiario, BeneficiarioPatchDTO dados) {
        Beneficiario beneficiario = obterCadastro(idBeneficiario);
        if (dados.nome() != null)
            beneficiario.setNome(dados.nome());
        if (dados.telefone() != null)
            beneficiario.setTelefone(dados.telefone());
        if (dados.endereco() != null)
            beneficiario.setEndereco(dados.endereco());
        if (dados.situacaoVulnerabilidade() != null)
            beneficiario.setSituacaoVulnerabilidade(dados.situacaoVulnerabilidade());
        return montarResposta(beneficiarioRepository.save(beneficiario));
    }

    public void deletar(Long idBeneficiario) {
        if (!beneficiarioRepository.existsById(idBeneficiario)) {
            throw new RecursoNaoEncontradoException("Beneficiário não encontrado: " + idBeneficiario);
        }
        beneficiarioRepository.deleteById(idBeneficiario);
    }

    private Beneficiario obterCadastro(Long idBeneficiario) {
        return beneficiarioRepository.findById(idBeneficiario)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Beneficiário não encontrado: " + idBeneficiario));
    }

    private String extrairNumerosCpf(String cpf) {
        return cpf.replaceAll("\\D", "");
    }

    private BeneficiarioResponseDTO montarResposta(Beneficiario beneficiario) {
        return new BeneficiarioResponseDTO(
                beneficiario.getIdBeneficiario(), beneficiario.getNome(), beneficiario.getCpf(),
                beneficiario.getTelefone(), beneficiario.getEndereco(),
                beneficiario.getSituacaoVulnerabilidade(), beneficiario.getDataCadastro());
    }
}
