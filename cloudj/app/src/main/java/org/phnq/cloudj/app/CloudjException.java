package org.phnq.cloudj.app;

/**
 *
 * @author pgostovic
 */
public class CloudjException extends Exception {

    /**
     * Creates a new instance of <code>CloudjException</code> without detail message.
     */
    public CloudjException() {
    }

    /**
     * Constructs an instance of <code>CloudjException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CloudjException(String msg) {
        super(msg);
    }

    public CloudjException(Throwable thrwbl) {
        super(thrwbl);
    }

    public CloudjException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
