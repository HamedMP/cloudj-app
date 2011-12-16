package org.phnq.phnqlets.pdk;

/**
 *
 * @author pgostovic
 */
public class PhnqletsException extends Exception {

    /**
     * Creates a new instance of <code>PhnqletsException</code> without detail message.
     */
    public PhnqletsException() {
    }

    /**
     * Constructs an instance of <code>PhnqletsException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PhnqletsException(String msg) {
        super(msg);
    }
}
