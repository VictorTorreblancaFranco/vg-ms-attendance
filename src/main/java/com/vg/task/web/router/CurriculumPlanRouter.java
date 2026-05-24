package com.vg.task.web.router;

import com.vg.task.web.handler.CurriculumPlanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CurriculumPlanRouter {
    private static final String API_V1 = "/api/v1/curriculum-plan";

    @Bean
    public RouterFunction<ServerResponse> curriculumPlanRoutes(CurriculumPlanHandler handler) {
        return route(GET(API_V1 + "/class/{classId}"), handler::findByClassId)
                .andRoute(GET(API_V1 + "/{id}"), handler::findById)
                .andRoute(POST(API_V1 + "/save"), handler::save)
                .andRoute(PUT(API_V1 + "/{id}"), handler::update)
                .andRoute(DELETE(API_V1 + "/{id}"), handler::delete);
    }
}
