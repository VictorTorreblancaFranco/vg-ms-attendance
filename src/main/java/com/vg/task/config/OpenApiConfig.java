package com.vg.task.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.List;

@Configuration
public class OpenApiConfig implements WebFluxConfigurer {

    @Bean
    public OpenAPI taskMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("📚 API de Tareas - Valle Grande")
                        .description("""
                                ## Microservicio Reactivo para Gestión de Tareas Académicas
                                
                                ### 🎯 Características Principales:
                                * **Gestión de Tareas** - CRUD completo con estados (borrador/publicado/cerrado)
                                * **Entregas de Estudiantes** - Sistema de entregas con reintentos y justificaciones
                                * **Calificaciones** - Calificación simple y por rúbricas
                                * **Dashboard** - Estadísticas en tiempo real
                                * **Notificaciones** - Sistema interno de notificaciones
                                * **Archivos** - Subida a Cloudinary con validación
                                * **Rate Limiting** - 10 peticiones por segundo por IP
                                * **Resiliencia** - Circuit Breaker y Retry con Resilience4j
                                
                                ### 🚀 Tecnologías:
                                - Spring Boot 3.4.5 + WebFlux (Reactivo)
                                - R2DBC + PostgreSQL
                                - Resilience4j (Circuit Breaker)
                                - Cloudinary (Almacenamiento)
                                - OpenAPI 3.0 + Swagger UI
                                """)
                        .version("v2.0.0")
                        .contact(new Contact()
                                .name("Victor Torreblanca")
                                .email("victor.torreblanca@vallegrande.edu.pe")
                                .url("https://github.com/victortorreblanca"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8085")
                                .description("Servidor Local"),
                        new Server()
                                .url("https://api-vallegrande.edu.pe/task")
                                .description("Servidor de Producción")))
                .tags(List.of(
                        new Tag().name("Tareas").description("Operaciones CRUD para tareas académicas"),
                        new Tag().name("Entregas").description("Gestión de entregas de estudiantes"),
                        new Tag().name("Dashboard").description("Estadísticas y métricas"),
                        new Tag().name("Notificaciones").description("Sistema de notificaciones"),
                        new Tag().name("Rúbricas").description("Gestión de rúbricas de evaluación"),
                        new Tag().name("Archivos").description("Subida y gestión de archivos")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentación completa en Wiki")
                        .url("https://github.com/victortorreblanca/vg-ms-task/wiki"));
    }
}
