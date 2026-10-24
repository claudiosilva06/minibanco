package br.com.claudio.minibanco.service;

import br.com.claudio.minibanco.exception.LimiteDiarioExcedidoException;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import br.com.claudio.minibanco.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

/**
 * Limita quanto dinheiro pode sair de uma conta por dia (saques, transferências e Pix somados).
 * Deve ser chamado com a conta já travada, para que duas saídas simultâneas não furem o limite.
 */
@Service
public class LimiteDiarioService {

    private static final Set<TipoMovimentacao> SAIDAS = EnumSet.of(
            TipoMovimentacao.SAQUE,
            TipoMovimentacao.TRANSFERENCIA_ENVIADA,
            TipoMovimentacao.PIX_ENVIADO);

    private final MovimentacaoRepository movimentacaoRepository;
    private final BigDecimal limite;

    public LimiteDiarioService(MovimentacaoRepository movimentacaoRepository,
                               @Value("${minibanco.limite-diario-saida}") BigDecimal limite) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.limite = limite;
    }

    public void validar(Long contaId, BigDecimal valor) {
        BigDecimal usadoHoje = movimentacaoRepository.somarValores(
                contaId, SAIDAS, LocalDate.now().atStartOfDay());
        BigDecimal disponivel = limite.subtract(usadoHoje).max(BigDecimal.ZERO);
        if (valor.compareTo(disponivel) > 0) {
            throw new LimiteDiarioExcedidoException(disponivel);
        }
    }
}
