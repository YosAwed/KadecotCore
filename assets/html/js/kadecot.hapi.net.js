/*
 * 
 * kadecot.hapiの中で、Kadecotサーバーとの通信をつかさどる部分。
 * Kadecotサーバーとの通信には、WebSocketを介したものと、AndroidのAddJavascriptInterfaceを通したものの
 * 二通りがあり、kHAPI.isOnAndroidにより区別される。
 */

kHAPI.net = {
  init: function() {
  }
  // Internal object to achieve invoke-oninvoke match
  ,
  callServerFunc_invokeMatch: {},
  id: 0
  // Home panel -> Server
  ,
  callServerFunc: function(method, argObject, callbackfunc) {
    // change to JSON String.
    if (argObject === undefined) argObject = {};
    // arg = JSON.stringify(argObject);

    if (!(method in this.ServerCall)) {
      console.log('Unsupported server api : ' + '.' + method);
      return;
    }

    var r = this.ServerCall[method].call(this.ServerCall, argObject,
            callbackfunc);
    if (r === undefined) return;

    var id = this.genId();

    this.callServerFunc_invokeMatch[id + '_'] = function(re, success) {
      if (typeof r.callback === 'function') r.callback(re, success);
      if (typeof callbackfunc === 'function') callbackfunc(re, success);
    };

    var arg = argObject;
    if (method === 'get' || method === 'set' || method === 'exec') {
      arg = {
        'param': argObject
      };
    }
    var st = JSON.stringify({
      'version': kHAPI.APIVer,
      'method': method,
      'params': arg,
      'id': id
    });
    console.log("request params: " + st);

    if (r.next === 1
            && (kHAPI.isOnAndroid || this.WS.serverConnection !== undefined)) {
      ServerCall.invoke(st);
    } else if (r.next === 2 && kHAPI.isOnAndroid) {
      ServerCall.invoke(st);
    } else {
      // if running on browser and no KadecotServer. or
      // call server management function on browser
      kHAPI.net.callFromServer(JSON.stringify({
        version: kHAPI.APIVer,
        id: id,
        result: r.result,
        method: r.method
      }));
    }

  }

  ,
  callOnWamp: function(procedure, options, params, paramsKw, callbackfunc) {
    var id = this.genId();
    this.callServerFunc_invokeMatch[id + '_'] = callbackfunc;

    console.log("WAMP CALL, procedure:" + procedure + ", options:"
            + JSON.stringify(options) + ", params:" + JSON.stringify(params)
            + ", paramsKw:" + JSON.stringify(paramsKw));

    if (kHAPI.isOnAndroid) {
      LocalInterface.call(procedure, JSON.stringify(options), JSON.stringify(paramsKw), "func", "func2");
    } else {
      this.WS.serverConnection.call(procedure, params, paramsKw, options).then(
              function(result) {
                console.log("CALL result: " + JSON.stringify(result));
                kHAPI.net.callServerFunc_invokeMatch[id + '_'](result, true);
              },
              function(error) {
                console.log("CALL error: " + "procedure=" + argObject.procedure
                        + ", error=" + JSON.stringify(error));
                kHAPI.net.callServerFunc_invokeMatch[id + '_'](error, false);
              });
    }
  }

  ,
  subscribeOnWamp: function(options, topic, callbackfunc) {
    var id = this.genId();
    this.callServerFunc_invokeMatch[id + '_'] = callbackfunc;

    console.log("WAMP SUBSCRIBE, procedure:" + procedure + ", options:"
            + JSON.stringify(options) + ", topic:" + topic);

    if (kHAPI.isOnAndroid) {
      LocalInterface.subscribe(topic, JSON.stringify(options), "func1", "func2", "func3");
    } else {
      var onEvent = function(args, kwargs, details) {
        console.log("onEvent: topic=" + topic + ", args" + args + ", kwargs"
                + JSON.stringify(kwargs));
        kHAPI.net.callServerFunc_invokeMatch[id + '_'](args, kwargs);
      }

      this.WS.serverConnection.subscribe(topic, onEvent, options).then(
              function(subscription) {
                console.log("subscribed: topic=" + subscription.topic);
              }, function(error) {
                console.log("subscribe error, error:" + error.error);
              });
    }


  }

  // Server -> Home panel
  ,
  callFromServer: function(msg) {
    var d = JSON.parse(msg);

    if (d.error !== undefined) {
      console.log("error code " + d.error.code + " : " + d.error.message);
      alert("error code " + d.error.code + " : " + d.error.message);
    }
    // -> version,method,params,id, 返答が必要なとき、method,idがある。
    // console.log('callFromServer : '+JSON.stringify(d));
    // onInvoke(methodなし)のときはresult(object)がある。
    if (d.version !== kHAPI.APIVer) {
      alert('API version mismatch in server reply (' + msg
              + '). Kadecot Server:' + d.version + ' , ServerPanelJS:'
              + kHAPI.APIVer);
    } else if (d.method === undefined) {
      this.ServerPredefinedReplies.onInvoke.call(this, d);
    } else if (typeof this.ServerPredefinedReplies[d.method] === 'function') {
      this.ServerPredefinedReplies[d.method].call(this, d);
    } else {
      if (typeof kHAPI[d.method] === 'function') {
        kHAPI[d.method](d.params);
      } else {
        console.log('Undefined method call from the server:' + d.method + ', '
                + d.params);
      }
    }
  }

  // ///////////////////////////////
  // Internal variables / utility funcs
  // ///////////////////////////////

  ,
  genId: function() {
    if (this.id >= Number.MAX_VALUE - 1) {
      this.id = 0;
    }

    return ++this.id;
  },

  info: {
    isConnected: false
  }
  // ///////////////////////////////
  // Websocket object
  // ///////////////////////////////
  ,
  WS: {
    serverConnection: undefined,
    connection: undefined,
    connectServer: function(ip) {
      _WS = this;
      if (this.serverConnection !== undefined) {
        this.connection.close('kadecot.hapi.net.js', 'Disconnect');
        this.serverConnection = undefined;
      }
      kHAPI.net.ServerPredefinedReplies.onDeviceListUpdatedNew();

      this.connection = new autobahn.Connection({
        url: 'ws://' + ip + ':41314/',
        realm: 'realm1'
      });

      this.connection.onopen = function(session) {
        _WS.serverConnection = session;
        console.log("on server connected");
        kHAPI.onServerConnected();

        _WS.serverConnection
                .subscribe(
                        'com.sonycsl.kadecot.topic.device',
                        function onEvent(args, kwargs, details) {
                          console.log("device found: kwargs:"
                                  + JSON.stringify(kwargs));
                          kHAPI.net.ServerPredefinedReplies
                                  .onDeviceFoundNew(kwargs);
                        }).then(function(subscription) {
                  console.log("subscribed " + subscription.topic);
                }, function(error) {
                  console.log("subscribe error, error:" + error.error);
                });

        _WS.serverConnection
                .call('com.sonycsl.kadecot.procedure.deviceList', [], {}, {})
                .then(
                        function(result) {
                          console
                                  .log("Result: com.sonycsl.kadecot.procedure.deviceList success:"
                                          + JSON.stringify(result));
                          if (result.args === undefined) {
                            kHAPI.net.ServerPredefinedReplies
                                    .onDeviceFoundNew(result);
                          } else {
                            kHAPI.net.ServerPredefinedReplies
                                    .onDeviceListUpdatedNew(result.args);
                          }
                        }, function(error) {
                          console.log("call fail:" + JSON.stringify(error));
                        });
      }

      this.connection.onclose = function(reason, details) {
        kHAPI.net.info = {
          isConnected: false
        };
        if (this.serverConnection !== undefined) {// Unintentional close
          // onSystemPaused
          kHAPI.net.ServerPredefinedReplies.onSystemPaused.call(kHAPI.net);
          _WS.serverConnection = undefined;
          // Try reconnection
          // setupws(new WebSocket('ws://'+ip+':41314/')) ;
        } else {
          // ip address is incorrect?
        }
      }

      this.connection.open();
    },
    disconnectServer: function() {
      this.connection.close('kadecot.hapi.net.js', 'Disconnect');
    }
  }
};

