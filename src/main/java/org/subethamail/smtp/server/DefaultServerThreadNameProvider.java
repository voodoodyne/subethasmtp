package org.subethamail.smtp.server;

public class DefaultServerThreadNameProvider implements ServerThreadNameProvider {

    public String getName(SMTPServer server) {
        return ServerThread.class.getName() + " " + server.getDisplayableLocalSocketAddress();
    }
}
