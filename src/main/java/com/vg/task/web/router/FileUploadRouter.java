package com.vg.task.web.router;

import com.vg.task.web.handler.FileUploadHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FileUploadRouter {

    private static final String API_V1 = "/api/v1/upload";

    @Bean
    public RouterFunction<ServerResponse> fileUploadRoutes(FileUploadHandler handler) {
        return route(POST(API_V1 + "/task"), handler::uploadTaskFile);
    }
}
