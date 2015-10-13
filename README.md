[![Build Status](https://travis-ci.org/excelsior-oss/restler.svg?branch=master)](https://travis-ci.org/excelsior-oss/restler)
[![Maven Central](https://img.shields.io/maven-central/v/org.restler/restler-core.svg)](https://maven-badges.herokuapp.com/maven-central/org.restler/restler-core)

Restler
=======
 
### Overview
*Restler* is a library that generates a client of a web service by its annotated Spring controller interface at runtime. *Restler* may help you to remove HTTP-specific boilerplate from your integration tests, microservices and [thirdparty HTTP API clients](https://github.com/excelsior-oss/restler/wiki/GitHub-client).

**EPA warning: Restler currently is in early public access stage and it is neither feature complete, tested in production or backward compatible**

### Features
 * Easuly extensible architecture
 * Custom authentication, authorization and errors mapping strategies.
 * Form-based authorization.
 * Cookie and HTTP Basic authentication.
 * Support of async controllers through methods returning Future, DefferedResult or Callabe objects
 * Experemental Spring Data Rest support

### Simple usage example

Assuming, you have following interface on the server
```java
/** 
  * An annotated Spring controller interface
  */
@Controller
@RequestMapping("greeter")
public interface Greeter {

	@RequestMapping("greetings/{language}")	
	String getGreeting(@PathVariable String language, @RequestParam(defaultValue = "Anonimous") String name); 

}
```

Then from your client you can invoke getGreating method on remote service using following code snippet
```java
Service service = new Restler("https://www.excelsior-usa.com/api", new SpringMvcSupport()).build();
Greeter greeter = service.produceClient(Greeter.class);
String greeting = greeter.getGreeting("en","Boddy"); // the result of https://www.excelsior-usa.com/api/greeter/greetings/en?name=Boddy call
```
