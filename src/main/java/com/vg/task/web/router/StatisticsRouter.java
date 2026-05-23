package com.vg.task.web.router;

import com.vg.task.web.handler.StatisticsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class StatisticsRouter {

    private static final String API_V1 = "/api/v1/statistics";

    @Bean
    public RouterFunction<ServerResponse> statisticsRoutes(StatisticsHandler handler) {
        return route(GET(API_V1 + "/hardest-tasks"), handler::getHardestTasks)
            .andRoute(GET(API_V1 + "/teacher/{teacherId}"), handler::getTeacherStats)
            .andRoute(GET(API_V1 + "/student/{studentId}"), handler::getStudentSummary)
            .andRoute(GET(API_V1 + "/pending-grading"), handler::getPendingGrading)
            .andRoute(GET(API_V1 + "/transcript/{studentId}"), handler::exportTranscript);
    }
}
