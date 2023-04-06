package io.jkelly.evadiscordbot.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @NotNull
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        var factoryBean = new YamlPropertiesFactoryBean();
        var properties = factoryBean.getObject();
        factoryBean.setResources(resource.getResource());

        return new PropertiesPropertySource(resource.getResource().getFilename(), properties);
    }

}