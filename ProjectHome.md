SubEtha SMTP is a Java library which allows your application to receive SMTP mail with a simple, easy-to-understand API.

This component can be used in almost any kind of email  processing application.  Hypothetical (and not-so hypothetical) uses include:

  * A mailing list manager (see SubEthaMail)
  * A mail server that delivers mail to user inboxes
  * A mail archiver like [The Mail Archive](http://www.mail-archive.com/)
  * An email test harness (see [Wiser](Wiser.md))
  * An email2fax system
  * SMTPseudo [A filtering forwarding server](http://code.google.com/p/smtpseudo/)
  * [Baton](http://code.google.com/p/baton/) SMTP proxy for one or more backends (rules based on sender/envelope)
  * [Mireka](http://code.google.com/p/mireka/) - Mail server and SMTP proxy with detailed logging, statistics and built-in, fail-fast filters


SubEthaSMTP's simple, low-level API is suitable for writing almost any kind of mail-receiving application.  Read more in [UsingSubEthaSMTP](UsingSubEthaSMTP.md) or join our MailingList.

## A Little History ##
SubEthaSMTP was split out of the SubEthaMail mailing list manager because it is a useful standalone component.  When we wrote SubEtha, the last thing we wanted to do was write our own SMTP server.  In our search for a modular Java SMTP component, we examined:

  * [Apache JAMES](http://james.apache.org/)
  * [JBoss Mail Server](http://labs.jboss.com/portal/jbossmail/index.html), now also defunct [Meldware Mail](http://www.buni.org/mediawiki/index.php/Meldware_Mail)
  * [Dumbster](http://quintanasoft.com/dumbster/)
  * [Jsmtpd](http://www.jsmtpd.org/site/)
  * [JES](http://www.ericdaugherty.com/java/mailserver/)
  * [Green Mail](http://www.icegreen.com/greenmail/)

Since you're reading this page you probably already know what we found:  Seven different SMTP implementations without the slightest thought given to reusability. Even Jstmpd, which purports to be a "A Modular Java SMTP Daemon", isn't.  Even though JBoss Mail/Meldware Mail is in active development, the team was unintersted in componentization of the SMTP processing portion of their server.  GreenMail, which is based on the JAMES code base is best summarized with this [blog posting](http://eokyere.blogspot.com/2006/10/get-wiser-with-subethasmtp.html).

During the development of SubEtha's testing harness, we tried out the [Dumbster](http://quintanasoft.com/dumbster/) software  and found that not only was the API difficult to use, it did it not work properly, the developer has not done any development on it in about a year and it does not work reliably on Mac OS X. With two simple classes we re-implemented it as an included project called [Wiser](Wiser.md).

We hate reinventing wheels.  This should be the LAST FREAKING JAVA SMTP IMPLEMENTATION.

## Project Authors ##
Ian McFarland contributed the first codebase to SubEtha Mail. Then, Jon Stevens and Jeff Schnitzer re-wrote most of Ian's code into what we have today. Edouard De Oliveira and Scott Hernandez have also made significant contributions.

## Support ##
If you have any bug reports, questions or comments about SubEtha SMTP, it's best that you bring these issues up on the Mailing Lists.  Please do not email the authors directly.

## Spec Compliance ##
For now, we have just focused on implementing just the minimal  required aspects of http://rfc.net/rfc2821.html#s4.5.1. We also return SMTP status responses that mimic what Postfix returns.

Thanks to a contribution from [Mike Wildpaner](mailto:mikeREMOVETHISPART@wildpaner.com), we support the [StartTLS specification](http://rfc.net/rfc2487.html).

Thanks to a contribution from [Marco Trevisan](mailto:mrctrevisanREMOVETHISPART@yahoo.it), we support the [SMTP AUTH specification](http://rfc.net/rfc2554.html).
