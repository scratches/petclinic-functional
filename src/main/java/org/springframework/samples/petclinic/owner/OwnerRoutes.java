/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author Dave Syer
 *
 */
@Configuration(proxyBeanMethods = false)
class OwnerRoutes {

    private final OwnerRepository owners;

    public OwnerRoutes(OwnerRepository clinicService) {
        this.owners = clinicService;
    }

    @Bean
    public RouterFunction<ServerResponse> ownerRoutes() {
        return RouterFunctions.route().path("/owners", builder -> builder //
                .GET("", this::processFindForm) //
                .GET("/find", this::initFindForm) //
                .GET("/{ownerId}", this::showOwner)) //
                .build();
    }

    private ServerResponse initFindForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("owner", new Owner());
        return ServerResponse.ok().render("owners/findOwners", model);
    }

    private ServerResponse showOwner(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        model.put("owner",
                this.owners.findById(Integer.valueOf(request.pathVariable("ownerId"))));
        return ServerResponse.ok().render("owners/ownerDetails", model);
    }

    private ServerResponse processFindForm(ServerRequest request) throws Exception {
        Map<String, Object> model = new HashMap<>();
        Owner owner = new Owner();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(owner, "owner");
        binder.bind(request.servletRequest());
        BindingResult result = binder.getBindingResult();
        model.put("owner", owner);
        model.put("org.springframework.validation.BindingResult.owner", result);
        return ServerResponse.ok().render(processFindForm(owner, result, model), model);
    }

    private String processFindForm(Owner owner, BindingResult result,
            Map<String, Object> model) {

        // allow parameterless GET request for /owners to return all records
        if (owner.getLastName() == null) {
            owner.setLastName(""); // empty string signifies broadest possible search
        }

        // find owners by last name
        Collection<Owner> results = this.owners.findByLastName(owner.getLastName());
        if (results.isEmpty()) {
            // no owners found
            result.rejectValue("lastName", "notFound", "not found");
            return "owners/findOwners";
        }
        else if (results.size() == 1) {
            // 1 owner found
            owner = results.iterator().next();
            return "redirect:/owners/" + owner.getId();
        }
        else {
            // multiple owners found
            model.put("selections", results);
            return "owners/ownersList";
        }
    }

}
