package com.derbysoft.nuke.dlm.server.dispatch;

import java.lang.annotation.*;

/**
 * Created by passyt on 16-9-22.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    String uri();

    String contentType() default "text/html";

}
