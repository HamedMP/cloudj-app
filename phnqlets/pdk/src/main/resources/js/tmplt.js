phnq.exec("phnq.tmplt", function(log, $)
{
    window.phnq = window.phnq || {};

    var PARAM_REGEX = /_\{([^\}]*)\}(\{([^\}]*)\})?/g;

    phnq.tmplt =
    {
        all: {},

        scanDOM: function()
        {
            $(".tmplt").detach().each(function(i, elmnt)
            {
                $(elmnt).removeClass("tmplt");
                var tmpltName = $(elmnt).attr("class").split(/\s/)[0];
                phnq.tmplt.all[tmpltName] = new phnq.tmplt.Template(elmnt);
            });
        },

		isSet: function(val)
		{
			return val != null && val != undefined;
		},

        Template: phnq.createClass(
        {
            init: function(src)
            {
                this.txt = (typeof(src) == "string" ? src : $(src).html()).replace(/___/g, "");
                this.comps = null;
                this.defaultValues = {};
            },

            parse: function()
            {
                this.comps = [];
                var txt = this.txt;
                var m;
                var idx = 0;
                while((m = PARAM_REGEX.exec(txt)))
                {
                    this.comps.push(txt.substring(idx, m.index)); // static text
                    this.comps.push(m[1]); // param name

                    if(m[3])
                        this.defaultValues[this.comps.length-1] = m[3];
                    
                    idx = PARAM_REGEX.lastIndex;
                }
                this.comps.push(txt.substring(idx)); // leftover static text
            },

            getParameterizedMarkup: function(params)
            {
                if(!this.comps)
                    this.parse();

				var comps = this.comps;
				var compsLen = comps.length;
				
				var paramVals;
				try // try fast method first -- all or nothing...
				{
					var evalBuf = [];
					for(var i=1; i<compsLen; i+=2)
					{
	                    var comp = comps[i];
						var defaultVal = this.defaultValues[i];
						if(defaultVal == undefined)
						{
							evalBuf.push("p"+i+":"+comp);
						}
						else
						{
							var val = "phnq.tmplt.isSet("+comp+")?"+comp+":"+defaultVal;
							evalBuf.push("p"+i+":("+val+")");
						}
					}

					with(params)
					{
						paramVals = eval("({"+evalBuf.join(",")+"})");
					}
				}
				catch(ex) // on error, try the slow, granular method...
				{
					log.warn("Caught error in fast parameterization -- using slow...", ex);
					
					paramVals= {};
					with(params)
					{
						for(var i=1; i<compsLen; i+=2)
						{
		                    var comp = comps[i];
							var defaultVal = this.defaultValues[i];
							try
							{
								var val = eval(comp);
								paramVals["p"+i] = phnq.tmplt.isSet(val) ? val : defaultVal;
							}
							catch(exx)
							{
								paramVals["p"+i] = defaultVal;
							}
						}
					}
				}
				
				var buf = [];
				for(var i=0; i<compsLen; i++)
				{
                    var comp = comps[i];
                    if(i % 2)
						buf.push(paramVals["p"+i]);
					else
                        buf.push(comps[i]);
				}

                return buf.join("");
            }
        })
    };

    $(function()
    {
        phnq.tmplt.scanDOM();
    });
});
