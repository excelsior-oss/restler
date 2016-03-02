package org.restler.spring.data.proxy;

import org.restler.client.CallExecutor;

import java.util.HashMap;

public interface ResourceProxy {
    void setExecutor(CallExecutor executor);

    String getRepositoryUri();
    String getSelfUri();
    Object getResourceId();
    Object getObject();
    HashMap<String, String> getHrefs();
}
