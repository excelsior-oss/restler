package org.restler.spring.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("greeter")
public interface Greeter {

    @ResponseBody
    @RequestMapping("greetings/{language}")
    String getGreeting(@PathVariable(value = "language") String language,
                       @RequestParam(value = "name", defaultValue = "Antonymous") String name);

    @ResponseBody
    @RequestMapping("{pathVar}")
    String methodWithNotMappedVar(String pathVar);

}

