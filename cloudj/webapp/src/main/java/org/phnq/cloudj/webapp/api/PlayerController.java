package org.phnq.cloudj.webapp.api;

import java.net.URI;
import javax.servlet.http.HttpServletResponse;
import org.phnq.cloudj.app.Cloudj;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author pgostovic
 */
@Controller
@RequestMapping("/player")
public class PlayerController {

    @RequestMapping(value = "/playbackToken", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getPlaybackToken(@RequestHeader(value = "Referer", required = false) final String refererHeader, final HttpServletResponse res) {
        return new APIResponse(res) {

            @Override
            protected Object getResponseData() throws Throwable {
                String domain = null;
                if (refererHeader == null) {
                    res.setStatus(400);
                    setStatus(400, "Bad Request: no Referer header", null);
                } else {
                    URI refUri = new URI(refererHeader);
                    domain = refUri.getHost();
                }
                return Cloudj.getPlaybackToken(domain);
            }
        };
    }
}
