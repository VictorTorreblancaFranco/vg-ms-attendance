package com.vg.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Tareas - Valle Grande")
                        .description("Microservicio Reactivo para la gestión de tareas, entregas, asistencia y recursos educativos.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Victor Torreblanca")
                                .email("victor@edunova.com")));
    }
}
