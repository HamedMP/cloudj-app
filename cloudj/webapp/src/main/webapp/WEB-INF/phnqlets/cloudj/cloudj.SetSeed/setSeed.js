/* #require ext.jqueryui */
/* #require phnq.net */
/* #require phnq.notify */

var phnqlet =
{
    onInsert: function($$)
    {
        var _this = this;
        
        $$(".errorMsg").hide();
        
        $$(".results").isotope({});
        
        $$(".queryField").autocomplete({
            source: function(input, fn)
            {
                var term = input.term;

                phnq.net.getJSON("http://www.last.fm/search/autocomplete", {force:"1", q:term}, function(res)
                {
                    var trackNames = [];
                    $(res.response.docs).each(function()
                    {
                        if(this.restype == 9)
                            trackNames.push({track:this.track, artist:this.artist, label:(this.track+", "+this.artist)});
                    });
                    fn(trackNames);
                })
            },

            select: function(evt, ui)
            {
                addTrack(ui.item.track, ui.item.artist);
            }
        });
        
        phnq.notify.listen("cloudj.progress", function(prog)
        {
            if(prog)
                $$().addClass("progress");
            else
                $$().removeClass("progress");
        });

        
        var reset = function()
        {
            $$(".results").isotope("remove", $$(".results > *"));
            $$(".results").isotope("reLayout");
        };
        
        phnq.notify.listen("cloudj.api.TrackListUpdated", function(tracks)
        {
            if(tracks.length == 0)
                $$(".queryField").focus();
        });
        
        $$(".queryField").keydown(function()
        {
            _this.hideError();
            reset();
        });
    
        var addTrack = function(name, artist, fn)
        {
            reset();

            phnq.notify.post("cloudj.progress", true, "Generating playlist...");

            cloudj.api.appendTrack({artist:artist,name:name}, false, function(res)
            {
                $$(".queryField").val("");
                if(res)
                {
                    var num = 14;

                    var doAdd = function()
                    {
                        cloudj.api.appendSimilarTrack(function()
                        {
                            num--;
                            if(num > 0)
                            {
                                doAdd();
                            }
                            else
                            {
                                phnq.notify.post("cloudj.progress", false);
                                if(fn)
                                    fn();
                            }
                        });
                    };

                    doAdd();
                }
                else
                {
                    phnq.notify.post("cloudj.progress", false);
                    _this.showError("Sorry, there were no results for that search.");
                }
            });
        };
    
        $$("form").submit(function()
        {
            search();
            return false;
        });
     
        $$(".searchButton").click(function()
        {
            search();
        });
        
        var search = function()
        {
            var query = $$(".queryField").val();
            if(!query.match(/\S/))
                return;

            $$(".queryField").blur();
            
            phnq.notify.post("cloudj.progress", true, "Searching...");
            
            cloudj.api.searchForTracks(query, function(results)
            {
                phnq.notify.post("cloudj.progress", false);
                
                if(results.length == 0)
                    _this.showError("Sorry, there were no results for that search.");

                var buf = [];
                $(results).each(function()
                {
                    if(this.imageLarge)
                        buf.push(phnq.phnqlets.getPlaceholderMarkup("cloudj.SearchResult", this));
                });
                $$(".resultsStaging").html(buf.join(""));

                phnq.phnqlets.scan(function(resultPhnqlets)
                {
                    $$(".results").isotope("insert", $($$(".resultsStaging").html()), function(items)
                    {
                        $$(".results > *").click(function()
                        {
                            var trackName = $("h1", this).text();
                            var artistName = $("h2", this).text();
                            addTrack(trackName, artistName);
                        });
                    });
                    $$(".resultsStaging").html("");
                    $$(".queryField").val("");
                });
            });
        };
        
        $$(".addRandomButton").click(function()
        {
            phnq.notify.post("cloudj.progress", true, "Generating playlist...");
            
            cloudj.api.getRandomHypedTrack(function(track)
            {
                addTrack(track.name, track.artist, function()
                {
                    phnq.notify.post("cloudj.progress", false);
                });
            });
        });
        
        $$(".addSimilarTrackButton").click(function()
        {
            cloudj.api.appendSimilarTrack(function(){});
        });

        $$(".addSeveralSimilarTracksButton").click(function()
        {
            var num = 10;
            
            var doAdd = function()
            {
                cloudj.api.appendSimilarTrack(function()
                {
                    num--;
                    if(num > 0)
                        doAdd();
                });
            };
            
            doAdd();
        });
    },
    
    showError: function(msg)
    {
        var $$ = this.get$$();
        $$(".errorMsg").text(msg).fadeIn();
    },
    
    hideError: function()
    {
        var $$ = this.get$$();
        $$(".errorMsg").fadeOut();
    }
};
    