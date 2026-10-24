package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.Movimentacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    Page<Movimentacao> findByContaId(Long contaId, Pageable pageable);
}
