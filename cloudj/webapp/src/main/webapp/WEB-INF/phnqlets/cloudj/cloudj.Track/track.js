/* #require phnq.notify */

var phnqlet =
{
    init: function(track)
    {
        this.track = track;
        
        var _this = this;
        
        // Update this phnqlet's track reference if needed...
        phnq.notify.listen("cloudj.api.TrackUpdated", function(_track)
        {
            if(_track.id == _this.track.id)
                _this.track = _track;
        });
    },

    ready: function($$)
    {
        var _this = this;

        $$().removeClass("loading");
        
        $$().click(function()
        {
            if($$().hasClass("playing"))
                cloudjPlayer.pause();
            else
                cloudjPlayer.play(_this.track);
        });
        
        $$().hover(function()
        {
            $$().addClass("playable");
        }, function()
        {
            $$().removeClass("playable");
        });
        
        this.errorImg = $$(".trackImage").attr("src");
        
        $$(".trackImage").error(function()
        {
            $$(".trackImage").attr("src", _this.errorImg);
        });
        
        $$(".trackImage").attr("src", _this.track.imageLarge);


        phnq.notify.listen("cloudj.Player.unableToPlayTrack", function(track)
        {
            if(track == _this.track)
            {
                log.debug("unableToPlayTrack: ", track.name, track.artist);
                $$().addClass("unableToPlay");
            }
        });

        phnq.notify.listen("cloudj.Player.currentTrackChanged", function(track)
        {
            if(track == _this.track)
                $$().addClass("current");
            else
                $$().removeClass("current");
        });
        
        phnq.notify.listen("cloudj.Player.playStateChanged", function(playState, track)
        {
            $$().removeClass("playing");
            $$().removeClass("buffering");
            
            if(track == _this.track)
            {
                if(playState == 1)
                    $$().addClass("playing");
                else if(playState == 3)
                    $$().addClass("buffering");
            }
        });
    }
};
