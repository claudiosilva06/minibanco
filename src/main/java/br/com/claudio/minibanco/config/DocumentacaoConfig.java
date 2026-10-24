package br.com.claudio.minibanco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Endereços curtos que abrem o Swagger UI: http://localhost:8080 e http://localhost:8080/minibancoclaudioassis. */
@Configuration
public class DocumentacaoConfig implements WebMvcConfigurer {

    private static final String SWAGGER_UI = "/minibancoclaudioassis/swagger-ui.html";

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", SWAGGER_UI);
        registry.addRedirectViewController("/minibancoclaudioassis", SWAGGER_UI);
        registry.addRedirectViewController("/minibancoclaudioassis/", SWAGGER_UI);
    }
}
