package com.vg.task.web.router;

import com.vg.task.web.handler.AttendanceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AttendanceRouter {
    private static final String API_V1 = "/api/v1/attendance";

    @Bean
    public RouterFunction<ServerResponse> attendanceRoutes(AttendanceHandler handler) {
        return route(GET(API_V1 + "/class/{classId}"), handler::findByClassId)
                .andRoute(GET(API_V1 + "/class/{classId}/date/{date}"), handler::findByClassIdAndDate)
                .andRoute(GET(API_V1 + "/student/{studentId}"), handler::findByStudentId)
                .andRoute(POST(API_V1 + "/save"), handler::save)
                .andRoute(PUT(API_V1 + "/{id}"), handler::update)
                .andRoute(DELETE(API_V1 + "/{id}"), handler::delete)
                .andRoute(POST(API_V1 + "/bulk-upload"), handler::bulkUpload)
                .andRoute(GET(API_V1 + "/export/{classId}/{date}"), handler::exportToExcel)
                .andRoute(GET(API_V1 + "/template"), handler::getTemplate);
    }
}
