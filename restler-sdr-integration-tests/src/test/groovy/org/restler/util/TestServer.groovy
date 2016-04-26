package org.restler.util

import org.restler.integration.TestServerKt

@Singleton(lazy = true)
class TestServer {

    private def server = TestServerKt.server()

    def ensureStarted() {
        if (!server.started) {
            server.start()
            server.stopAtShutdown = true
        }
    }

}
