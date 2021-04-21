package com.fengxu.fxhttp;

import com.fengxu.http.proxy.FxHttpInterceptor;
import com.fengxu.http.proxy.FxHttpMain;
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

    public Map<String, HttpProxyProps> getHttpProxy() {
        return httpProxy;
    }

    public void setHttpProxy(Map<String, HttpProxyProps> httpProxy) {
        this.httpProxy = httpProxy;
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

        /**
         * 生成代理
         * @return 代理对象
         */
        public Object generatorProxy(){
            Class<?> targetClass;
            try {
                targetClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("未找到"+className+"目标类!");
            }
            FxHttpMain.Builder builder = new FxHttpMain.Builder()
                    .baseUrl(baseUrl)
                    .startLog(startLog);
            for (Map.Entry<List<Pattern>, Consumer<FxHttpInterceptor>> entry : patternMap.entrySet()) {
                builder.addInterceptor(entry.getValue(),entry.getKey().toArray(new Pattern[0]));
            }
            return builder.build(targetClass);
        }
    }

}
