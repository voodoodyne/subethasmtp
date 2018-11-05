package org.subethamail.smtp.server;

public interface ServerThreadNameProvider {

    /**
     * Generates a thead name for given server
     *
     * @param server
     */
    String getName(SMTPServer server);
}
