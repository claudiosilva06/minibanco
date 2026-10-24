package br.com.claudio.minibanco.repository;

import br.com.claudio.minibanco.model.Conta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    // Trava a linha da conta (SELECT ... FOR UPDATE) até o fim da transação,
    // para que operações simultâneas na mesma conta sejam feitas uma de cada vez.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.id = :id")
    Optional<Conta> findByIdParaAtualizacao(Long id);
}
