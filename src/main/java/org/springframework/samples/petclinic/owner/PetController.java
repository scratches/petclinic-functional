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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Configuration(proxyBeanMethods = false)
class PetController {

    private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";
    private final PetRepository pets;
    private final OwnerRepository owners;
    private FormattingConversionService conversionService;

    public PetController(PetRepository pets, OwnerRepository owners,
            @Qualifier("mvcConversionService") FormattingConversionService conversionService) {
        this.pets = pets;
        this.owners = owners;
        this.conversionService = conversionService;
    }

    @ModelAttribute("types")
    public Collection<PetType> populatePetTypes() {
        return this.pets.findPetTypes();
    }

    private Owner findOwner(@PathVariable("ownerId") int ownerId) {
        return this.owners.findById(ownerId);
    }

    private void initPetBinder(WebDataBinder dataBinder) {
        dataBinder.setValidator(new PetValidator());
        dataBinder.setConversionService(conversionService);
    }

    @Bean
    public RouterFunction<ServerResponse> petRoutes() {
        return RouterFunctions.route().path("/owners/{ownerId}", builder -> builder //
                .GET("/pets/new", this::initCreationForm) //
                .POST("/pets/new", this::processCreationForm) //
                .GET("/pets/{petId}/edit", this::initUpdateForm) //
                .POST("/pets/{petId}/edit", this::processUpdateForm) //
        ).build();
    }

    private ServerResponse initCreationForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        Owner owner = findOwner(Integer.valueOf(request.pathVariable("ownerId")));
        Pet pet = new Pet();
        owner.addPet(pet);
        model.put("pet", pet);
        model.put("owner", owner);
        return ServerResponse.ok().render(VIEWS_PETS_CREATE_OR_UPDATE_FORM, model);
    }

    private ServerResponse processCreationForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        BindingResult result = bindPet(model, request);
        Pet pet = (Pet) model.get("pet");
        Owner owner = (Owner) model.get("owner");
        if (StringUtils.hasLength(pet.getName()) && pet.isNew()
                && owner.getPet(pet.getName(), true) != null) {
            result.rejectValue("name", "duplicate", "already exists");
        }
        if (result.hasErrors()) {
            return ServerResponse.ok().render(VIEWS_PETS_CREATE_OR_UPDATE_FORM, model);
        }
        else {
            this.pets.save(pet);
            return ServerResponse.ok().render("redirect:/owners/{ownerId}", model);
        }
    }

    private ServerResponse initUpdateForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        int petId = Integer.valueOf(request.pathVariable("petId"));
        Pet pet = this.pets.findById(petId);
        model.put("pet", pet);
        return ServerResponse.ok().render(VIEWS_PETS_CREATE_OR_UPDATE_FORM, model);
    }

    private ServerResponse processUpdateForm(ServerRequest request) {
        Map<String, Object> model = new HashMap<>();
        BindingResult result = bindPet(model, request);
        Pet pet = (Pet) model.get("pet");
        if (result.hasErrors()) {
            return ServerResponse.ok().render(VIEWS_PETS_CREATE_OR_UPDATE_FORM, model);
        }
        else {
            this.pets.save(pet);
            return ServerResponse.ok().render("redirect:/owners/{ownerId}", model);
        }
    }

    private BindingResult bindPet(Map<String, Object> model, ServerRequest request) {
        Pet pet = new Pet();
        ServletRequestDataBinder binder = new ServletRequestDataBinder(pet, "pet");
        initPetBinder(binder);
        binder.bind(request.servletRequest());
        binder.validate();
        BindingResult result = binder.getBindingResult();
        Integer ownerId = Integer.valueOf(request.pathVariable("ownerId"));
        Owner owner = findOwner(ownerId);
        ServletRequestDataBinder ownerBinder = new ServletRequestDataBinder(owner,
                "owner");
        model.put("ownerId", ownerId);
        owner.addPet(pet);
        model.put("owner", owner);
        model.put("org.springframework.validation.BindingResult.owner",
                ownerBinder.getBindingResult());
        model.put("pet", pet);
        model.put("org.springframework.validation.BindingResult.pet", result);
        return result;
    }

}
