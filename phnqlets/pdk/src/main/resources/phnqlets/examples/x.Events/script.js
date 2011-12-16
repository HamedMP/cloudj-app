/* #require phnq.notify */

var phnqlet =
{
    onInsert: function($$)
    {
        $$("input").click(function()
        {
            alert("Clicked");
        });
    }
};
