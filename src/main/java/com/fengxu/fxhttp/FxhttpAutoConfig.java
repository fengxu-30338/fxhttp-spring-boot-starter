package com.fengxu.fxhttp;

import com.fengxu.fxhttp.v2.FxBeanRegisterV2;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FxBeanRegisterV1.class, FxBeanRegisterV2.class})
@EnableConfigurationProperties(FxhttpConfigProperties.class)
public class FxhttpAutoConfig{

}
