package com.vg.task.config.swagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag(name = "Rúbricas", description = "Gestión de rúbricas de evaluación")
public @interface ApiRubricGroup {
}
