/* #require phnq.notify */
/* #require cloudj.api */
/* #require cloudj.Track */
/* #require ext.isotope */

var trackElmntsToIdArray = function(elmnts)
{
    var ids = [];
    $(elmnts).each(function()
    {
        ids.push(this.id);
    });
    return ids;
};

var phnqlet =
{
    linkTracksByItems: function(items)
    {
        var _this = this;
        var prevTrack = null;
        $(items).each(function()
        {
            var track = _this.tracksById[this.id];
            track.previous = prevTrack;
            track.next = null;
            if(prevTrack)
                prevTrack.next = track;
            
            prevTrack = track;
        });
    },

    onInsert: function($$)
    {
        var _this = this;
        
        $$(".tracks").isotope({});

        phnq.notify.listen("cloudj.api.TrackListUpdated", function(tracks)
        {
            _this.setTracks(tracks);
        });
        
        $$(".resetButton").click(function()
        {
            cloudj.api.clearTracks();
        });

        $$(".shuffleButton").click(function()
        {
            $$(".tracks").isotope("shuffle", function(items)
            {
                _this.linkTracksByItems(items);
                
                var ids = [];
                $(items).each(function()
                {
                    ids.push(this.id);
                });
                
                cloudj.api.setTracksSortOrder(ids, function()
                {
                    log.debug("tracks sort order persisted");
                });
            });
        });
        
        phnq.notify.listen("cloudj.Track.trackWasClicked", function(id)
        {
            var track = _this.tracksById[id];
            if(track)
                phnq.notify.post("cloudj.TrackList.trackShouldBePlayed", track);
        });
        
        phnq.notify.listen("cloudj.Player.currentTrackChanged", function(track)
        {
            $$(".tracks").isotope("reLayout", function(items)
            {

            });
        });
    },
    
    onShow: function($$)
    {
        var _this = this;

        cloudj.api.getTracks(); // tracks get update from notification
    },
    
    eachTrack: function(fn)
    {
        var track = null;
        for(var k in this.tracksById)
        {
            track = this.tracksById[k];
            if(track.previous == null)
                break;
        }
        
        if(track)
        {
            fn.apply(track);
            while(track.next)
            {
                fn.apply(track = track.next);
            }
        }
    },
    
    setTracks: function(tracks)
    {
        var _this = this;
        var $$ = this.get$$();

        if(tracks.length == 0)
            $$().addClass("empty");
        else
            $$().removeClass("empty");

        this.tracksById = {};
        $(tracks).each(function()
        {
            _this.tracksById[this.id] = this;
            phnq.notify.post("cloudj.api.TrackUpdated", this);
        });
        
        this.linkTracksByItems(tracks);
        
        var currentNum = $$(".tracks > .cloudj\\.Track").length;
        if(tracks.length > currentNum)
        {
            // Assume all new items are added at the end
            tracks.splice(0, currentNum);

            var buf = [];
            $(tracks).each(function()
            {
                buf.push(phnq.phnqlets.getPlaceholderMarkup("cloudj.Track", this));
            });
            $$(".tracksStaging").html(buf.join(""));

            phnq.phnqlets.scan(function(trackPhnqlets)
            {
                $$(".tracks").isotope("insert", $($$(".tracksStaging").html()), function(items)
                {
                    _this.trackIndex = trackElmntsToIdArray(items);
                });
                $$(".tracksStaging").html("");
                
                $(trackPhnqlets).each(function()
                {
                    this.ready(this.get$$());
                });
            });
        }
        else
        {
            var allIds = {};
            $(tracks).each(function()
            {
                allIds[this.id] = true;
            });
            
            var toRemove = [];
            $$(".tracks > .cloudj\\.Track").each(function()
            {
                if(!allIds[this.id])
                    toRemove.push(this);
            });
            
            $$(".tracks").isotope("remove", $(toRemove));
            $$(".tracks").isotope("reLayout", function(items)
            {
                _this.trackIndex = trackElmntsToIdArray(items);
            });
        }
    }
};
