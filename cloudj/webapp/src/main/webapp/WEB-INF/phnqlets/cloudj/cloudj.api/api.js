/* #require phnq.net */

window.cloudj = window.cloudj || {};

var apiBase = phnq.serverContextBase + "/api";

phnq.notify.listen("phnq.net.ResponseWillBeHandled", function(resp)
{
    if(resp.tracks)
        phnq.notify.post("cloudj.api.TrackListUpdated", resp.tracks);
});

cloudj.api = 
{
    clearTracks: function(fn)
    {
        phnq.net.postJSON(apiBase + "/tracks/clear", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },

    getTracks: function(fn)
    {
        phnq.net.getJSON(apiBase + "/tracks", {}, function(res)
        {
            phnq.notify.post("cloudj.api.TrackListUpdated", res.data);
            
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    setTracks: function(tracks, fn)
    {
        phnq.net.postJSON(apiBase + "/tracks", tracks, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },

    setTracksSortOrder: function(trackIds, fn)
    {
        phnq.net.postJSON(apiBase + "/tracks/sortOrder", trackIds, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },

    appendTrack: function(track, reset, fn)
    {
        phnq.net.postJSON(apiBase + "/tracks/append?reset="+reset, track, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    appendSimilarTrack: function(fn)
    {
        phnq.net.postJSON(apiBase + "/tracks/appendSimilar", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    getPlaybackToken: function(fn)
    {
        phnq.net.getJSON(apiBase + "/player/playbackToken", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },

    getTrackPlaybackKey: function(trackId, fn)
    {
        phnq.net.getJSON(apiBase + "/tracks/"+escape(trackId)+"/playbackKey", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    getArtist: function(artistName, fn)
    {
        phnq.net.getJSON(apiBase + "/artists/"+escape(artistName), {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    seedRandomHypedTrack: function(fn)
    {
        phnq.net.postJSON(apiBase + "/tracks/seedRandomHyped", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },

    getRandomHypedTrack: function(fn)
    {
        phnq.net.getJSON(apiBase + "/tracks/randomHyped", {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    },
    
    searchForTracks: function(query, fn)
    {
        phnq.net.getJSON(apiBase + "/tracks/search/"+escape(query), {}, function(res)
        {
            if(fn)
                fn(res.data);
        }, function(res)
        {
            log.error(res);
        });
    }
};
