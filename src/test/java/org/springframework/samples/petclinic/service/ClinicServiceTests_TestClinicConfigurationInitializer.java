package org.springframework.samples.petclinic.service;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.init.func.ConditionService;
import org.springframework.init.func.InfrastructureUtils;
import org.springframework.init.func.TypeService;
import org.springframework.samples.petclinic.PetClinicApplication;

public class ClinicServiceTests_TestClinicConfigurationInitializer
        implements ApplicationContextInitializer<GenericApplicationContext> {
    @Override
    public void initialize(GenericApplicationContext context) {
        ConditionService conditions = InfrastructureUtils
                .getBean(context.getBeanFactory(), ConditionService.class);
        if (conditions.matches(PetClinicApplication.class)) {
            if (context.getBeanFactory()
                    .getBeanNamesForType(PetClinicApplication.class).length == 0) {
                TypeService types = InfrastructureUtils.getBean(context.getBeanFactory(),
                        TypeService.class);
                context.registerBean(types.getType(
                        "org.springframework.samples.petclinic.owner.JdbcOwnerRepositoryImpl"));
                context.registerBean(types.getType(
                        "org.springframework.samples.petclinic.owner.JdbcPetRepositoryImpl"));
                context.registerBean(types.getType(
                        "org.springframework.samples.petclinic.vet.JdbcVetRepositoryImpl"));
                context.registerBean(types.getType(
                        "org.springframework.samples.petclinic.visit.JdbcVisitRepositoryImpl"));
            }
        }
    }
}
