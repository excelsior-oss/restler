Restler
=======
 
Overview
--------

*Restler* is a library that generates a client of a web service by its annotated Spring controller interface at runtime. 
 
### Simple usage example

```java
/** 
*     An annotated Spring controller interface
*/
@Controller
@RequestMapping("greeter")
public interface Greeter {

	@RequestMapping("greetings/{language}")	
	String getGreeting(@PathVariable String language, @RequestParam(defaultValue = "Anonimous") String name); 

}

// Consuming code 
Service service = new Service("https://www.excelsior-usa.com/api");
Greeter greeter = service.getController(Greeter.class);
String greeting = greeter.getGreeting("en","Boddy"); // the result of https://www.excelsior-usa.com/api/greeter/greetings/en?name=Boddy call

// Session support in consuming code
Service service = new Service("https://www.excelsior-usa.com/api");
AuthorizationStrategy authorizationStrategy = new LoginAuthorizationStrategy(...);
Session session = service.startSession(authorizationStrategy);
Greeter greeter = service.getController(Greeter.class);
String greeting = greeter.getGreeting("en","Boddy");
```
