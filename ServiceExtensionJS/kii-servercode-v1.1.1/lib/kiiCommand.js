var fs = require('fs'),
    util = require('util'),
    nodeutil = require('nodeutil'),
    url = require('url'),
    cfg = require('./kiiConfig')
    http = require('./kiiHttp');


exports.deployFile = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  cfg.checkOption(cmd, 'file', 'file');
  http.login(ctx, function(context) {
    fs.readFile(cmd.file, 'utf8', function(err, data) {
      if(err) {
        util.error("Failed to open file. " + err);
        process.exit(1);
      }

      if(cmd.hookConfig) {
        fs.readFile(cmd.hookConfig, 'utf8', function(err, confData) {
          if(err) {
            util.error("Failed to open file. " + err);
            process.exit(1);
          }
          // deploy file, then deploy hooks, then set current. 
          // That way the current version will be set if none of the previous steps fail
          deployFile(context, data, function(ctx, versionID) {
            deployHookConfig(ctx, confData, versionID, function() {
              if (cmd.setCurrent) 
                setCurrent(ctx, versionID);
            });
          });
        });
      } else {
        if(cmd.setCurrent)
          deployFile(context, data, setCurrent);
        else
          deployFile(context, data);
      }
    });
  });
}

exports.setCurrent = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  cfg.checkOption(cmd, 'codeVersion', 'code-version');
  http.login(ctx, function(context) {
    setCurrent(context, cmd.codeVersion);
  });
}

exports.downloadFile = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  cfg.checkOption(cmd, 'codeVersion', 'code-version');
  http.login(ctx, function(context) {
    downloadFile(context, cmd.codeVersion, cmd.outputFile);
  });
}

exports.downloadHookConfigFile = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  cfg.checkOption(cmd, 'codeVersion', 'code-version');
  http.login(ctx, function(context) {
    downloadHookConfigFile(context, cmd.codeVersion, cmd.outputFile);
  });
}

exports.listFiles = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  http.login(ctx, function(context) {
    listFiles(context);
  });
}

exports.deleteFile = function(program, cmd) {
  var ctx = cfg.loadContext(program);
  cfg.checkOption(cmd, 'codeVersion', 'code-version');
  http.login(ctx, function(context) {
    deleteFile(context, cmd.codeVersion, deleteHookConfigFile);
  });
}

exports.configure = function(program, cmd) {
  var ctx = cfg.parseOptions(program);

  util.log("Configuring app credentials: ");
  util.log(JSON.stringify(ctx, null, 2));

  http.login(ctx, function(context) {
    configure(ctx);
  });
}

/** server code commands */

function configure(ctx) {
  cfg.storeConfig(ctx);
}

function deployFile(ctx, file, callback) {
  util.log('Deploying file...');
  var headers = { 'Content-Type' : 'application/javascript' };

  http.send(ctx, ctx.basePath, 'POST', headers, file, function(responseBody) {
    var versionID = JSON.parse(responseBody)['versionID'];
    util.log('File deployed as version ' + versionID);
    if(callback != null)
      callback(ctx, versionID);
  });  
}

function deployHookConfig(ctx, hookConfig, versionID, callback) {
  util.log('Deploying hook config...');
  var headers = { 'Content-Type' : 'application/vnd.kii.HooksDeploymentRequest+json' };

  http.send(ctx, ctx.hookVersionsPath + '/' + versionID, 'PUT', headers, hookConfig, 
    function(responseBody) {
      util.log('Hook Config deployed at version ' + versionID);
      if(callback != null)
        callback(ctx, versionID);
  });
}

function downloadFile(ctx, version, outputFile) {
  util.log('Downloading code version ' + version + '...');
  http.send(ctx, ctx.versionsPath + '/' + version, 'GET', {}, null, function(responseBody) {   
    if(outputFile) {
      fs.writeFile(outputFile, responseBody, function(err) {
        if(err) {
          util.log('Error accessing output file. ' + err);
          process.exit(1);
        } else {
          util.log("Code version written to " + outputFile);
        }
      }); 
    } else {
      util.log("Downloaded content: \n" + responseBody);
    }
  });
}

function downloadHookConfigFile(ctx, version, outputFile) {
  util.log('Downloading hook config version ' + version + '...');
  http.send(ctx, ctx.hookVersionsPath + '/' + version, 'GET', {}, null, function(responseBody) {   
    if(outputFile) {
      fs.writeFile(outputFile, responseBody, function(err) {
        if(err) {
          util.log('Error accessing output file. ' + err);
          process.exit(1);
        } else {
          util.log("Hook config version written to " + outputFile);
        }
      }); 
    } else {
      util.log("Downloaded content: \n" + responseBody);
    }
  });
}

function listFiles(ctx) {
  util.log('Listing available versions...');
  http.send(ctx, ctx.versionsPath, 'GET', {}, null, function(responseBody) {
    var res = JSON.parse(responseBody);
    util.log("Found " + res.versions.length + " versions: ");
    res.versions.forEach(function(v, i, array) {
      util.puts(v.versionID + ' ' + nodeutil.dateutil.getDateString(new Date(v.modifiedAt),"yyyy-mm-dd hh24:mi:ss") + ' ' + (v.current ? "active" : "inactive"));
    });
  });
}

function deleteFile(ctx, version, callback) {
  util.log('Removing code version ' + version + '...');

  http.send(ctx, ctx.versionsPath + '/' + version, 'DELETE', {}, null, function(responseBody) {
    util.log('Version ' + version + ' removed');
    if(callback != null)
      callback(ctx, version);
  });
}

function deleteHookConfigFile(ctx, version) {
  util.log('Trting to remove hook config version ' + version + '...');

  http.send(ctx, ctx.hookVersionsPath + '/' + version, 'DELETE', {}, null, 
    function(responseBody) {
      util.log('Hook config for version ' + version + ' removed');
    },
    function(status) {
      if (status == 404)
        util.log('No hook config for version ' + version + ' found.');
      else {
        util.error('Error while deleting hooks for version '  + version + ' - error code: ' + status);
      }
    });
}

function setCurrent(ctx, version) {     
  util.log('Setting current version to ' + version + '...');
  var headers = { 'Content-Type' : 'text/plain' }; 

  http.send(ctx, ctx.versionsPath + '/current', 'PUT', headers, version, function(responseBody) {
    util.log('Current version set to ' + version);    
  });
}
