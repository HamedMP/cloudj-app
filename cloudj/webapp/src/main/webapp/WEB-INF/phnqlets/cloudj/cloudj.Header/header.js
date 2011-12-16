/* #require phnq.notify */

var phnqlet =
{
    onInsert: function($$)
    {
        $$("p").hide();
        
        phnq.notify.listen("cloudj.Player.playerReady", function(user)
        {
            if(!user)
            {
                $$("p").fadeIn();
            }
        });
    }
};