// /////////////////////////////////////
// /////////////////////////////////////
// API preprocesses
// /////////////////////////////////////
// /////////////////////////////////////

// /////////////////////////////////////
// Server -> Home panel
// /////////////////////////////////////
kHAPI.net.ServerPredefinedReplies = {
  // this is kHAPI.net
  onInvoke: function(d) {
    if (typeof this.callServerFunc_invokeMatch[d.id + '_'] === 'function') {
      if (d.error !== undefined)
        this.callServerFunc_invokeMatch[d.id + '_'](d.error, false);
      else
        this.callServerFunc_invokeMatch[d.id + '_'](d.result, true);
    }
    this.callServerFunc_invokeMatch[d.id + '_'] = undefined;
  },
  onServerStatusUpdated: function(d) {
    var settings = {
      network: d.params.networkInfo,
      location: d.params.location,
      persistence: d.params.serverMode.persistent,
      jsonp: d.params.serverMode.jsonpServer,
      websocket: d.params.serverMode.websocketServer,
      snap: d.params.serverMode.snapServer
    };
    kHAPI.net.info = settings;
    kHAPI.onServerStatusUpdated(settings);
  },
  onDeviceFoundNew: function(args) {
    if (kHAPI.dev.addDevice(args)) {// Truly new device
      kHAPI.devListHandlers.onDeviceFound(args, kHAPI.dev.devices);
    } else {
      kHAPI.devListHandlers.onDeviceActivated(args, kHAPI.dev.devices);
    }
  },
  onDeviceFound: function(args) {
    var newDevices = args.params.device;

    for (var devi = 0; devi < newDevices.length; devi++) {
      if (kHAPI.dev.addDevice(newDevices[devi])) {// Truly new device
        kHAPI.devListHandlers
                .onDeviceFound(newDevices[devi], kHAPI.dev.devices);
      } else {
        kHAPI.devListHandlers.onDeviceActivated(newDevices[devi],
                kHAPI.dev.devices);
      }
    }
  },
  onDeviceListUpdatedNew: function(args) {
    if (args === undefined) {
      kHAPI.dev.setDevicesList();
    } else {
      kHAPI.dev.setDevicesList(args);
    }
    kHAPI.devListHandlers.onUpdateList(kHAPI.dev.devices);
  },
  onDeviceListUpdated: function(args) {
    if (args === undefined) {
      kHAPI.dev.setDevicesList();
    } else {
      kHAPI.dev.setDevicesList(args.params.device);
    }
    kHAPI.devListHandlers.onUpdateList(kHAPI.dev.devices);
  },
  onDeviceDeleted: function(args) {
    kHAPI.dev.removeDevice(args.params.targetName);
    kHAPI.devListHandlers.onDeviceDeleted(args.params.targetName,
            kHAPI.dev.devices);
  },
  onNicknameChanged: function(args) {
    var oldnickname = args.params.oldName, newnickname = args.params.currentName;
    kHAPI.dev.changeNickname(oldnickname, newnickname);
    kHAPI.devListHandlers.onNicknameChanged(oldnickname, newnickname,
            kHAPI.dev.devices);
  },
  onPropertyChanged: function(d) {
    if (typeof kHAPI.app.running.onPropertyChanged === 'function') {
      kHAPI.app.running.onPropertyChanged.call(kHAPI.app, d.params);
    }
    kHAPI.onPropertyChanged(d.params);
  }
  // this is call by WS.close, or server wifi disconnection(WebView).
  ,
  onSystemPaused: function() {
    this.info.isConnected = false;
    kHAPI.dev.setDevicesList();
    kHAPI.net.ServerPredefinedReplies.onUpdateList([]);
    kHAPI.onServerDisconnected();
  }
};

