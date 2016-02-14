package org.restler.spring.data.proxy;

import org.restler.RestlerConfig;
import org.restler.client.CallExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by rudenko on 09.02.2016.
 */
public interface ProxyObject {
    void setExecutor(CallExecutor executor);
    void setRestlerConfig(RestlerConfig config);

    CallExecutor getCallExecutor();
    RestlerConfig getRestlerConfig();
    Object getObject();
    HashMap<String, String> getHrefs();
}
