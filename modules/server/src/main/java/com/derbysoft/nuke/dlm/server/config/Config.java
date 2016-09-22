package com.derbysoft.nuke.dlm.server.config;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Config {

    /**
     * key of configuration
     *
     * @return
     */
    String value();
}
