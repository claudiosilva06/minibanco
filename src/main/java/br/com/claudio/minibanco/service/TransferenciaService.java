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
    private final LimiteDiarioService limiteDiarioService;

    public TransferenciaService(ContaRepository contaRepository,
                                MovimentacaoRepository movimentacaoRepository,
                                LimiteDiarioService limiteDiarioService) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.limiteDiarioService = limiteDiarioService;
    }

    @Transactional
    public void transferir(Long origemId, Long destinoId, BigDecimal valor) {
        transferir(origemId, destinoId, valor,
                TipoMovimentacao.TRANSFERENCIA_ENVIADA, TipoMovimentacao.TRANSFERENCIA_RECEBIDA);
    }

    /**
     * Move o dinheiro entre as contas e registra uma movimentação em cada uma.
     * Usado também pelo Pix, que muda apenas o tipo das movimentações.
     *
     * @return a movimentação registrada na conta de origem
     */
    @Transactional
    Movimentacao transferir(Long origemId, Long destinoId, BigDecimal valor,
                            TipoMovimentacao tipoEnvio, TipoMovimentacao tipoRecebimento) {
        if (origemId.equals(destinoId)) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta");
        }

        // As contas são travadas sempre na mesma ordem (menor id primeiro). Assim, duas
        // transferências cruzadas (A -> B e B -> A) não ficam esperando uma pela outra (deadlock).
        Conta primeira = travar(Math.min(origemId, destinoId));
        Conta segunda = travar(Math.max(origemId, destinoId));
        Conta origem = primeira.getId().equals(origemId) ? primeira : segunda;
        Conta destino = origem == primeira ? segunda : primeira;

        limiteDiarioService.validar(origemId, valor);
        origem.sacar(valor);
        destino.depositar(valor);

        movimentacaoRepository.save(new Movimentacao(destino, tipoRecebimento, valor));
        return movimentacaoRepository.save(new Movimentacao(origem, tipoEnvio, valor));
    }

    private Conta travar(Long contaId) {
        return contaRepository.findByIdParaAtualizacao(contaId)
                .orElseThrow(() -> new ContaNaoEncontradaException(contaId));
    }
}
