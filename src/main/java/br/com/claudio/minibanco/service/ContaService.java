package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.ContaNaoEncontradaException;
import br.com.claudio.minibanco.model.Conta;
import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import br.com.claudio.minibanco.repository.ContaRepository;
import br.com.claudio.minibanco.repository.MovimentacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public ContaService(ContaRepository contaRepository,
                        MovimentacaoRepository movimentacaoRepository) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional
    public Conta depositar(Long contaId, BigDecimal valor) {
        Conta conta = buscar(contaId);
        conta.depositar(valor);
        movimentacaoRepository.save(new Movimentacao(conta, TipoMovimentacao.DEPOSITO, valor));
        return conta;
    }

    @Transactional
    public Conta sacar(Long contaId, BigDecimal valor) {
        Conta conta = buscar(contaId);
        conta.sacar(valor);
        movimentacaoRepository.save(new Movimentacao(conta, TipoMovimentacao.SAQUE, valor));
        return conta;
    }

    @Transactional(readOnly = true)
    public List<Movimentacao> extrato(Long contaId) {
        buscar(contaId);
        return movimentacaoRepository.findByContaIdOrderByDataHoraDesc(contaId);
    }

    private Conta buscar(Long contaId) {
        return contaRepository.findById(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId));
    }
}