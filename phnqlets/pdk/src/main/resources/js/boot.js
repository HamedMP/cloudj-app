(function()
{
	window.phnq = window.phnq || {};

    // phnq.serverContextBase is set dynamically.

    var $ = phnq.preExistingJQuery ? jQuery.noConflict() : jQuery;
    if(phnq.preExistingJQuery)
        jQuery = phnq.preExistingJQuery;

	phnq.createClass = function(ext)
	{
		var cls = function()
		{
			if(this.init)
				this.init.apply(this, arguments);
		};
		
		for(var k in ext)
		{
			cls.prototype[k] = ext[k];
		}
		
		return cls;
	};
	
	var loggingEnabled = !!window.console && !! window.console.log && !! window.console.log.apply;
    
	var Levels =
	{
		NONE: 0,
		ERROR: 1,
		WARN: 2,
		INFO: 3,
		DEBUG: 4
	};
	
	var pad2 = function(arg)
	{
		var s = new String(arg);
		if(s.length < 2)
			s = "0"+s;
		
		return s;
	};
	
	var Logger = phnq.createClass(
	{
		init: function(category)
		{
			this.category = category
			this.startTime = null;
		},
		
		append: function(/* levelName, level, arg1, arg2, ... */)
		{
			var levelName = arguments[0];
			
			var d = new Date();
			var buf = [];
			buf.push(d.getFullYear());
			buf.push("-");
			buf.push(pad2(d.getMonth()+1));
			buf.push("-");
			buf.push(pad2(d.getDate()));
			buf.push(" ");
			buf.push(pad2(d.getHours()));
			buf.push(":");
			buf.push(pad2(d.getMinutes()));
			buf.push(":");
			buf.push(pad2(d.getSeconds()));
			buf.push(".");
			buf.push(d.getMilliseconds());
			buf.push(" ["+levelName+"] ");
			buf.push(this.category);
			if(this.startTime)
			{
				var t = new Date().getTime()-this.startTime;
				buf.push((" ("+t+"ms)"));
				this.startTime = null;
			}
			buf.push(" -");
			
			var args = [buf.join("")];
			var argsLen = arguments.length;
			for(var i=2; i<argsLen; i++)
			{
				args.push(arguments[i]);
			}
			
			if(loggingEnabled)
				console.log.apply(console, args);
		},
		
		startTimer: function()
		{
			this.startTime = new Date().getTime();
		}
	});
	
	$.each(Levels, function(key, val)
	{
		Logger.prototype[key.toLowerCase()] = function()
		{
			if(!loggingEnabled || val > phnq.log.level)
				return undefined;
			
			var args = [key, val];
			var argsLen = arguments.length;
			for(var i=0; i<argsLen; i++)
			{
				args.push(arguments[i]);
			}
			return this.append.apply(this, args);
		};
	});
	
	var logMatch = /[?&]log=([^&]*)[&$]?/.exec(location.search);
	var logLevelName = logMatch ? logMatch[1].toUpperCase() : "none";
	var logLevel = Levels[logLevelName] || Levels.NONE;
	
	phnq.log =
	{
		level: logLevel,
		
		Logger: Logger
	};
    
    phnq.exec = function(category, fn)
    {
        var log = new Logger(category);
        log.info("init logger");
        fn(log, $, $);
    };
})();
