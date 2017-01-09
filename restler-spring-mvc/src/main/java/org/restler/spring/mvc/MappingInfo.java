package org.restler.spring.mvc;

import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.function.Function;

class MappingInfo<T> {

    private final T annotation;
    private final Function<T, String[]> pathSupplier;
    private final Function<T, RequestMethod[]> methodSupplier;

    private MappingInfo(T annotation, Function<T, String[]> pathSupplier, Function<T, RequestMethod[]> methodSupplier) {
        this.annotation = annotation;
        this.pathSupplier = pathSupplier;
        this.methodSupplier = methodSupplier;
    }

    String[] path() {
        return pathSupplier.apply(annotation);
    }

    RequestMethod[] method() {
        return methodSupplier.apply(annotation);
    }

    static MappingInfo forAnnotation(Annotation a) {
        if (a instanceof RequestMapping) {
            return new MappingInfo<>((RequestMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    RequestMapping::method);
        } else if (a instanceof GetMapping) {
            return new MappingInfo<>((GetMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    (any) -> new RequestMethod[]{RequestMethod.GET});
        } else if (a instanceof PostMapping) {
            return new MappingInfo<>((PostMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    (any) -> new RequestMethod[]{RequestMethod.GET});
        } else if (a instanceof PutMapping) {
            return new MappingInfo<>((PutMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    (any) -> new RequestMethod[]{RequestMethod.GET});
        } else if (a instanceof DeleteMapping) {
            return new MappingInfo<>((DeleteMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    (any) -> new RequestMethod[]{RequestMethod.GET});
        } else if (a instanceof PatchMapping) {
            return new MappingInfo<>((PatchMapping) a,
                    (ann) -> select(ann.value(), ann.path()),
                    (any) -> new RequestMethod[]{RequestMethod.GET});
        }
        throw new IllegalArgumentException("Unknown annotation: " + a);
    }

    private static String[] select(String[] value, String[] path) {
        // As of Spring 4.3 there are may be specified value for only one of aliased attributes
        // see AbstractAliasAwareAnnotationAttributeExtractor.getAttributeValue
        if (value.length > 0) {
            return value;
        } else {
            return path;
        }
    }
}