// /////////////////////////////////////
// Home panel -> Server
// /////////////////////////////////////
/*
 * 
 * kadecot.hapiがサーバと通信するさいに仲介をするクラス。 ブラウザ内で処理すべきことは処理したりする。
 * 
 * ServerCall (ServerPanel <-> Server)の実装。net.callServerFunc()内からしか呼ばれない。
 * isOnAndroidによって動作を切り替える。
 * 
 * 返答の作り方 undefined -> notthing happens. {next:0} -> Do not call server
 * function(js send nothing to server.) ,but call onInvoke. {next:1} -> Call
 * server function. {next:2} -> Call server function only if "isOnAndroid".This
 * is for functions to management.
 * 
 * callback is called when server returns value. result is used when (running on
 * browser && no server) or (call server management function on browser)
 */

kHAPI.net.ServerCall = {
  setServerLocation: function(args, cbfunc) {
    // this = kHAPI.net.ServerCall
    return this.subCachedReplyGen_Set('_sys_location', '', args);
  },
  enableServerNetwork: function(args, cbfunc) {
    return {
      next: 2,
      result: null
    };
  },
  enableWebSocketServer: function(args, cbfunc) {
    return {
      next: 2,
      result: null
    };
  },
  enablePersistentMode: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  enableServerNetwork: function(args, cbfunc) {
    return {
      next: 2,
      result: null
    };
  },
  enableJSONPServer: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  enableSnapServer: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  fullInitialize: function() {
    return {
      next: 1
    };
  },
  refreshDeviceList: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  getDeviceList: function(args, cbfunc) {
    return {
      next: 1,
      result: []
    };
  }
  // args is [from,to]
  // if from or to is -1,it means start or end point of logging.
  ,
  queryLog: function(args, cbfunc) {
    return {
      next: 1,
      result: []
    };
  },
  set: function(args, cbfunc) {
    var d = kHAPI.dev.findDeviceByNickname(args[0]);
    if (d === undefined) {
      console.log(d + " not found");
      return;
    }
    if (d.isEmulation) {
      var result_value = {
        nickname: d,
        property: []
      };

      // ECHONET Lite Emulator.
      for (i = 1; i < args.length; i++) {
        if (args[i][0] in d.access) {
          result_value.property[i - 1] = {
            name: args[i][0],
            value: args[i][1],
            success: true
          };
          d.access[args[i][0].toLowerCase()] = args[i][1];
        } else {
          result_value.property[i - 1] = {
            name: args[i][0],
            value: args[i][1],
            success: false
          };
        }
      }
      return {
        next: 0,
        result: result_value
      };
    } else {
      return {
        next: 1
      };
    }
  },
  get: function(args, cbfunc) {
    var d = kHAPI.dev.findDeviceByNickname(args[0]);
    if (d === undefined) {
      console.log(d + " not found");
      return;
    }
    if (d.isEmulation) {

      var ret = {
        nickname: args[0],
        property: []
      };
      for (var ai = 1; ai < args.length; ++ai) {
        ret.property.push({
          name: args[ai][0],
          value: d.access[args[ai][0]],
          sucess: true
        });
      }
      return {
        next: 0,
        result: ret
      };
    } else
      return {
        next: 1
      };

  },
  exec: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  changeNickname: function(args, cbfunc) {
    var dev = null;
    var oldNickname = args.currentName;
    var newNickname = args.newName;

    var di;
    for (di = 0; di < kHAPI.dev.devices.length; ++di) {
      dev = kHAPI.dev.devices[di];
      if (dev.nickname !== oldNickname) continue;
      if (dev.isEmulation !== true) return {
        next: 1
      };
      dev.nickname = newNickname;
      break;
    }
    // no device found
    if (di === kHAPI.dev.devices.length) return;
    for (di = 0; di < kHAPI.dev.emulation_devices.length; ++di) {
      dev = kHAPI.dev.emulation_devices[di];
      if (dev.nickname !== oldNickname) continue;
      dev.nickname = newNickname;
      break;
    }
    return;
  }

  ,
  deleteDevice: function(args, cbfunc) {
    return {
      next: 1
    };
  },
  deleteInactiveDevices: function(args, cbfunc) {
    return {
      next: 1
    };
  }

  // Utility functions
  ,
  subCachedReplyGen_Set: function(cacheName, defaultValueStr, args) {
    window.localStorage.setItem(cacheName, JSON.stringify(args));
    return {
      next: 1,
      result: defaultValueStr
    };
  }
  // set時にlocalStorageのキャッシュをデフォルトの値とし、bDevNetAccessibleならそののちにサーバーに聞きに行く。
  // 返答が帰ってきた場合はその値をキャッシュに置く。
  ,
  subCachedReplyGen_Get: function(cacheName, defaultValue, cbfunc) {
    var ret = window.localStorage.getItem(cacheName);
    if (ret === undefined || ret === null) {
      ret = defaultValue;
    } else {
      ret = JSON.parse(ret);
    }
    return {
      next: 1,
      result: ret // デフォルト値としてキャッシュの値を返しておく。
      ,
      callback: function(re) {
        window.localStorage.setItem(cacheName, JSON.stringify(re));
        if (typeof cbfunc === 'function') cbfunc(re);
      }
    };
  }
};
