function convertResult(result){
	var entry={};
	
	entry.deviceID=result.get("deviceID");
	entry.location=result.get("location");
	entry.uv=result.get("uv");
	entry.temperature=result.get("temperature");
	entry.humidity=result.get("humidity");
	entry.created=new Date(result.getCreated());
	
	return entry;
}

DeviceQuery=(function(){
	function DeviceQuery(user){
		this.bucket=user.bucketWithName("wdDataRecord");
	}
	
	DeviceQuery.prototype.setDeviceID=function(deviceID){
		this.deviceID=deviceID;
		return this;
	};
	
	DeviceQuery.prototype.doQuery=function(callback){
    //get query with clause
		var clause=KiiClause.equals("deviceID",this.deviceID);
		var query=KiiQuery.queryWithClause(clause);

    var bucket = this.bucket;
    var queryCallbacks = {
      success: function (queryPerformed, resultSet, nextQuery) {
        // do something with the results
        for (i in resultSet) {
          var result = resultSet[i];
          callback(convertResult(result));
        }
        // if there are more results to be retrieved
        if (nextQuery != null) {
          bucket.executeQuery(nextQuery, queryCallbacks);
        }
      },
      failure: function (queryPerformed, anErrorString) {
        console.log("error:" + anErrorString);
        //TODO
      }
    };
    this.bucket.executeQuery(query, queryCallbacks);
  }
	
	return DeviceQuery;
	
})();

DeviceGeoQuery=(function() {
	
	function DeviceGeoQuery(user){
		this.bucket=user.bucketWithName("wdDataRecord");
		
	}
	
	DeviceGeoQuery.prototype.setEastNorth=function(latitude,longitude){
		this.eastNorth=KiiGeoPoint.geoPoint(latitude,longitude);
		
		return this;
	};
	
	DeviceGeoQuery.prototype.setWestSouth=function(latitude,longitude){
		this.westSouth=KiiGeoPoint.geoPoint(latitude,longitude);
		return this;
	};
	
	DeviceGeoQuery.prototype.setCenterPoint=function(latitude,longitude){
		this.centerPoint=KiiGeoPoint.geoPoint(latitude,longitude);
		return this;
	};
	
	DeviceGeoQuery.prototype.setRadius=function(radius){
		this.radius=radius;
		return this;
	}

  DeviceGeoQuery.prototype.doQuery = function (callback) {
    var clause = null;
    if (this.radius) {
      clause = KiiClause.geoDistance("location", this.centerPoint, this.radius, "distance")
    } else {
      clause = KiiClause.geoBox("location", this.eastNorth, this.westSouth);
    }
    var query = KiiQuery.queryWithClause(clause);

    var bucket = this.bucket;
    var queryCallbacks = {
      success: function (queryPerformed, resultSet, nextQuery) {
        // do something with the results
        for (i in resultSet) {
          var result = resultSet[i];
          if (result.get("_calculated")) {
            var distance = result.get("_calculated").distance;
          }
          callback(convertResult(result), distance);
        }

        // if there are more results to be retrieved
        if (nextQuery != null) {
          bucket.executeQuery(nextQuery, queryCallbacks);
        }
      },

      failure: function (queryPerformed, anErrorString) {
        console.log("error:" + anErrorString);
        //TODO
      }
    };
    this.bucket.executeQuery(query, queryCallbacks);
  };
  return DeviceGeoQuery;
})();


