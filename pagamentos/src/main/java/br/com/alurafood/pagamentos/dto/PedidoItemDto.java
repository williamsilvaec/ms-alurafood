package br.com.alurafood.pagamentos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PedidoItemDto {

    private Long id;
    private BigDecimal quantidade;
    private String descricao;
}
