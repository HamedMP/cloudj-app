package org.phnq.clients.lastfm;

/**
 *
 * @author pgostovic
 */
public class LastFMException extends Exception {

    /**
     * Creates a new instance of <code>LastFMException</code> without detail message.
     */
    public LastFMException() {
    }

    /**
     * Constructs an instance of <code>LastFMException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public LastFMException(String msg) {
        super(msg);
    }

    public LastFMException(Throwable thrwbl) {
        super(thrwbl);
    }

    public LastFMException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }
}
