package org.restler.spring.data.proxy;

import org.restler.RestlerConfig;
import org.restler.client.CallExecutor;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by rudenko on 09.02.2016.
 */
public interface Resource {
    void setExecutor(CallExecutor executor);
    void setRestlerConfig(RestlerConfig config);

    String getRepositoryUri();
    String getSelfUri();
    Object getResourceId();

    CallExecutor getCallExecutor();
    RestlerConfig getRestlerConfig();
    Object getObject();
    HashMap<String, String> getHrefs();
}
