## Changes ##

### 3.1.7 - June 12, 2012 ###
  * Complete Maven build (in addition to the original Ant based build)
  * SLF4J upgraded from the 1.5 series to 1.6
  * More graceful shutdown: SMTPServer.stop waits until all connections finish their work.
  * [Issue 45](https://code.google.com/p/subethasmtp/issues/detail?id=45): AUTH LOGIN authentication is compatible with the .NET SMTP library. Patch by Reid Nimz.
  * AUTH command replies with specific error messages if something goes wrong.
  * [Issue 46](https://code.google.com/p/subethasmtp/issues/detail?id=46): Suebetha loses session.helo after first email.
  * Case-sensitivity issue with the AUTH command
  * Build-time libraries are placed into a separate directory.
  * [Issue 47](https://code.google.com/p/subethasmtp/issues/detail?id=47): MessageHandler lifecycle better matches its purpose: following a single mail transaction within a full session. HELO and EHLO resets the mail transaction in progress.
  * [Issue 48](https://code.google.com/p/subethasmtp/issues/detail?id=48): Use ExecutorService instead of new Thread. Patch from Eric Parusel.
  * Possible concurrency issue on connection shutdown.
  * [Issue 49](https://code.google.com/p/subethasmtp/issues/detail?id=49): No error if server is running already
  * Improved Javadoc about authentication
  * SMTP session identifier is generated and included in the Received header and in the logging context.

### 3.1.6 - June 23, 2011 ###

  * [Issue 39](https://code.google.com/p/subethasmtp/issues/detail?id=39)
  * [Issue 40](https://code.google.com/p/subethasmtp/issues/detail?id=40)
  * More error handling in SMTPClient when a broken server does not send a reply at all
  * Received header now does not contain a second source domain name string in the address part, so it conforms to the RFC. In addition, if the address cannot be resolved to a domain name then it does not print the address in place of the domain name, but simply omits that part according to the RFC.
  * ReceivedHeaderStream adds a FOR Single-recipient clause to the Received header when there is only a single accepted recipient in the mail transaction.
  * ReceivedHeaderStream shows sofware name and version, as they are set in SMTPServer
  * Logging of RuntimeException and Error throwables in the session thread. This is in addition to the UncaughtExceptionHandler which is set by the JRE or by the application. The default handler installed in the JRE prints such exceptions to STDERR.
  * Log the name of the selected cipher suite after a successful TLS negotiation. This makes it possible to check if an appropriate cipher suite was selected. Moreover, this log message is a good indicator of a successful TLS handshake.
  * Added documentation about the steps necessary for the simplest working TLS configuration. The default status of TLS support is changed: now TLS is disabled by default. This is an incompatible change! TLS should not be enabled without explicit consent, because it requires a considerable amount of configuration. An enabled TLS without a configured JSSE causes that some mails will not be delivered.
  * [Issue 42](https://code.google.com/p/subethasmtp/issues/detail?id=42)
  * [Issue 32](https://code.google.com/p/subethasmtp/issues/detail?id=32)
  * [Issue 34](https://code.google.com/p/subethasmtp/issues/detail?id=34)
  * bugfix: avoid logging of socket close twice
  * Upgrade mail.jar. The old version has a bug affecting unit tests, it added an unnecessary line end to mail bodies.
  * [Revision 417](https://code.google.com/p/subethasmtp/source/detail?r=417) fixes tests
  * unexpected errors are logged in the server socket thread; external interrupts are ignored
  * Now the handler is not required to consume all mail content bytes. This was intended already, but the code did not really allowed it.
  * A method is added to make easier the wrapping of existing SMTP command objects by code outside of SubEthaSMTP.

### 3.1.5 - February 28, 2011 ###

  * [Issue 36](https://code.google.com/p/subethasmtp/issues/detail?id=36): PlainAuthenticationHandlerFactory should accept non-null authorization identity. Patch is provided by Ian White.
  * [Issue 35](https://code.google.com/p/subethasmtp/issues/detail?id=35): Addition of Received headers is configurable. Patch is provided by rwiermer.
  * [Issue 30](https://code.google.com/p/subethasmtp/issues/detail?id=30): Client timeouts if the server does not respond
  * Wiser.messages is now protected so subclasses can override

### 3.1.4 - June 1, 2010 ###
  * Added the ability to drop connections by throwing a DropConnectionException
  * RejectException is now a RuntimeException **NOTE this might be a breaking change for your app**
  * TLS can be disabled
  * Publicly reported name of server can be controlled with a "softwareName" property

### 3.1.3 - February 11, 2010 ###
  * [Fix a bug in ReceivedHeaderStream](http://code.google.com/p/subethasmtp/issues/detail?id=28)

### 3.1.2 - October 24, 2009 ###
  * Much better TLS support
  * Added support for the SIZE extension
  * Helo information now available in MessageContext
  * Fixed casing bug running in Turkish locale
  * Better logging for mulithreaded environments
  * Fixed issue with month formatting in smtp exchange
  * Hides dropped connection exceptions from logs
  * SMTPClient can now set bind address
  * More logging in SMTClient

### 3.1.1 - August 17, 2009 ###
  * Targeted to java 1.5 rather than 1.6.  No other changes.

### 3.1 - June 4, 2009 ###
  * Now adds Received: headers to data stream
  * Added done() method to MessageHandler
  * AuthenticationHandler now provides the authenticated identity
  * Added some simple client tools
  * Moved everything in the smtp.server.io package to just smtp.io
  * Added a slight variation on the message listener adapter
  * Now consumes any leftover data that the handler did not eat rather than throwing exception
  * Jar file name now includes version number
  * Distribution now includes wiki docs

### 3.0 - May 5, 2009 ###
  * 3.0 is a thorough refactoring based on the 1.2.1 codebase
  * Removed MINA and NIO, returning to blocking I/O
  * Redesigned Authentication API
  * Moved MessageListener API into a helper package
  * Timeouts are now controlled by sockets rather than a watchdog thread
  * MessageHandlers are no longer recycled
  * Better handling of idle connections
  * Countless other changes
  * Moved project from tigris.org to code.google.com

### 2.1 - July 11, 2008 ###
  * Fix to ensure session is closed on QUIT command response
  * Fix to close connections after QUIT for clients which don't auto close the session
  * Announce TLS support patch
  * small optimization for getAuthenticationMechanisms()
  * catch exceptions so that all the shutdown methods get called if there is an error.
  * Default MessageHandler implementation easily extendable or replaceable
  * Modified to allow easier tweaking of the message handling
  * Added default authentication handling / removed dummy message handling methods / added a private stream generator
  * Replaced the old AuthLoginTest and AuthPlainTest classes
  * Fix for encoding problems in junit classes src
  * Fixed [issue 8](https://code.google.com/p/subethasmtp/issues/detail?id=8) : shutdown the thread pools to avoid unnecessary delays when server stopped
  * Fixed max length to comply with RFC
  * Added some encoding tests
  * The new smtp decoder (enhanced performances, charset friendly)
  * Fixed default charset and added some methods to further configure the core
  * Added SMTP server configuration methods
  * Updated mina libraries
  * Modified ThreadFactory to setup debug friendly thread's names

### 2.0.1 - January 25, 2008 ###
  * Fix issue with Wiser not storing messages for re-use.
  * Major refatoring to prevent OOM and improve perfs when dealing with large attached files
  * Changed returned hostname to locahost when null
  * Added -1 value to override limits on setMaxConnections() and setMaxRecipients()
  * Fixed handling of auth mechanisms to require a configured auth mechanism when needed
  * Fixed missing space char in extended HELO AUTH string
  * Will prevent an SLF4J error on startup by linking the jdk14 logging facility

### 2.0 - October 26, 2007 ###
  * Use Apache Mina 1.1.3 for the networking layer. This make us Native IO (NIO) based. woot!
  * Expose Mina JMX monitoring.
  * Use slf4j instead of log4j for logging.
  * Rewrite build system to be easier / cleaner / faster.
  * Remove Maven pom.xml crap. Its a pile of dog shit. Don't ever use it.

### 1.2.1 - June 28, 2007 ###
  * Fix issue with requiring a HELO/EHLO after a RSET.
  * Fix issue with canceling the sending of a message and not resetting internal state.
  * Add more unit tests.

### 1.2 - March 1, 2007 ###
  * Integrate retroweaver into the ant build system for JDK 1.4 compatibility.
  * Change the license from LGPL to ASFL 2.0 to allow for wider adoption.
  * New feature: Pluggable SMTP AUTH and related unit tests. Thanks Marco.
  * Bug fix: Accept 'MAIL FROM: <>' and properly reject 'MAIL FROM:'.  Thanks to the report from David Haggerty.

### 1.1 - Oct 5, 2006 ###
  * Added new interface for instantiating the server (MessageHandler/MessageHandlerFactory). This allows much more control over the message handling. MessageListenerAdapter has been added as a backwards compatible way of creating the server instance.
  * Removed SMTPServer.setDataDeferredSize() method. This is a non backwards compatible change, but we assume not many people were using it anyway. It's possible to still modify this setting by instantiating your own SMTPServer instance and passing the value into MessageListenerAdapter or your own MessageHandlerFactory.
  * Rewrote unit tests to use a lightweight telnet client.
  * Fixed some threading issues with fast start/stop'ing of the server.
  * Added code to explicitly close any open Socket connections when the server is shut down.
  * Now including the source code in the distribution for easy viewing.
  * java -jar on the smtp/wiser jar files now returns it's version info.

### 1.0.4 - Sep 12, 2006 ###
  * Fixed issue where HELO/EHLO was required after a RSET.

### 1.0.3 - Aug 20, 2006 ###
  * Fixed issue with sockets not properly being closed.
  * Made it possible to override the way that sockets are created.
  * Fixed a bug when MAIL FROM is executed after RSET is called after DATA.
  * Added support for TLS. Might have bugs.
  * More maven POM cleanup.

### 1.0.2 - Aug 2, 2006 ###
  * Include www/wiser.html in the distribution.
  * Add http://maven.apache.org/ pom.xml files.
  * Documentation improvements.

### 1.0.1 - Jul 5, 2006 ###
  * Adding junit tests.
  * SMTP spec compliance improvements.
  * Fixes a problem with multiple recipients.

### 1.0 - Jun 20, 2006 ###
  * First public release.