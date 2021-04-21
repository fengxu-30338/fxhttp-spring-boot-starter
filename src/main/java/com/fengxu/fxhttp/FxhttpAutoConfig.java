package com.fengxu.fxhttp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(FxhttpConfigProperties.class)
@Import(FxBeanRegister.class)
public class FxhttpAutoConfig implements ApplicationRunner {

    @Autowired
    private FxBeanRegister fxBeanRegister;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        fxBeanRegister.registerBean();
    }

}
