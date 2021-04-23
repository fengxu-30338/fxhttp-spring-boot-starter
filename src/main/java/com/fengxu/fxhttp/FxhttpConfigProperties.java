package com.fengxu.fxhttp;

import com.fengxu.http.proxy.FxHttpInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "fxhttp")
public class FxhttpConfigProperties {

    // http接口标识(beanName)，该接口的属性
    private Map<String,HttpProxyProps> httpProxy;

    // 注解形式配置的接口存在的基本包
    private String basePackage;

    public Map<String, HttpProxyProps> getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(Map<String, HttpProxyProps> httpProxy) {
        this.httpProxy = httpProxy;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public static class HttpProxyProps{

        private String className;

        private String baseUrl = "";

        private Boolean startLog = false;

        private Map<List<Pattern>, Consumer<FxHttpInterceptor>> patternMap = new LinkedHashMap<>();

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Boolean getStartLog() {
            return startLog;
        }

        public void setStartLog(Boolean startLog) {
            this.startLog = startLog;
        }

        public Map<List<Pattern>, Consumer<FxHttpInterceptor>> getPatternMap() {
            return patternMap;
        }

    }

}
