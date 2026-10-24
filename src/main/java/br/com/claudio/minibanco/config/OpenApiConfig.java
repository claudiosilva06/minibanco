package br.com.claudio.minibanco.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minibancoOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Mini Banco Digital")
                .description("API REST de um banco digital simplificado: contas, depósitos, saques, "
                        + "transferências, Pix e extrato.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Claudio Augusto Silva de Assis")
                        .url("https://github.com/claudiosilva06/minibanco")));
    }
}
