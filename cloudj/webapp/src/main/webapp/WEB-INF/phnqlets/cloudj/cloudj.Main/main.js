/* #require cloudj.api */
/* #require phnq.notify */

window.cloudj = window.cloudj || {};

window.cloudj.Main =
{
};

var phnqlet =
{
    onInsert: function($$)
    {
        phnq.notify.listen("cloudj.Player.currentTrackChanged", function(track)
        {
            if(track)
                $$().addClass("hasCurrentTrack");
            else
                $$().removeClass("hasCurrentTrack");
        });
    }
};
