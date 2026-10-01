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

@Service
public class TransferenciaService {

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public TransferenciaService(ContaRepository contaRepository,
                                MovimentacaoRepository movimentacaoRepository) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional
    public void transferir(Long origemId, Long destinoId, BigDecimal valor) {
        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta");
        }

        Conta origem = contaRepository.findById(origemId)
                .orElseThrow(() -> new ContaNaoEncontradaException(origemId));
        Conta destino = contaRepository.findById(destinoId)
                .orElseThrow(() -> new ContaNaoEncontradaException(destinoId));

        origem.sacar(valor);
        destino.depositar(valor);

        movimentacaoRepository.save(new Movimentacao(origem, TipoMovimentacao.TRANSFERENCIA_ENVIADA, valor));
        movimentacaoRepository.save(new Movimentacao(destino, TipoMovimentacao.TRANSFERENCIA_RECEBIDA, valor));
    }
}