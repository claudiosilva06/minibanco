package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.ChavePix;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChavePixRepository extends JpaRepository<ChavePix, Long> {

    @EntityGraph(attributePaths = "conta")
    Optional<ChavePix> findByChave(String chave);

    List<ChavePix> findByContaIdOrderById(Long contaId);

    boolean existsByChave(String chave);

    long countByContaId(Long contaId);
}
