package br.com.claudio.minibanco.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentacaoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveExporAEspecificacaoOpenApiComTodosOsEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Mini Banco Digital"))
                .andExpect(jsonPath("$.paths['/contas']").exists())
                .andExpect(jsonPath("$.paths['/contas/{id}/extrato'].get.parameters[?(@.name == 'page')]").exists())
                .andExpect(jsonPath("$.paths['/transferencias']").exists())
                .andExpect(jsonPath("$.paths['/contas/{contaId}/chaves-pix']").exists())
                .andExpect(jsonPath("$.paths['/pix']").exists());
    }

    @Test
    void enderecosCurtosDevemLevarAoSwagger() throws Exception {
        for (String endereco : new String[]{"/", "/minibancoclaudioassis", "/minibancoclaudioassis/"}) {
            mockMvc.perform(get(endereco))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/minibancoclaudioassis/swagger-ui.html"));
        }
    }
}
