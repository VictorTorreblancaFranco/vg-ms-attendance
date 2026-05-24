package com.vg.task.web.router;

import com.vg.task.web.handler.EducationalResourceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EducationalResourceRouter {
    private static final String API_V1 = "/api/v1/educational-resources";

    @Bean
    public RouterFunction<ServerResponse> educationalResourceRoutes(EducationalResourceHandler handler) {
        return route(GET(API_V1), handler::findAll)
                .andRoute(GET(API_V1 + "/subject/{subjectId}"), handler::findBySubjectId)
                .andRoute(GET(API_V1 + "/grade/{gradeId}"), handler::findByGradeId)
                .andRoute(GET(API_V1 + "/public"), handler::findPublic)
                .andRoute(GET(API_V1 + "/{id}"), handler::findById)
                // POST con JSON (sin archivo)
                .andRoute(POST(API_V1 + "/save"), handler::saveJson)
                // POST con archivo (multipart)
                .andRoute(POST(API_V1 + "/save-with-file"), handler::saveWithFile)
                .andRoute(PUT(API_V1 + "/{id}"), handler::update)
                .andRoute(DELETE(API_V1 + "/{id}"), handler::delete);
    }
}
