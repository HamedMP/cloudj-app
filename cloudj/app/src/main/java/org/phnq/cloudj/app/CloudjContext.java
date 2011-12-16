package org.phnq.cloudj.app;

import org.phnq.core.webapp.Context;

/**
 *
 * @author pgostovic
 */
public class CloudjContext extends Context {

    public static CloudjContext getCurrent() {
        return (CloudjContext) Context.getCurrent();
    }
}
