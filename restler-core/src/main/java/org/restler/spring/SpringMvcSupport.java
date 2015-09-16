package org.restler.spring;

import com.fasterxml.jackson.databind.Module;
import org.restler.Restler;
import org.restler.client.CallExecutionAdvice;
import org.restler.client.CoreModule;
import org.restler.client.CoreModuleFactory;
import org.restler.client.ParameterResolver;
import org.restler.http.RequestExecutor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class SpringMvcSupport implements CoreModuleFactory {

    private final List<Module> jacksonModules = new ArrayList<>();

    private ParameterResolver parameterResolver = ParameterResolver.valueOfParamResolver();
    private Executor executor = Restler.restlerExecutor;
    private Optional<RequestExecutor> requestExecutor = Optional.empty();

    @Override
    public CoreModule createModule(URI baseUri, List<CallExecutionAdvice<?>> enhancers) {
        List<CallExecutionAdvice<?>> totalEnhancers = new ArrayList<>();
        totalEnhancers.addAll(enhancers);
        totalEnhancers.addAll(singletonList(new DeferredResultHandler(executor)));

        return new SpringMvc(requestExecutor.orElseGet(this::createExecutor), totalEnhancers, baseUri, parameterResolver);
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

    public SpringMvcSupport threadPool(Executor executor) {
        this.executor = executor;
        return this;
    }

    private SpringMvcRequestExecutor createExecutor() {
        RestTemplate restTemplate = new RestTemplate();

        List<MappingJackson2HttpMessageConverter> jacksonConverters = restTemplate.getMessageConverters().stream().
                filter(converter -> converter instanceof MappingJackson2HttpMessageConverter).
                map(converter -> (MappingJackson2HttpMessageConverter) converter).
                collect(Collectors.toList());

        jacksonModules.stream().forEach(module ->
                jacksonConverters.forEach(converter ->
                        converter.getObjectMapper().registerModule(module)));
        return new SpringMvcRequestExecutor(restTemplate);
    }
}
