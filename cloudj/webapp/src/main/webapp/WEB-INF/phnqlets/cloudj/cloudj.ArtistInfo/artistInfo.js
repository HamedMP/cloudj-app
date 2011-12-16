/* #require cloudj.api */
/* #require phnq.notify */

var phnqlet =
{
    onInsert: function($$)
    {
        var _this = this;
        
        this.progCount = 0;
        
        $$("p.progText").hide();

        this.defaultImg = $$("img.grey").attr("src");
        
        phnq.notify.listen("cloudj.Player.currentTrackChanged", function(track)
        {
            if(track)
            {
                cloudj.api.getArtist(track.artist, function(artist)
                {
                    $$("img.grey").attr("src", artist.imageExtraLarge);
                    $$("h1").html(artist.name);
                    $$("p.bio").html(artist.bioSummary);
                    $$("p.bio a").attr("target", "_blank");
                });
            }
            else
            {
                _this.reset();
            }
        });
        
        phnq.notify.listen("cloudj.progress", function(prog, text)
        {
            if(prog)
                _this.startProgress(text);
            else
                _this.stopProgress();
        });
    },
    
    reset: function()
    {
        var $$ = this.get$$();
        $$("img.grey").attr("src", this.defaultImg);
        $$("h1").html("");
        $$("p.bio").html("");
    },
    
    onShow: function()
    {
    },
    
    startProgress: function(text)
    {
        var $$ = this.get$$();
        text = text || "working...";
        if(this.progCount == 0)
        {
            
            $$().addClass("progress");
            var blueImg = $$("img.blue");
            var progText = $$("p.progText");
            progText.show();
            var blue = true;
            blueImg.css("opacity", 1);

            this.progPid = setInterval(function()
            {
                blueImg.css("opacity", (blue ? 0 : 1));
                progText.css("color", (blue ? "#fff" : "#bbb"));
                blue = !blue;
            }, 700);
        }
        this.progCount++;
        if(text)
            $$("p.progText").text(text);
    },
    
    stopProgress: function()
    {
        var $$ = this.get$$();
        this.progCount--;
        if(this.progCount == 0)
        {
            $$().removeClass("progress");
            var blueImg = $$("img.blue");
            blueImg.css("opacity", 0);
            var progText = $$("p.progText");
            progText.css("color", "#bbb");
            clearInterval(this.progPid);
            this.progPid = 0;
            progText.hide();
        }
    }
};
