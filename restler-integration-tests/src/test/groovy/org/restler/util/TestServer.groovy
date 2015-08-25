package org.restler.util

import org.restler.integration.IntegrationPackage

@Singleton(lazy = true)
class TestServer {

    private def server = IntegrationPackage.server()

    def ensureStarted() {
        if (!server.started) {
            server.start()
            server.stopAtShutdown = true
        }
    }

}
