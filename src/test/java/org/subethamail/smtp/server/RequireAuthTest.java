package org.subethamail.smtp.server;

import org.subethamail.smtp.auth.EasyAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.util.Base64;
import org.subethamail.smtp.util.Client;
import org.subethamail.smtp.util.ServerTestCase;
import org.subethamail.smtp.util.TextUtils;

/**
 * @author Evgeny Naumenko
 */
public class RequireAuthTest  extends ServerTestCase
{
    static final String REQUIRED_USERNAME = "myUserName";
    static final String REQUIRED_PASSWORD = "mySecret01";

    class RequiredUsernamePasswordValidator implements UsernamePasswordValidator
    {
        public void login(String username, String password) throws LoginFailedException
        {
            if (!username.equals(REQUIRED_USERNAME) || !password.equals(REQUIRED_PASSWORD))
            {
                throw new LoginFailedException();
            }
        }
    }

    /** */
    public RequireAuthTest(String name)
    {
        super(name);
    }

    /*
      * (non-Javadoc)
      *
      * @see org.subethamail.smtp.ServerTestCase#setUp()
      */
    @Override
    protected void setUp() throws Exception
    {
        this.wiser = new TestWiser();
        this.wiser.setHostname("localhost");
        this.wiser.setPort(PORT);

        UsernamePasswordValidator validator = new RequiredUsernamePasswordValidator();

        EasyAuthenticationHandlerFactory fact = new EasyAuthenticationHandlerFactory(validator);
        this.wiser.getServer().setAuthenticationHandlerFactory(fact);
        this.wiser.getServer().setRequireAuth(true);
        this.wiser.start();
        this.c = new Client("localhost", PORT);
    }

    /*
      * (non-Javadoc)
      *
      * @see org.subethamail.smtp.ServerTestCase#tearDown()
      */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /** */
    public void testAuthRequired() throws Exception
    {
        this.expect("220");

        this.send("HELO foo.com");
        this.expect("250");

        this.send("EHLO foo.com");
        this.expect("250");

        this.send("NOOP");
        this.expect("250");

        this.send("RSET");
        this.expect("250");

        this.send("MAIL FROM: test@example.com");
        this.expect("530 5.7.0  Authentication required");

        this.send("RCPT TO: test@example.com");
        this.expect("530 5.7.0  Authentication required");

        this.send("DATA");
        this.expect("530 5.7.0  Authentication required");

        this.send("STARTTLS");
        this.expect("454 TLS not supported");

        this.send("QUIT");
        this.expect("221 Bye");
    }

    /** */
    public void testAuthSuccess() throws Exception
    {
        this.expect("220");

        this.send("HELO foo.com");
        this.expect("250");

        this.send("AUTH LOGIN");
        this.expect("334");

        String enc_username = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_USERNAME), false);

        this.send(enc_username);
        this.expect("334");

        String enc_pwd = Base64.encodeToString(TextUtils.getAsciiBytes(REQUIRED_PASSWORD), false);
        this.send(enc_pwd);
        this.expect("235");

        this.send("MAIL FROM: test@example.com");
        this.expect("250");

        this.send("RCPT TO: test@example.com");
        this.expect("250");

        this.send("DATA");
        this.expect("354");

        this.send("\r\n.");
        this.expect("250");

        this.send("QUIT");
        this.expect("221 Bye");
    }
}
