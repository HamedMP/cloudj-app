package org.phnq.cloudj.webapp.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.phnq.cloudj.app.CloudjException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pgostovic
 */
public class APIResponse {

    private static final Logger logger = LoggerFactory.getLogger(APIResponse.class);
    private long responseTime;
    private Map<String, Object> status;
    private Object data;

    public APIResponse() {
        this(null);
    }
    
    public APIResponse(HttpServletResponse resp) {
        this.data = null;
        this.status = new HashMap<String, Object>();
        this.setStatus(200, "ok", null);

        long startTime = System.currentTimeMillis();
        
        try {
            this.setData(this.getResponseData());
            responseTime = System.currentTimeMillis() - startTime;
        } catch (CloudjException ex) {
            this.setStatus(400, "Bad Request", ex);
            if(resp != null)
                resp.setStatus(400);
        } catch (Throwable t) {
            this.setStatus(500, "Server Error", t);
            if(resp != null)
                resp.setStatus(500);
        }
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public final void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return this.data;
    }

    public final void setStatus(int code, String key, Throwable t) {
        this.setStatusAttribute("code", new Integer(code));
        this.setStatusAttribute("key", key);

        if (t != null) {
            this.setStatusAttribute("message", t.getClass().getName() + ": " + t.getMessage());

            if (t != null) {
                List<String> stackTrace = new ArrayList<String>();
                for (StackTraceElement ste : t.getStackTrace()) {
                    stackTrace.add(ste.toString());
                }
                this.setStatusAttribute("stackTrace", stackTrace);
            }
        }
    }

    public void setStatusAttribute(String key, Object value) {
        this.status.put(key, value);
    }

    public Map<String, Object> getStatus() {
        return this.status;
    }

    protected Object getResponseData() throws Throwable {
        return null;
    }
}
