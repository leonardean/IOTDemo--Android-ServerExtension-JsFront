var groupMap = {daily: "dayno", weekly: "weekno", monthly: "monthno"};
var analyticsMap = {uv: 489, temperature: 488, humidity: 487};

var convertFunMap = {
  daily: function (num) {
    return new Date(num * 24 * 60 * 60 * 1000);
  },
  weekly: function (num) {
    return new Date(num * 24 * 60 * 60 * 1000 * 7);
  },
  monthly: function (num) {
    var year = Math.floor(num / 12);
    var month = num % 12;
    return new Date(year, month, 1, 0, 0, 0);
  }
}

WearableAnalytics = (function () {
  function WearableAnalytics(ruleType) {
    this.queryParam = new QueryParam(analyticsMap[ruleType]);
    this.start = -1;
    this.end = Date.now();
  }

  WearableAnalytics.prototype.setSummary = function (type) {
    this.queryParam.initGroup(groupMap[type]);
    this.groupType = type;
    return this;
  };

  WearableAnalytics.prototype.setUser = function (userID) {
    var filter = {"username": userID};
    this.queryParam.initFilter(filter);
    return this;
  };

  WearableAnalytics.prototype.setDeviceID = function (deviceID) {
    var filter = {"deviceID": deviceID};
    this.queryParam.initFilter(filter);
    return this;
  };

  WearableAnalytics.prototype.setPeriod = function (start, end) {
    if (start != null) {
      this.start = start.getTime();
    }
    if (end != null) {
      this.end = end.getTime() + 24 * 60 * 60 * 1000 - 1;
    }
    return this;
  };

  WearableAnalytics.prototype.setFormatPeriod = function (start, end) {
    var parseDate = function (str) {
      if (str == null || str == "") {
        return null;
      }
      var list = str.split("-");
      return new Date(list[0], list[1] - 1, list[2]);
    };
    if (start != "") {
      var startDate = parseDate(start);
    }
    if (end != "") {
      var endDate = parseDate(end);
    }
    return this.setPeriod(startDate, endDate);
  };

  WearableAnalytics.prototype.doAnalytics = function (lineCallback, failureCallback) {
    var groupType = this.groupType;
    var start = this.start;
    var end = this.end;
    var callback = {
      success: function (result) {
        var list = [];
        for (i in  result.snapshots) {
          var snap = result.snapshots[i];
          var date = convertFunMap[groupType](snap.name);
          var val = snap.data[snap.data.length - 1];
          var time = date.getTime();
          if (time >= start && time <= end) {
            list.push({name: date, value: val});
          }
        }
        lineCallback(list);
      },
      failure: function (error, statusCode) {
        console.log("error:" + error);
        console.log('statusCode:' + statusCode);
        if (failureCallback) {
          failureCallback(error, statusCode);
        }
      }
    }
    this.queryParam.doAnalytics(callback);
  };

  return WearableAnalytics;
})();




