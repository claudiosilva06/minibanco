package br.com.claudio.minibanco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PixControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarListarERemoverChaves() throws Exception {
        long conta = criarConta("Ana");

        postJson("/contas/" + conta + "/chaves-pix", "{\"tipo\": \"CPF\", \"chave\": \"390.533.447-05\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chave").value("39053344705"))
                .andExpect(jsonPath("$.tipo").value("CPF"));
        postJson("/contas/" + conta + "/chaves-pix", "{\"tipo\": \"ALEATORIA\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.chave").value(matchesPattern("[0-9a-f-]{36}")));

        mockMvc.perform(get("/contas/" + conta + "/chaves-pix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(delete("/contas/" + conta + "/chaves-pix/39053344705"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/contas/" + conta + "/chaves-pix"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deveRetornar409ParaChaveRepetida() throws Exception {
        long conta = criarConta("Ana");
        String corpo = "{\"tipo\": \"CPF\", \"chave\": \"12345678909\"}";
        postJson("/contas/" + conta + "/chaves-pix", corpo).andExpect(status().isCreated());

        postJson("/contas/" + conta + "/chaves-pix", corpo)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("A chave Pix 12345678909 já está cadastrada"));
    }

    @Test
    void deveRetornar400ParaChaveInvalidaOuTipoDesconhecido() throws Exception {
        long conta = criarConta("Ana");

        postJson("/contas/" + conta + "/chaves-pix", "{\"tipo\": \"CPF\", \"chave\": \"123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("CPF inválido"));
        postJson("/contas/" + conta + "/chaves-pix", "{\"tipo\": \"CNPJ\", \"chave\": \"123\"}")
                .andExpect(status().isBadRequest());
        postJson("/contas/" + conta + "/chaves-pix", "{\"chave\": \"a@b.com\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O tipo da chave é obrigatório"));
    }

    @Test
    void deveConsultarChaveEPagarPix() throws Exception {
        long pagador = criarConta("Mano");
        long recebedor = criarConta("Bia Costa");
        postJson("/contas/" + pagador + "/deposito", "{\"valor\": 100}");
        postJson("/contas/" + recebedor + "/chaves-pix", "{\"tipo\": \"TELEFONE\", \"chave\": \"+5521999990000\"}");

        mockMvc.perform(get("/pix/chaves/+5521999990000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titular").value("Bia Costa"))
                .andExpect(jsonPath("$.tipo").value("TELEFONE"))
                .andExpect(jsonPath("$.saldo").doesNotExist());

        postJson("/pix", "{\"origemId\": " + pagador + ", \"chave\": \"+5521999990000\", \"valor\": 20}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recebedor").value("Bia Costa"))
                .andExpect(jsonPath("$.valor").value(20));

        mockMvc.perform(get("/contas/" + recebedor + "/extrato"))
                .andExpect(jsonPath("$.content[0].tipo").value("PIX_RECEBIDO"));
    }

    @Test
    void deveRetornar404ParaChaveInexistente() throws Exception {
        mockMvc.perform(get("/pix/chaves/ninguem@email.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Chave Pix ninguem@email.com não encontrada"));
    }

    private long criarConta(String titular) throws Exception {
        String resposta = postJson("/contas", "{\"titular\": \"" + titular + "\"}")
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    private ResultActions postJson(String url, String corpo) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
