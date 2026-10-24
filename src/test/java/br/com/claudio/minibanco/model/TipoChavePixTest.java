package br.com.claudio.minibanco.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoChavePixTest {

    @Test
    void cpfDeveSerGuardadoSemPontuacao() {
        assertThat(TipoChavePix.CPF.normalizar("529.982.247-25")).isEqualTo("52998224725");
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224726", "11111111111", "1234567890", "abc"})
    void cpfComDigitoErradoOuFormatoInvalidoDeveSerRecusado(String cpf) {
        assertThatThrownBy(() -> TipoChavePix.CPF.normalizar(cpf))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    void emailDeveSerGuardadoEmMinusculas() {
        assertThat(TipoChavePix.EMAIL.normalizar("  Maria.Souza@Email.com ")).isEqualTo("maria.souza@email.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"maria", "maria@", "maria@email", "@email.com"})
    void emailInvalidoDeveSerRecusado(String email) {
        assertThatThrownBy(() -> TipoChavePix.EMAIL.normalizar(email))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void telefoneDeveSerGuardadoSemFormatacao() {
        assertThat(TipoChavePix.TELEFONE.normalizar("+55 (11) 98765-4321")).isEqualTo("+5511987654321");
    }

    @ParameterizedTest
    @ValueSource(strings = {"11987654321", "+1 555 123 4567", "+55119876"})
    void telefoneInvalidoDeveSerRecusado(String telefone) {
        assertThatThrownBy(() -> TipoChavePix.TELEFONE.normalizar(telefone))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chaveAleatoriaDeveSerGeradaPeloBanco() {
        String chave = TipoChavePix.ALEATORIA.normalizar(null);

        assertThat(UUID.fromString(chave)).isNotNull();
        assertThat(TipoChavePix.ALEATORIA.normalizar(null)).isNotEqualTo(chave);
    }

    @Test
    void chaveAleatoriaNaoPodeSerEscolhidaPeloCliente() {
        assertThatThrownBy(() -> TipoChavePix.ALEATORIA.normalizar("minha-chave"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chaveVaziaDeveSerRecusada() {
        assertThatThrownBy(() -> TipoChavePix.EMAIL.normalizar(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A chave é obrigatória");
    }
}
