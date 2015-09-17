package org.restler.spring.mvc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

@FunctionalInterface
public interface ParameterResolver {

    static ParameterResolver valueOfParamResolver() {
        return (m, args, annotations, paramNames, paramIdx) -> Optional.ofNullable(args[paramIdx]).map(String::valueOf);
    }

    Optional<String> resolve(Method m, Object[] args, Annotation[][] annotations, String[] paramNames, int paramIdx);

}
