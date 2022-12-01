package br.com.alurafood.pagamentos.service;

import br.com.alurafood.pagamentos.dto.PagamentoComPedidoDto;
import br.com.alurafood.pagamentos.dto.PagamentoDto;
import br.com.alurafood.pagamentos.dto.PedidoItemDto;
import br.com.alurafood.pagamentos.httpclient.PedidoClient;
import br.com.alurafood.pagamentos.model.Pagamento;
import br.com.alurafood.pagamentos.model.Status;
import br.com.alurafood.pagamentos.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PedidoClient pedidoClient;

    public Page<PagamentoDto> obterTodos(Pageable paginacao) {
        return pagamentoRepository.findAll(paginacao)
                .map(p -> modelMapper.map(p, PagamentoDto.class));
    }

    public PagamentoDto obterPorId(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public PagamentoDto criarPagamento(PagamentoDto pagamentoDto) {
        Pagamento pagamento = modelMapper.map(pagamentoDto, Pagamento.class);
        pagamento.criar();
        pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public PagamentoDto atualizarPagamento(Long id, PagamentoDto dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setId(id);
        pagamento = pagamentoRepository.save(pagamento);
        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public void excluirPagamento(Long id) {
        pagamentoRepository.deleteById(id);
    }

    public void confirmarPagamento(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        pagamento.setStatus(Status.CONFIRMADO);
        pagamentoRepository.save(pagamento);

        pedidoClient.atualizaPagamento(pagamento.getPedidoId());
    }

    public void alteraStatus(Long id) {
        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        pagamento.setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        pagamentoRepository.save(pagamento);
    }

    public PagamentoComPedidoDto obterPagamentoComItensDoPedido(Long id) {
        PagamentoDto pagamentoDto = obterPorId(id);
        List<PedidoItemDto> itens = pedidoClient.listarItensDoPedido(pagamentoDto.getPedidoId());

        PagamentoComPedidoDto pagamento = modelMapper.map(pagamentoDto, PagamentoComPedidoDto.class);
        pagamento.setItens(itens);

        return pagamento;
    }
}
