package com.vg.task.web.router;

import com.vg.task.web.handler.FileUploadHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@Tag(name = "Archivos", description = "Subida y gestión de archivos")
public class FileUploadRouter {

    private static final String API_V1 = "/api/v1/upload";

    @Bean
    @RouterOperation(path = API_V1 + "/task", beanClass = FileUploadHandler.class, beanMethod = "uploadTaskFile", method = RequestMethod.POST,
        operation = @Operation(operationId = "uploadTaskFile", summary = "Subir archivo de tarea",
            description = "Sube un archivo a Cloudinary para adjuntar a una tarea. Formatos permitidos: PDF, DOC, DOCX, JPG, PNG, MP4, etc."))
    public RouterFunction<ServerResponse> fileUploadRoutes(FileUploadHandler handler) {
        return route(POST(API_V1 + "/task"), handler::uploadTaskFile);
    }
}
