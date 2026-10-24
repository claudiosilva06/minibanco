package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.Movimentacao;
import br.com.claudio.minibanco.model.TipoMovimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    Page<Movimentacao> findByContaId(Long contaId, Pageable pageable);

    @Query("""
            select coalesce(sum(m.valor), 0) from Movimentacao m
            where m.conta.id = :contaId and m.tipo in :tipos and m.dataHora >= :desde
            """)
    BigDecimal somarValores(Long contaId, Collection<TipoMovimentacao> tipos, LocalDateTime desde);
}
