function getSimpleDate(time) {
  if (time.getFullYear) {
    return time.getFullYear() + "-" + (time.getMonth() + 1) + "-" + time.getDate();
  } else {
    return time;
  }
};

QueryParam = (function () {

  function QueryParam(rule) {
    this.queryUrl = "";
    this.ruleID = rule;
  }

  QueryParam.prototype.initFilter = function (filters) {
    if (filters != null) {
      var i = 0;
      var filterUrl = "";
      for (k in filters) {
        filterUrl += "filter" + i + ".name=" + k + "&";
        filterUrl += "filter" + i + ".value=" + filters[k] + "&";
        i++;
      }
      this.queryUrl += filterUrl;
    }
    return this;
  };

  QueryParam.prototype.initGroup = function (groups) {
    this.queryUrl += "group=" + groups + "&";
    return this;
  };

  QueryParam.prototype.doAnalytics = function (callbacks) {
    //remove the last &  if exist
    if (this.queryUrl[this.queryUrl.length - 1] == "&") {
      this.queryUrl = this.queryUrl.substr(0, this.queryUrl.length - 1);
    }
    var url = "/analytics/" + this.ruleID + "/data?" + this.queryUrl;

    var request = new KiiAnalyticsRequest(url);
    request.method = "GET";
    request.anonymous = true;
    request.contentType = "application/vnd.kii.GroupedAnalyticResult+json";

    var trackingCallbacks = {
      success: function (data) {
        if (callbacks != null) {
          return callbacks.success(data);
        }
      },
      failure: function (error, statusCode) {
        if (callbacks != null) {
          return callbacks.failure(error, statusCode);
        }
      }
    };
    return request.execute(trackingCallbacks, false);
  };
  return QueryParam;
})();


KiiAnalyticsRequest = (function () {
  var _thisRequest;
  _thisRequest = null;

  //set request params
  function KiiAnalyticsRequest(path) {
    _thisRequest = this;
    this.path = "/apps/" + (Kii.getAppID()) + path;
    this.method = "GET";
    this.headers = {
      "user-agent": "js/1.0",
      "accept": "*/*"
    };
    this.data = null;
    this.contentType = "application/json";
    this.anonymous = false;
    this.accept = null;
    this.success = function () {
    };
    this.failure = function () {
    };
  }

  KiiAnalyticsRequest.prototype.execute = function (callbacks, ignoreBody) {
    var ajaxData, json_text, url;

    if (callbacks != null) {
      this.success = callbacks.success != null ? callbacks.success : this.success;
      this.failure = callbacks.failure != null ? callbacks.failure : this.failure;
    }
    url = Kii.getBaseURL() + this.path;
    json_text = JSON.stringify(this.data);
    console.log("Making request[" + this.method + "] to " + url + " with path: " + this.path + " and data: " + json_text);
    //set headers for kii
    this.headers['x-kii-appid'] = Kii.getAppID();
    this.headers['x-kii-appkey'] = Kii.getAppKey();
    if (this.accept != null) {
      this.headers['accept'] = this.accept;
    }
    this.headers['x-kii-path'] = this.path;
    if (!this.anonymous) {
      this.headers['Authorization'] = "Bearer " + (Kii.getAccessToken());
    }

    //send request with ajax
    ajaxData = {
      type: this.method,
      url: url,
      dataType: "json",
      headers: this.headers,
      contentType: this.contentType,
      complete: function (xhr, status) {
        var data, errString, error, numstatus, suc;
        console.log("Completed Request[" + xhr.status + "]");
        console.log(xhr.responseText);
        numstatus = parseInt(xhr.status, 10);
        suc = 200 <= numstatus && numstatus < 400;
        data = null;
        if (!ignoreBody) {
          try {
            data = jQuery.parseJSON(xhr.responseText);
          } catch (_error) {
            error = _error;
            data = null;
          }
        }
        if (suc) {
          return _thisRequest.success(data, xhr.status);
        } else {
          errString = null;
          if (data != null) {
            if (data.errorCode != null) {
              errString = data.errorCode;
              if (data.message != null) {
                errString += ": " + data.message; } } }
          return _thisRequest.failure(errString, xhr.status);
        }
      }
    };
    if (json_text != null) {
      ajaxData.headers['content-length'] = json_text.length;
      ajaxData.data = json_text;
      ajaxData.processData = false;
    }
    return $.ajax(ajaxData);
  };
  return KiiAnalyticsRequest;
})();

