package com.fengxu.fxhttp;

import com.fengxu.http.proxy.FxHttpInterceptor;
import com.fengxu.http.proxy.FxHttpMain;
import org.springframework.beans.factory.FactoryBean;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 风珝代理http创建工厂
 * @param <T> 代理接口类型
 */
public class FxHttpProxyFactory<T> implements FactoryBean<T> {

    private Class<T> interfaceType;

    private FxhttpConfigProperties.HttpProxyProps httpProps;


    public FxHttpProxyFactory(Class<T> interfaceType, FxhttpConfigProperties.HttpProxyProps props) {
        this.interfaceType = interfaceType;
        this.httpProps = props;
    }

    @Override
    public T getObject() throws Exception {
        FxHttpMain.Builder builder = new FxHttpMain.Builder()
                .baseUrl(httpProps.getBaseUrl())
                .startLog(httpProps.getStartLog());
        for (Map.Entry<List<Pattern>, Consumer<FxHttpInterceptor>> entry : httpProps.getPatternMap().entrySet()) {
            builder.addInterceptor(entry.getValue(),entry.getKey().toArray(new Pattern[0]));
        }
        return builder.build(interfaceType);
    }

    @Override
    public Class<?> getObjectType() {
        return interfaceType;
    }
}
