package br.com.alurafood.pagamentos.controller;

import br.com.alurafood.pagamentos.dto.PagamentoComPedidoDto;
import br.com.alurafood.pagamentos.dto.PagamentoDto;
import br.com.alurafood.pagamentos.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    @GetMapping
    public Page<PagamentoDto> obterTodos(Pageable pageable) {
        return service.obterTodos(pageable);
    }

    @GetMapping("{id}")
    public PagamentoComPedidoDto obterPorId(@PathVariable Long id) {
        return service.obterPagamentoComItensDoPedido(id);
    }

    @PostMapping
    public ResponseEntity<PagamentoDto> criar(@RequestBody @Valid PagamentoDto pagamentoDto, UriComponentsBuilder uriComponentsBuilder) {
        PagamentoDto pagamento = service.criarPagamento(pagamentoDto);
        URI uri = uriComponentsBuilder.path("/pagamentos/{id}").buildAndExpand(pagamento.getId()).toUri();
        return ResponseEntity.created(uri).body(pagamento);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public PagamentoDto atualizar(@PathVariable Long id, @RequestBody @Valid PagamentoDto pagamentoDto) {
        return service.atualizarPagamento(id, pagamentoDto);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluirPagamento(id);
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizaPedido", fallbackMethod = "pagamentoAutorizadoComIntegracaoPendente")
    public void confirmarPagamento(@PathVariable("id") Long pagamentoId) {
        service.confirmarPagamento(pagamentoId);
    }

    public void pagamentoAutorizadoComIntegracaoPendente(Long id, Exception e){
        service.alteraStatus(id);
    }
}
