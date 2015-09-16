package org.restler.client;

import java.net.URI;
import java.util.List;

public interface CoreModuleFactory {

    CoreModule createModule(URI baseUri, List<CallExecutionAdvice<?>> enhancers);

}
