[![Build Status](https://travis-ci.org/excelsior-oss/restler.svg?branch=master)](https://travis-ci.org/excelsior-oss/restler)
[![Maven Central](https://img.shields.io/maven-central/v/org.restler/restler-core.svg)](https://maven-badges.herokuapp.com/maven-central/org.restler/restler-core)

Restler
=======
 
### Overview
*Restler* is a library that automatically generates a client for a web service at run time, by analyzing the respective annotated Spring controller interface. *Restler* may help you remove HTTP-specific boilerplate from your integration tests, microservices and [thirdparty HTTP API clients](https://github.com/excelsior-oss/restler/wiki/GitHub-client).

**EPA warning: Restler currently is in early public access stage and it is neither feature complete, tested in production or backward compatible**

### Features
 * Easily extensible architecture
 * Custom authentication, authorization and errors mapping strategies
 * Support of form-based, http basic, cookie-based and generic header-based authentication
 * Support of async controllers through methods returning `Future`, `DefferedResult` or `Callable` objects
 * Experemental Spring Data REST support

### Simple Usage Example

Suppose you have the following interface on the server:
```java
/** 
  * An annotated Spring controller interface
  */
@Controller
@RequestMapping("greeter")
public interface Greeter {

	@RequestMapping("greetings/{language}")	
	String getGreeting(@PathVariable String language, @RequestParam(defaultValue = "Anonymous") String name); 

}
```

Then in your client you can invoke the `getGreeting` method of the remote service using the following code snippet:
```java
Service service = new Restler("https://www.example.com/api", new SpringMvcSupport()).build();
Greeter greeter = service.produceClient(Greeter.class);
String greeting = greeter.getGreeting("en","Buddy"); // the result of https://www.example.com/api/greeter/greetings/en?name=Buddy call
```
