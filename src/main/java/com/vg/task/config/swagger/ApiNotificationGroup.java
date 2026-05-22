package com.vg.task.config.swagger;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag(name = "Notificaciones", description = "Sistema de notificaciones internas")
public @interface ApiNotificationGroup {
}
