/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * PetClinic Spring Boot Application.
 *
 * @author Dave Syer
 *
 */
@SpringBootApplication(proxyBeanMethods = false)
public class PetClinicApplication {

    private static final class Ripper
            implements BeanDefinitionRegistryPostProcessor, Ordered {
        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
                throws BeansException {
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
                throws BeansException {
            System.err.println(Arrays.asList(registry.getBeanDefinitionNames()));
            if (!registry.containsBeanDefinition(
                    AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
                RootBeanDefinition def = new RootBeanDefinition(
                        AutowiredAnnotationBeanPostProcessor.class);
                def.setInstanceSupplier(() -> new AutowiredAnnotationBeanPostProcessor());
                registry.registerBeanDefinition(
                        AnnotationConfigUtils.AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
                        def);
            }
            if (registry.containsBeanDefinition("methodValidationPostProcessor")) {
                registry.removeBeanDefinition("methodValidationPostProcessor");
            }
        }

        @Override
        public int getOrder() {
            return 10;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PetClinicApplication.class, args);
    }

    @Bean
    public static Ripper ripper() {
        return new Ripper();
    }
}
