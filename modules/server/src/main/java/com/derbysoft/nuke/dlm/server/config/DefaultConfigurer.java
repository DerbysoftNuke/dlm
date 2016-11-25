package com.derbysoft.nuke.dlm.server.config;

import com.derbysoft.nuke.dlm.server.log.LogbackInitializing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.*;
import java.util.Map;
import java.util.Properties;

public class DefaultConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static final Logger log = LoggerFactory.getLogger(DefaultConfigurer.class);

    private Properties configProperties = new Properties();
    private FileSystemResource configResource;

    public DefaultConfigurer(String applicationKey) {
        this(applicationKey, "config.properties");
    }

    public DefaultConfigurer(String applicationKey, String fileName) {
        String configPath = System.getProperty(applicationKey);
        if (configPath == null) {
            log.warn("No external config set, please use -D{}=<your config path> to set config.properties", applicationKey);
        } else {
            try {
                configResource = new FileSystemResource(configPath + File.separator + fileName);
                configProperties.load(new InputStreamReader(new FileInputStream(configResource.getPath()), "UTF-8"));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            setLocations(new ClassPathResource("application.properties"), configResource);
            setOrder(1);
            setIgnoreUnresolvablePlaceholders(true);
            setLocalOverride(true);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        super.postProcessBeanFactory(beanFactory);
        new LogbackInitializing(this.getAppliedPropertySources());
    }

    public String getProperty(String name) {
        PropertyResolver resolver = new PropertySourcesPropertyResolver(getAppliedPropertySources());
        return resolver.getProperty(name);
    }

    public boolean setProperty(String name, String value) {
        if (configProperties.containsKey(name)) {
            configProperties.setProperty(name, value);
            try {
                configProperties.store(new OutputStreamWriter(new FileOutputStream(configResource.getPath()), "UTF-8"), value);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            return true;
        }

        return false;
    }

    public Map<String, String> getProperties() {
        return PropertiesUtils.getProperties(getAppliedPropertySources());
    }

}
