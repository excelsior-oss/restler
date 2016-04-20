package org.restler.spring.mvc;

import com.fasterxml.jackson.databind.Module;
import org.restler.RestlerConfig;
import org.restler.client.CGLibClientFactory;
import org.restler.client.CachingClientFactory;
import org.restler.client.CallEnhancer;
import org.restler.client.CoreModule;
import org.restler.http.OkHttpRequestExecutor;
import org.restler.http.RequestExecutor;
import org.restler.spring.mvc.spring.DeferredResultHandler;
import org.restler.spring.mvc.spring.SpringMvcRequestExecutor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class
SpringMvcSupport implements Function<RestlerConfig, CoreModule> {

    private final List<Module> jacksonModules = new ArrayList<>();

    private ParameterResolver parameterResolver = ParameterResolver.valueOfParamResolver();

    private Optional<RequestExecutor> requestExecutor = Optional.empty();

    @Override
    public CoreModule apply(RestlerConfig config) {
        List<CallEnhancer> totalEnhancers = new ArrayList<>();
        totalEnhancers.addAll(config.getEnhancers());

        try {
            Class.forName("org.springframework.web.context.request.async.DeferredResult");
            totalEnhancers.addAll(singletonList(new DeferredResultHandler(config.getRestlerThreadPool())));
        } catch (ClassNotFoundException e) {
            //nothing
        }

        return new SpringMvc(new CachingClientFactory(new CGLibClientFactory()), requestExecutor.orElseGet(this::createExecutor), totalEnhancers, config.getBaseUri(), parameterResolver);
    }

    public SpringMvcSupport addJacksonModule(Module module) {
        jacksonModules.add(module);
        return this;
    }

    public SpringMvcSupport requestExecutor(RequestExecutor requestExecutor) {
        this.requestExecutor = Optional.of(requestExecutor);
        return this;
    }

    public SpringMvcSupport parameterResolver(ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
        return this;
    }

    private RequestExecutor createExecutor() {

        try {
            Class.forName("org.springframework.web.client.RestTemplate");
            Class.forName("org.springframework.http.converter.json.MappingJackson2HttpMessageConverter");

            RestTemplate restTemplate = new RestTemplate();
            List<MappingJackson2HttpMessageConverter> jacksonConverters = restTemplate.getMessageConverters().stream().
                    filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).
                    map(converter -> (MappingJackson2HttpMessageConverter) converter).
                    collect(Collectors.toList());

            jacksonModules.stream().forEach(module ->
                    jacksonConverters.forEach(converter ->
                            converter.getObjectMapper().registerModule(module)));


            return new SpringMvcRequestExecutor(restTemplate);
        } catch (ClassNotFoundException e) {
            return new OkHttpRequestExecutor(jacksonModules);
        }
    }
}
