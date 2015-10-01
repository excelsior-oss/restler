package org.restler.http;

import org.restler.client.Call;

public interface RequestExecutor {

    Response execute(Call request);

}
