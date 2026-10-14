package br.com.socialconnect.api.produtos.dto;

import br.com.socialconnect.api.produtos.model.CategoriaProduto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Produto retornado pela API")
public record ProdutoResponseDTO(
        @Schema(description = "Identificador do produto", example = "1")
        Long idProduto,
        @Schema(example = "Arroz 5kg")
        String nome,
        @Schema(example = "ALIMENTO")
        CategoriaProduto categoria,
        @Schema(example = "3")
        Integer estoqueAtual,
        @Schema(example = "10")
        Integer estoqueMinimo,
        @Schema(example = "unidade")
        String unidadeMedida,
        @Schema(description = "Data de cadastro (gerada pelo servidor)", example = "2026-09-18")
        LocalDate dataCadastro,
        @Schema(description = "true quando estoqueAtual < estoqueMinimo", example = "true")
        boolean estoqueBaixo
) {}
