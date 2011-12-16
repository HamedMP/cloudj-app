/* #require ext.json2 */
/* #require phnq.notify */

phnq.net =
{
    getJSON: function(url, params, fn, errorFn)
    {
        req("GET", url, params, function(resp, status)
        {
            try
            {
                phnq.notify.post("phnq.net.ResponseWillBeHandled", resp, status);
            }
            catch(ex)
            {
                log.error(ex);
            }
            fn(resp, status);
        }, errorFn);
    },
    
    postJSON: function(url, obj, fn, errorFn)
    {
        req("POST", url, obj, function(resp, status)
        {
            try
            {
                phnq.notify.post("phnq.net.ResponseWillBeHandled", resp, status);
            }
            catch(ex)
            {
                log.error(ex);
            }
            fn(resp, status);
        }, errorFn);
    }
};

var domainRe = /^https?:\/\/([^/:]*)/;
var getDomainForUrl = function(url)
{
    var m = domainRe.exec(url);
    if(!m)
        m = domainRe.exec(location.href);
    
    return m[1];
};

var pageDomain = getDomainForUrl(location.href);
var phnqDomain = getDomainForUrl(phnq.serverContextBase);

var req = function(method, url, data, fn, errorFn)
{
    errorFn = errorFn || fn;

    if(!url.match(/^https?:\/\//))
        url = phnq.serverContextBase + url;
    
    var urlDomain = getDomainForUrl(url);
    if(urlDomain != phnqDomain)
    {
        data.proxyDestUrl = url;
        url = phnq.serverContextBase + "/phnqlets/proxy";
        urlDomain = phnqDomain;
    }
    
    var sameDomain = pageDomain == urlDomain;
    
    if(sameDomain) // use XHR
    {
        $.ajax(
        {
            url: url,
            type: method,
            data: method == "POST" ? JSON.stringify(data) : data,
            dataType: "json",
            contentType: "application/json; charset=utf-8"
        }).success(function(resp, status, xhr)
        {
            fn(resp, xhr.status);
        }).error(function(resp, status, xhr)
        {
            errorFn(resp, xhr.status);
        });
    }
    else // use JSONP
    {
        url = url + "?jsoncallback=?"; // assume now Q params in url for now...
        
        var params = (method == "POST" || method == "PUT") ?
        {
            jsonmethod:  method,
            jsonbody: JSON.stringify(data)
        } : data;
        
        $.getJSON(url, params, function(resp)
        {
            if(resp.status == 200)
                fn(resp.data, resp.status);
            else
                errorFn(resp.data, resp.status)
        });
    }
};
