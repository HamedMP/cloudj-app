var phnqlet =
{
    onInsert: function($$)
    {
        phnq.notify.listen("cloudj.api.TrackListUpdated", function(tracks)
        {
            if(tracks.length)
                $$().removeClass("emptyTrackList");
            else
                $$().addClass("emptyTrackList");
        });
    }
};
    