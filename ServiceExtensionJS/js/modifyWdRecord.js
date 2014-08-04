function modifyBucketACL(param, context, done) {
  //get user and bucket
  var userID = param.userID;
  var admin = context.getAppAdminContext();
  var log = admin.bucketWithName("log");
  var theUser = admin.userWithID(userID);
  var bucket = theUser.bucketWithName("wdDataRecord");

  //create a ACL that allows any authenticated user to query from the bucket
  var bucketACL = KiiACLEntry.entryWithSubject(new KiiAnyAuthenticatedUser(),
    KiiACLAction.KiiACLBucketActionQueryObjects);

  // Get the ACL handle and put the rule in the handle
  var acl = bucket.acl();
  acl.putACLEntry(bucketACL);
  acl.save({
    success: function (theACL) {
      recordLog(theACL, "set acl success", done, log);
    }, failure: function (theACL, errorString) {
      recordLog(errorString, "set acl fail", done, log);
    }
  });
}

function modifyRecord(param, context, done) {
  //get object
  var admin = context.getAppAdminContext();
  var log = admin.bucketWithName("log");
  var obj = KiiObject.objectWithURI(param.uri);

  KiiUser.authenticateWithToken(context.getAccessToken(), {
    success: function (theUser) {
      obj.refresh({
        success: function (theObj) {
          modifyObj(theObj);
          //set userSign in case it is empty
          var userSign = theUser.getUUID();
          if (theUser.getUsername() != null && theUser.getUsername() != "") {
            userSign = theUser.getUsername();
          } else if (theUser.getEmailAddress() != null &&
            theUser.getEmailAddress() != "") {
            userSign = theUser.getEmailAddress();
          }
          //save object
          theObj.set("username", userSign);
          theObj.save({
            success: function (theObj) {
              var acl = modifyACL(theObj);
              acl.save({
                success: function (theACL) {
                  var deviceID = theObj.get("deviceID");
                  maintainRel(theUser, deviceID, admin, done, log);
                }, failure: function (theACL, errorString) {
                  recordLog(errorString, "set acl fail", done, log); }});
            }, failure: function (theObj, errorString) {
              recordLog(errorString, "save fail", done, log); }});
        }, failure: function (theObject, anErrorString) {
          recordLog(anErrorString, "refresh fail", done, log); }});},
    failure: function (theUser, error) {
      recordLog(error, "auth fail", done, log);
    }
  });
}

function modifyObj(theObject, callback) {
  var timeVal = theObject.time;
  if (timeVal == undefined) {
    timeVal = theObject._created;
  }
  var time = new Date(timeVal);
  //calculate day, week, month
  var dayNum = Math.round(time / (1000 * 60 * 60 * 24));
  var weekNo = Math.round(dayNum / 7);
  var month = time.getMonth() + time.getFullYear() * 12;
  //set custom attributes
  theObject.set("weekno", weekNo);
  theObject.set("monthno", month);
  theObject.set("dayno", dayNum);
  return theObject;
}

function modifyACL(theObject) {
  //create ACL for object
  var objACL = KiiACLEntry.entryWithSubject(
    new KiiAnyAuthenticatedUser(), KiiACLAction.KiiACLObjectActionRead);
  // Get the ACL handle and put the rule in the handle
  var acl = theObject.objectACL();
  acl.putACLEntry(objACL);
  return acl;
}

function maintainRel(user, deviceID, admin, done, log) {
  //create query with clause
  var relation = admin.bucketWithName("UserDeviceRelation");
  var idEq = KiiClause.equals("userName", user.getUsername());
  var devEq = KiiClause.equals("deviceID", deviceID);
  var clause = KiiClause.and(idEq, devEq);
  var query = KiiQuery.queryWithClause(clause);

  relation.executeQuery(query, {
    success: function (queryPerformed, resultSet, nextQuery) {
      if (resultSet.length == 0) {
        var rel = relation.createObject();
        rel.set("deviceID", deviceID);
        rel.set("userID", user.getUUID());
        rel.set("userName", user.getUsername());
        rel.save({
          success: function (obj) {
            recordLog(obj, "save rel success", done, log);
          }, failure: function (obj, errorString) {
            recordLog(errorString, "save rel fail", done, log); }});
      } else {
        recordLog("every ok,nothing need to do", "finish", done, log); }
    }, failure: function (queryPerformed, anErrorString) {
      recordLog(anErrorString, "query rel fail", done, log);}});
}


function getDataByAdmin(param, context) {
  var obj = KiiObject.objectWithURI(param.uri);
  obj.refresh({
    success: function (theObj) {
      console.log(theObj);
    }, failure: function (theObj, error) {
      console.log(error); }
  });
}

function recordLog(log, type, done, bucket) {
  var obj = bucket.createObject();
  obj.set("log", log);
  obj.set("type", type);
  obj.save({
    success: function (theObj) {
      done(theObj);
    }, failure: function (theObj, err) {
      done(theObj, err); }
  });
}