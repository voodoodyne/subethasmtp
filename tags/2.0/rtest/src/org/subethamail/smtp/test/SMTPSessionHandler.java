package org.subethamail.smtp.test;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

/**
 * NIO Test client SMTP session handler
 * 
 * @author De Oliveira Edouard &lt;doe_wanted@yahoo.fr&gt;
 */
public class SMTPSessionHandler extends IoHandlerAdapter
{
    private String hostName;
    
    public SMTPSessionHandler(String hostName)
    {
        this.hostName = hostName;
    }

    @Override
    public void sessionOpened( IoSession session )
    {
        session.write("EHLO " + hostName);
        session.setAttribute("step", new Integer(0));
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception
    {
        System.out.println("Session idled !!!");
    }
    
    @Override
    public void messageReceived( IoSession session, Object message )
    {
        int step = ((Integer) session.getAttribute("step")).intValue();
        
        switch (step) 
        {
            case 0 : send(session, "MAIL FROM:Robot sender<sender@gmail.com>");
                break;
            case 1 : send(session, "RCPT TO:Katherine Dunn<Katherine.Dunn@fake.com>");
                break;
            case 2 : send(session, "DATA");
                break;
            case 3 : send(session, "");
                send(session, "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Suspendisse a neque. " +
                        "In nec dolor eu massa porttitor accumsan. Donec varius mollis dolor. Vestibulum ut nulla " +
                        "vel diam mollis imperdiet. Vestibulum fringilla varius augue. Nunc viverra. Aliquam erat " +
                        "volutpat. Morbi eros est, ullamcorper eget, ornare quis, dignissim nec, diam. Donec " +
                        "molestie imperdiet odio. Curabitur lacus orci, porta nec, porttitor et, dignissim ut, " +
                        "leo. Etiam id neque non ipsum faucibus malesuada. Sed mollis.\n\n"+
                        "Cras et tellus. Aliquam sodales lobortis sem. In id diam. Nam condimentum. " +
                        "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nullam purus leo, " +
                        "fermentum sed, lobortis ac, interdum vitae, nunc. Aliquam erat volutpat. Sed " +
                        "lectus lacus, imperdiet consectetuer, tempus at, condimentum et, arcu. Ut neque. " +
                        "Duis suscipit vestibulum lacus. Nullam suscipit sagittis felis. Ut convallis. " +
                        "Etiam sed sem. Curabitur ac sapien. Aliquam gravida risus eu nisl. Vestibulum erat " +
                        "dolor, dictum non, porttitor vel, sollicitudin a, tellus.");
                send(session, ".");
                break;
            case 4 : send(session, "QUIT");
                break;
        }
        
        step++;
        session.setAttribute("step", new Integer(step));
    }

    @Override
    public void exceptionCaught( IoSession session, Throwable cause )
    {
        session.close();
    }
    
    /**
     * Sends a message to the server. A newline will
     * be appended to the message.
     */
    public void send(IoSession session, String msg)
    {
        // Force \r\n since println() behaves differently on different platforms
        session.write(msg + "\r\n");
    }
}