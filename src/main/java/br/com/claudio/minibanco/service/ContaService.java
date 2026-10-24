package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.ContaNaoEncontradaException;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import br.com.claudio.minibanco.repository.ContaRepository;
import br.com.claudio.minibanco.repository.MovimentacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final LimiteDiarioService limiteDiarioService;

    public ContaService(ContaRepository contaRepository,
                        MovimentacaoRepository movimentacaoRepository,
                        LimiteDiarioService limiteDiarioService) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.limiteDiarioService = limiteDiarioService;
    }

    @Transactional
    public Conta criar(String titular) {
        return contaRepository.save(new Conta(titular));
    }

    @Transactional(readOnly = true)
    public Conta buscar(Long contaId) {
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId));
    }

    @Transactional
    public Conta depositar(Long contaId, BigDecimal valor) {
        Conta conta = buscarParaAtualizacao(contaId);
        conta.depositar(valor);
        movimentacaoRepository.save(new Movimentacao(conta, TipoMovimentacao.DEPOSITO, valor));
        return conta;
    }

    @Transactional
    public Conta sacar(Long contaId, BigDecimal valor) {
        Conta conta = buscarParaAtualizacao(contaId);
        limiteDiarioService.validar(contaId, valor);
        conta.sacar(valor);
        movimentacaoRepository.save(new Movimentacao(conta, TipoMovimentacao.SAQUE, valor));
        return conta;
    }

    @Transactional(readOnly = true)
    public Page<Movimentacao> extrato(Long contaId, Pageable pageable) {
        buscar(contaId);
        return movimentacaoRepository.findByContaId(contaId, pageable);
    }

    private Conta buscarParaAtualizacao(Long contaId) {
        return contaRepository.findByIdParaAtualizacao(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId));
    }
}
