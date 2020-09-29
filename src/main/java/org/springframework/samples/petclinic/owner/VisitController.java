/*
 * Copyright 2012-2018 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Configuration(proxyBeanMethods = false)
class VisitController {

    private final VisitRepository visits;
    private final PetRepository pets;

    private Validator validator;

    public VisitController(VisitRepository visits, PetRepository pets,
            @Qualifier("mvcValidator") Validator validator) {
        this.visits = visits;
        this.pets = pets;
        this.validator = validator;
    }

    @Bean
    public RouterFunction<ServerResponse> visitRoutes() {
        return RouterFunctions.route().path("/owners", builder -> builder //
                .GET("/*/pets/{petId}/visits/new", this::initNewVisitForm) //
                .POST("/{ownerId}/pets/{petId}/visits/new", this::processNewVisitForm) //
        ).build();
    }

    private void setAllowedFields(WebDataBinder dataBinder) {
        dataBinder.setDisallowedFields("id");
    }

    /**
     * Called by every handler method. 2 goals: - Make sure we always have fresh data -
     * Since we do not use the session scope, make sure that Pet object always has an id
     * (Even though id is not part of the form fields)
     *
     * @param petId
     * @return Pet
     */
    private Visit loadPetWithVisit(int petId, Map<String, Object> model) {
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        Visit visit = new Visit();
        pet.addVisit(visit);
        return visit;
    }

    private ServerResponse initNewVisitForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        Integer petId = Integer.valueOf(request.pathVariable("petId"));
        model.put("petId", petId);
        model.put("visit", loadPetWithVisit(petId, model));
        return ServerResponse.ok().render("pets/createOrUpdateVisitForm", model);
    }

    private ServerResponse processNewVisitForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        Integer petId = Integer.valueOf(request.pathVariable("petId"));
        model.put("petId", petId);
        Visit visit = loadPetWithVisit(petId, model);
        BindingResult result = bindVisit(visit, model, request);
        model.put("ownerId", Integer.valueOf(request.pathVariable("ownerId")));
        if (result.hasErrors()) {
            return ServerResponse.ok().render("pets/createOrUpdateVisitForm", model);
        }
        else {
            this.visits.save(visit);
            return ServerResponse.ok().render("redirect:/owners/{ownerId}", model);
        }
    }

    private BindingResult bindVisit(Visit visit, Map<String, Object> model,
            ServerRequest request) {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(visit, "visit");
        binder.setValidator(validator);
        setAllowedFields(binder);
        binder.bind(request.servletRequest());
        binder.validate();
        BindingResult result = binder.getBindingResult();
        model.put("visit", visit);
        model.put("org.springframework.validation.BindingResult.visit", result);
        return result;
    }

}
