package org.phnq.cloudj.webapp.api;

import javax.servlet.http.HttpServletResponse;
import org.phnq.cloudj.app.Artist;
import org.phnq.cloudj.app.Cloudj;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author pgostovic
 */
@Controller
@RequestMapping("/artists")
public class ArtistsController {

    @RequestMapping(value = "/{name:.*}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public @ResponseBody
    APIResponse getArtist(@PathVariable("name") final String name, final HttpServletResponse res) {
        return new APIResponse(res) {

            @Override
            protected Object getResponseData() throws Throwable {
                Artist artist = Cloudj.getArtist(name);
                
                if(artist == null)
                    res.setStatus(404);
                
                return artist;
            }
        };
    }
}
