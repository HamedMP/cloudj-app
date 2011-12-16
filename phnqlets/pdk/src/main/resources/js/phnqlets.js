phnq.exec("phnq.phnqlets", function(log, $)
{
    var phnqletsBase = phnq.serverContextBase + "/phnqlets/";
    
    window.phnq = window.phnq || {};
    
    var phnqletClasses = {};
    var requestedPhnqlets = {};
	var currentScanContext = null;
    
	var paramsTmp = {};
	var nextParamsTmpIdx = 0;
	var tmpParamsStore =
	{
		save: function(params)
		{
			var k = "p_"+(nextParamsTmpIdx++);
			paramsTmp[k] = params;
			return k;
		},

		retrieve: function(k)
		{
			return paramsTmp[k];
		}
	};

    var nextId = 0;
    var getNextId = function()
    {
        return "phnqlet_"+(nextId++);
    };
    
	var ScanContext = phnq.createClass(
	{
		init: function()
		{
			this.qGroups = {};
			this.onCompleteHandlers = [];
		},

		isInScanCycle: function()
		{
			return !!currentScanContext;
		},

		enqueue: function(groupKey, fn, args)
		{
			var qGroup = this.qGroups[groupKey];
			if(!qGroup)
			{
				qGroup = this.qGroups[groupKey] =
				{
					fn: fn,
					q: [],
					invokeEach: function(invFn)
					{
						for(var i=0; i<this.q.length; i++)
						{
							invFn.apply(null, this.q[i]);
						}
					}
				}
			}

			qGroup.q.push(args || arguments.callee.caller.arguments);

			if(!this.isInScanCycle())
				this.flush();
		},

		flush: function()
		{
			for(var k in this.qGroups)
			{
				var qGroup = this.qGroups[k];
				qGroup.fn(qGroup);
			}
			
			$(this.onCompleteHandlers).each(function()
			{
				this();
			});
			
			paramsTmp = {}; // clear the temp params store.
		}
	});

	var handleLoadResult = function(data)
	{
        // Add CSS to page
        $("head").append("<style type='text/css'>"+data.css+"</style>");
        
        // Execute JavaScript
        try
        {
            eval(data.js);
        }
        catch(ex)
        {
            log.error("ERROR evaluating loaded JS: "+ ex, data.js);
        }
        
        // Create classes and markup templates
        $(data.loadedTypes).each(function()
        {
            var type = this;
            var phnqletCls = phnqletClasses[type] || phnq.phnqlets.create(type);
            var markup = data.markup[type];
            if(markup)
                phnqletCls.prototype.tmplt = new phnq.tmplt.Template(markup);
        });
	};

    phnq.phnqlets =
    {
        getNextId: function()
        {
            return getNextId();
        },
    
		getScanContext: function()
		{
			return currentScanContext || new ScanContext();
		},

		/**
		 *  This allows you to group similar function calls together by some
		 *  "groupKey", deferring their invocations until the end of the current
		 *  phnqlet scan cycle.
		 */
		groupInvocations: function(groupKey, callback)
		{
			this.getScanContext().enqueue(groupKey, callback, arguments.callee.caller.arguments);
		},

        getPlaceholderMarkup: function(type, params)
        {
            params = params || {};
            params.type = type;
			return "<span class='phnqlet' tmp_params_key='"+tmpParamsStore.save(params)+"'></span>";
		},

		createPositionedPhnqlet: function(type, params, fn)
		{
			params = params || {};
			params._do_not_show = "true";
			var posPhnqletId = params._id = getNextId();
			
			var markup = this.getPlaceholderMarkup(type, params);

			phnq.phnqlets.getScanContext().onComplete(function()
			{
				$(document.body).append(markup);

				phnq.phnqlets.scan(function(addedPhnqlets)
				{
					var posPhnqlet = null;

					$(addedPhnqlets).each(function()
					{
						var phnqlet = this;
						if(phnqlet.elmntId == posPhnqletId)
						{
							var elmnt = phnqlet.getElement();
							elmnt.style.position = "absolute";

							phnqlet.moveTo = function(x, y)
							{
								$(this.getElement()).offset({left:x, top:y});
							};

							posPhnqlet = phnqlet;
						}
					});

					phnq.phnqlets.scan(function(moreAddedPhnqlets)
					{
						if(fn)
							fn(posPhnqlet);
					});
				});
			});
		},

        create: function(type, objExt)
        {
            var phnqletCls = phnq.createClass(objExt||{});
            phnqletCls.prototype.getElement = function()
            {
                return this.elmntId ? document.getElementById(this.elmntId) : null;
            };

            phnqletCls.prototype.get$$ = function()
            {
                if(!this.$$)
                {
                    var _this = this;
                    this.$$ = function(sel)
                    {
                        var phnqletElmnt = _this.getElement();
                        return $(sel||phnqletElmnt, phnqletElmnt);
                    };
                }
                return this.$$;
            };

            phnqletCls.prototype.insert = function()
            {
				// onInsert gets called even if the phnqlet has no element.
				if(this.onInsert)
                {
					if(this.onInsert(this.get$$()) == false)
						this._showOnInsert = false;
                }
            };
            phnqletCls.prototype.show = function()
            {
                if(this.getElement())
                {
                    if(this.doShowTransition)
                        this.doShowTransition(this.get$$());
                    else
                        this.getElement().style.display = "";
                    
                    if(this.onShow)
                        this.onShow(this.get$$());
                }
            };
            phnqletCls.prototype.hide = function()
            {
                if(this.getElement())
                {
                    if(this.doHideTransition)
                        this.doHideTransition(this.get$$());
                    else
                        this.getElement().style.display = "none";
                        
                    if(this.onHide)
                        this.onHide(this.get$$());
                }
            };
			phnqletCls.prototype.destroy = function()
			{
				if(this.onDestroy)
					this.onDestroy();

				var elmnt = this.getElement();
				if(elmnt)
				{
					elmnt.phnqlet = null;
					$(elmnt).remove();
				}

				for(var k in this)
				{
					delete this[k];
				}
			};
            return phnqletClasses[type] = phnqletCls;
        },
        
        scan: function(fn, options, addedPhnqlets)
        {
			if(currentScanContext)
				return; // already scanning -- no need

			this.scanInternal(fn, options);
		},

		scanInternal: function(fn, options, addedPhnqlets)
		{
			currentScanContext = this.getScanContext();
            var toLoadObj = {};
            var foundPlaceholders = false;
            options = options || {};

            addedPhnqlets = addedPhnqlets || [];
            
            /*
             *  Find all phnqlet placeholders and either replace them with
             *  instances, or load them.
             */
            $("span.phnqlet").each(function()
            {
				foundPlaceholders = true;
				var previousSibling = this.previousSibling;
				var parentNode = this.parentNode;

				var type = $(this).attr("type");
				var params;
				if(type)
                {
					params = {};
					$(this.attributes).each(function()
					{
						params[this.name] = this.value;
					});
				}
				else
				{
					var paramsKey = $(this).attr("tmp_params_key");
					params = tmpParamsStore.retrieve(paramsKey);
					type = params.type;
				}
                
                var phnqletCls = phnqletClasses[type];
                if(phnqletCls)
                {
					var showOnInsert = params._do_not_show != "true";
					delete params._do_not_show;
					
                    var phnqlet = new phnqletCls(params);

					if(params["var"])
						window[params["var"]] = phnqlet;

					delete params["var"];

					var childNodes = $("span.phnqlet", this).remove();
					$(childNodes).each(function()
					{
						this.parentPhnqlet = phnqlet;
					});

					if(this.parentPhnqlet)
					{
						phnqlet.parentPhnqlet = this.parentPhnqlet;
						this.parentPhnqlet = null;
						phnqlet.parentPhnqlet.childPhnqlets = phnqlet.parentPhnqlet.childPhnqlets || [];
						phnqlet.parentPhnqlet.childPhnqlets.push(phnqlet);
						showOnInsert = false;
					}
                    
                    addedPhnqlets.push(phnqlet);
                    
                    if(phnqlet.tmplt)
                    {
                        var markup = phnqlet.tmplt.getParameterizedMarkup(params);

                        $(this).replaceWith(markup);
                        
                        var phnqletElmnt = previousSibling ? previousSibling.nextSibling : parentNode.firstChild;
						phnqletElmnt.id = params._id || phnqletElmnt.id || getNextId();
                        phnqlet.elmntId = phnqletElmnt.id;
						phnqlet._showOnInsert = showOnInsert;

						/*
						*  I don't like this because hanging stuff on DOM elements
						*  can create mem leaks in IE -- for now, gotta live
						*  with it...
						*/
						phnqletElmnt.phnqlet = phnqlet;
						
						$(phnqletElmnt).append(childNodes);
                    }
                    else
                    {
						if(childNodes.length)
							$(this).replaceWith(childNodes);
						else
							$(this).remove();
                    }
                }
                else if(requestedPhnqlets[type])
                {
                    /**
                     *  If there is no phnqlet class for this type AND it has
                     *  already been requested, then it must not have loaded
                     *  properly.
                     */
					var errStyle = phnq.log.level == 0 ? "display:none" : "background:red; color:white; padding:5px; margin:5px";
					$(this).replaceWith("<span style='"+errStyle+"'><b>Error:</b> "+type+"</span>");
                }
                else
                {
                    toLoadObj[type] = true;
                }
            });
            
            var toLoad = [];
            for(var type in toLoadObj)
            {
                if(!requestedPhnqlets[type])
                {
                    toLoad.push(type);
                    requestedPhnqlets[type] = true;
                }
            }
            
            /**
             *  If any need to be loaded, then load them and then call scan
             *  again.
             */
            if(toLoad.length > 0)
            {
                $.getJSON(phnqletsBase+"load?jsoncallback=?",
                {
                    types:toLoad.join(",")
                }, function(resp)
                {
                    var data = resp.data;
                    
					log.debug("types to load: ", toLoad);
					log.debug("types actually loaded: ", data.loadedTypes);

					handleLoadResult(data);
                    
                    // Scan again...
                    phnq.phnqlets.scanInternal(fn, options, addedPhnqlets);
                });
            }
            else if(foundPlaceholders)
            {
                phnq.phnqlets.scanInternal(fn, options, addedPhnqlets);
            }
            else
            {
				addedPhnqlets.sort(function(p1, p2)
				{
					if(p1.parentPhnqlet == p2)
						return -1;
					else if(p2.parentPhnqlet == p1)
						return 1;
					else
						return 0;
				});

				// Call all the insert/show methods for added phnqlets
                $(addedPhnqlets).each(function()
                {
                    this.insert();
					if(this._showOnInsert)
						this.show();
						
					delete this._showOnInsert;
                });

				var ctx = currentScanContext;
				currentScanContext = null; // null this before flush to avoid infinite recursion...
				
				// this gets called when the whole process is done.
				if(fn)
					fn(addedPhnqlets);
				
				ctx.flush();
            }
        }
    };
    
    $(function()
    {
		if(window.initLoadResult)
		{
			handleLoadResult(window.initLoadResult);
			window.initLoadResult = null;
			$("#initLoadResult").remove();
		}
	
        phnq.phnqlets.scan();
    });
});
