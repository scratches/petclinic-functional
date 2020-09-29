package org.springframework.samples.petclinic.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration(proxyBeanMethods = false)
class WelcomeController {

    @Bean
    public RouterFunction<ServerResponse> ownerRoutes() {
        return RouterFunctions.route()
                .GET("/", request -> ServerResponse.ok().render("welcome")).build();
    }

}
