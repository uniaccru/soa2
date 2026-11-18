package org.example.dragon.resource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** Корень "/" — пути эндпоинтов в точности как в спецификации (/dragons, /teams). */
@ApplicationPath("/")
public class RestApplication extends Application {
}
