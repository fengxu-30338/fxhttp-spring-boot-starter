package com.fengxu.fxhttp;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

class FxBeanRegisterV1 implements InitializingBean {

    private DefaultListableBeanFactory ioc;

    private FxhttpConfigProperties httpProperties;

    public FxBeanRegisterV1(DefaultListableBeanFactory ioc, FxhttpConfigProperties httpProperties) {
        this.ioc = ioc;
        this.httpProperties = httpProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 未使用yml配置fxhttp接口代理
        if(httpProperties == null || httpProperties.getHttpProxy() == null){
            return;
        }
        FxBeanRegisterExecutor fxBeanRegisterExecutor = new FxBeanRegisterExecutor(ioc, httpProperties);
        fxBeanRegisterExecutor.handlerBlocker(true);
        fxBeanRegisterExecutor.registerBean(ioc);
    }
}
