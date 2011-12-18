/* #require ext.swfobject */
/* #require phnq.notify */
/* #require cloudj.api */

var nextAvailableTrack = function(track, fn)
{
    if(track)
    {
        cloudj.api.getTrackPlaybackKey(track.id, function(playbackKey)
        {
            track.playbackKey = playbackKey;
            if(playbackKey)
            {
                fn(track);
            }
            else
            {
                phnq.notify.post("cloudj.Player.unableToPlayTrack", track);
                nextAvailableTrack(track.next, fn);
            }
        });
    }
    else
    {
        fn(null);
    }
};

var phnqlet = 
{
    play: function(track)
    {
        if(!this.rdio)
        {
            log.debug("Player not ready yet (play).");
            return;
        }
        
        var _this = this;
        
        if(track == this.currentTrack)
        {
            this.rdio.rdio_play();
            return;
        }

        phnq.notify.post("cloudj.progress", true, "Queueing...");

        this.currentTrack = null;
        
        this.rdio.rdio_stop();
        this.rdio.rdio_clearQueue();
        
        nextAvailableTrack(track, function(nextTrack)
        {
            if(nextTrack)
            {
                _this.rdio.rdio_play(nextTrack.playbackKey);
                _this.currentTrack = nextTrack;
            }
            else
            {
                log.error("No tracks to play.");
            }
            phnq.notify.post("cloudj.progress", false);
        });
    },
    
    pause: function()
    {
        if(!this.rdio)
        {
            log.debug("Player not ready yet (pause).");
            return;
        }
        this.rdio.rdio_pause();
    },

    stop: function()
    {
        if(!this.rdio)
        {
            log.debug("Player not ready yet (stop).");
            return;
        }

        this.currentTrack = null;
        phnq.notify.post("cloudj.Player.currentTrackChanged", null);
        this.rdio.rdio_stop();
    },

    queue: function(track)
    {
        if(!this.rdio)
        {
            log.debug("Player not ready yet (queue).");
            return;
        }
        
        var _this = this;

        nextAvailableTrack(track, function(nextTrack)
        {
            if(nextTrack)
                _this.rdio.rdio_queue(nextTrack.playbackKey);
        });
    },
    
    onInsert: function($$)
    {
        var _this = this;
        
        var objId = phnq.phnqlets.getNextId();
        $$(".playerWrapper").attr("id", objId);
        
        phnq.notify.listen("cloudj.api.TrackListUpdated", function(tracks)
        {
            if(tracks.length == 0)
                _this.stop();
        });
        
        cloudj.api.getPlaybackToken(function(pbt)
        {
            window.cloudj = window.cloudj || {};
            cloudj.playerCallbacks = cloudj.playerCallbacks || {};
            cloudj.playerCallbacks[objId] = 
            {
                ready: function(user)
                {
                    phnq.notify.post("cloudj.Player.playerReady", user);
                    
                    _this.rdio = $("#"+objId).get(0);
                },
                
                playStateChanged: function(playState)
                {
                    log.debug("playStateChanged: ", playState, _this.currentTrack);

                    phnq.notify.post("cloudj.Player.playStateChanged", playState, _this.currentTrack);

                    if(playState == 2) // stopped
                    {
                        if(_this.currentTrack)
                        {
                            phnq.notify.post("cloudj.Player.unableToPlayTrack", _this.currentTrack);

                            log.error("This track would not playback -- playing next one...")
                            nextAvailableTrack(_this.currentTrack.next, function(nextTrack)
                            {
                                if(nextTrack)
                                    _this.play(nextTrack);
                            });
                        }
                    }
                },
                
                playingTrackChanged: function(playingTrack, sourcePosition)
                {
                    log.debug("playingTrackChanged: ", playingTrack);
                    
                    if(playingTrack)
                    {
                        _this.currentTrack.duration = playingTrack.duration;
                        phnq.notify.post("cloudj.Player.currentTrackChanged", _this.currentTrack);
                    }
                    else
                    {
                        phnq.notify.post("cloudj.Player.currentTrackChanged", null);
                    }
                },
                
                positionChanged: function(pos)
                {
                    if(_this.currentTrack.duration && (_this.currentTrack.duration-pos) < 10)
                    {
                        log.debug("Queing up the next track...", _this.currentTrack.next);
                        
                        _this.currentTrack.duration = 0;
                        
                        nextAvailableTrack(_this.currentTrack.next, function(nextTrack)
                        {
                            if(nextTrack)
                            {
                                _this.currentTrack = nextTrack;
                                log.debug("queueing up next track: "+_this.currentTrack.name+" by "+_this.currentTrack.artist);
                                _this.queue(_this.currentTrack);
                            }
                            else
                            {
                                log.debug("playlist is done");
                            }
                        });
                        
                    }
                }
            };
            
            var domain = /^https?:\/\/([^/:]*)/.exec(location.href)[1];

            var flashVars =
            {
                playbackToken: pbt,
                domain: domain,
                listener: "cloudj.playerCallbacks."+objId
            };

            var params =
            {
                'allowScriptAccess': 'always'
            };
            
            var expressInstallUrl = phnq.serverContextBase + "/phnqlets/_TYPE_/static/expressInstall.swf";
            log.debug("expressInstallUrl: ", expressInstallUrl);
            swfobject.embedSWF("http://www.rdio.com/api/swf/", objId, 1, 1, "9.0.0", expressInstallUrl, flashVars, params, {}, function(obj)
            {
                if(!obj.success)
                {
                    $$().addClass("noflash");
                    alert("To play music, you'll need to install Flash.");
                }
            });
        });
    }
};

