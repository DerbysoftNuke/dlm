package com.derbysoft.nuke.dlm.server;

import com.derbysoft.nuke.dlm.standalone.StandalonePermit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by passyt on 16-9-3.
 */
@SpringBootApplication
@EnableAutoConfiguration
public class PermitApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PermitApplication.class, args);
    }

}
