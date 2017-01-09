package org.restler.spring.mvc;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("greeter")
public interface Greeter {

    @RequestMapping("greetings/{language}")
    String getGreeting(@PathVariable(value = "language") String language,
                       @RequestParam(value = "name", defaultValue = "Antonymous") String name);

    @SuppressWarnings("MVCPathVariableInspection")
    @RequestMapping("{pathVar}")
    String methodWithNotMappedVar(String pathVar);

    @GetMapping(path = "path/{pathVar}")
    String getMapping(@PathVariable(value = "pathVar") Integer pathVar);
}

