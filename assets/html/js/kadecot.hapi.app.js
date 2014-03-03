/*
 * 外部アプリまわり。
 * 
 * 外部アプリのkadecot.initの中からreqMyPageConnectedが 呼ばれるのを待って、onMyPageConnectedを返す。
 * 
 * onMyPageConnectedは、その前提として、外部アプリからのonGetSetValue呼び出しに対応 できるようにしておく必要がある。
 * 
 * 
 * manifests (bookmarks)のキャッシュがkHAPI.app._manifestsにある(配列)
 * 
 * アプリを開くと、
 * 
 * running { manifest : the manifest of running app ,win : アプリのwindow ,origin :
 * origin ,cleanup : function
 * //終了。runningのクリーニングやら、全てのポーリングの停止やらする。引数がtrueの場合はウィンドウを閉じる。 }
 * 
 * が作られる。
 * 
 * running.manifestには、さらに以下の情報が追加される。
 * 
 * varNameMap : アクセスオブジェクト中のデバイス名とアクセス変数の名前をキーとして、Kadecot側の 名前のペアを配列で
 * varNameMap[accessDevName][accessVarName] = [device nickname ,device property
 * name , option string if exist]
 * 
 * running.manifest.devices内の各要素には、assignedDevNameが追加され、実際に割り当てられた
 * デバイスのニックネームが格納される。
 * 
 */

var myPageURL = (location.href.indexOf('?') != -1 ? location.href.substring(0,
        location.href.indexOf('?')) : location.href);
myPageURL = myPageURL.substring(0, myPageURL.lastIndexOf('/') + 1);

// (unsigned) byte array to int
function echoByteArrayToInt(b) {
  var ret = 0;
  for (var bi = 0; bi < b.length; ++bi)
    ret = (ret << 8) | (b[bi] & 0xff);
  return ret;
}

function intToEchoByteArray(bint, siz) {
  var ret = [];
  for (var bi = 0; bi < siz; ++bi) {
    ret.unshift(bint & 0xff);
    bint >>= 8;
  }
  return ret;
}

function getTempFromECHONETLite2ByteArray(b) {
  var tempInt = echoByteArrayToInt(b);
  return parseFloat(((tempInt > 0x7fff) ? (tempInt - 0xf554 - 2732) * 0.1
          : tempInt * 0.1).toFixed(1));
}

function TwoByteArrayFromECHONETLiteTemp(ti) {
  ti = Math.round(ti * 10);
  if (ti < 0) ti = 0x10000 + ti;
  return intToEchoByteArray(ti, 2);
}

var manifest_tag = "local_storage_manifest";
function getSavedManifest() {
  var ret = window.localStorage.getItem(manifest_tag);
  if (ret == null) return [];
  return JSON.parse(ret);
};
function setSavedManifest(lis) {
  window.localStorage.setItem(manifest_tag, JSON.stringify(lis));
};

kHAPI.app = {
  'init': function() {
    // var bnum = 0 ;
    this
            .readManifests(
            /*
             * function(){bnum = kHAPI.app._manifests.length;} ,
             */
            function(manifests) {
              kHAPI.app.onMsgFromApp = function(origin, json_rpc) {
                // console.log( 'message by addEventListener_message : ' +
                // JSON.stringify(arguments) ) ;
                // console.log( 'json_rpc : ' + JSON.stringify(json_rpc) ) ;
                // クライアントアプリからの全てのメソッド呼び出しのルート
                if (json_rpc.method === 'getsetValue') {
                  var vnm = kHAPI.app.running.manifest.varNameMap[json_rpc.params[0]][json_rpc.params[1]];
                  var option = vnm[2];
                  if (json_rpc.params.length == 2) { // get
                    kHAPI.get([vnm[0], vnm[1]], function(result, success) {
                      if (!success) return;
                      result = result.property[0].value;
                      if (option !== undefined) {
                        if (option === 'echotemperature')
                          result = [getTempFromECHONETLite2ByteArray(result)];
                        else if (option === 'echomultibyteint')
                          result = [echoByteArrayToInt(result)];
                      } else
                        result = [result];

                      kHAPI.app.postMsgToApp('onGetSetValue', result,
                              json_rpc.id);
                    });
                  } else { // set
                    var newval = json_rpc.params[2];

                    if (option !== undefined) {
                      if (option === 'echotemperature')
                        newval = TwoByteArrayFromECHONETLiteTemp(newval);
                      else if (option === 'echomultibyteint')
                        newval = intToEchoByteArray(newval, 4); // to 4 bytes
                    }

                    kHAPI.set([vnm[0], [vnm[1], newval]], function(result,
                            success) {
                      if (!success) return;
                      result = result.property[0].value;
                      kHAPI.app.postMsgToApp('onGetSetValue', [result],
                              json_rpc.id);
                    });
                  }
                } else if (json_rpc.method === 'queryLog') {
                  kHAPI.queryLog(json_rpc.params, function(dat) {
                    kHAPI.app.postMsgToApp('onQueryLog', [dat], json_rpc.id);
                  });
                } else {
                  var method = kHAPI.app.appAPI[json_rpc.method];

                  if (method === undefined)
                    console.log('No method named ' + json_rpc.method);
                  else
                    method.apply(kHAPI.app, [origin].concat(json_rpc.params));

                }
              };

              if (!kHAPI.isOnAndroid) addEventListener('message', function(e) {
                kHAPI.app.onMsgFromApp(e.origin, JSON.parse(e.data));
              }, false);

              kHAPI.app.running = {};

              if (!kHAPI.isOnAndroid && window.opener !== undefined
                      && window.opener !== null) {
                // アプリを単独に走らせてしまい、MyPageが存在していなかったので開いた場合。
                // isOnAndroidでは起こらない。

                kHAPI.app.running = {
                  win: window.opener
                };
                window.opener.postMessage(JSON.stringify({
                  'method': 'onMyPageReady',
                  'params': null,
                  'id': -1
                }), '*');
              }
            });
  }
  // if src is with :// , it is absolute, refPath is not used.
  // otherwise, src is relative. add newPath's path as the prefix.
  ,
  getAbsPath: function(src, refPath) {
    if (src.indexOf('://') !== -1) return src;
    return refPath.substring(0, refPath.lastIndexOf('/') + 1) + src;
  },
  postMsgToApp: function(methodName, args, key) {
    var msgToPost = JSON.stringify({
      'method': methodName,
      'params': (args instanceof Array ? args : null),
      'id': key
    });
    if (kHAPI.isOnAndroid) {
      UserApp.postMessage(msgToPost);
    } else {
      if (this.running.win === undefined || this.running.win === null) return;
      this.running.win.postMessage(msgToPost, this.running.origin);
    }
  },
  readManifests: function(callback) {
    function onReadManifs(manifs_dat) {
      var mfs = manifs_dat;
      kHAPI.app._manifests = mfs;
      callback(mfs);
    }
    ;
    if (kHAPI.app._manifests === undefined) {
      kHAPI.app._manifests = {
        data: [
            {
              "url": "http://snap.berkeley.edu/snapsource/snap.html"
                      + "#open:http://%KADECOT_IP_ADDR%:31414/block.xml",
              "title": "Snap!",
              "subtitle": "Snap! Extension for Kadecot",
              "icon": "",
              "devices": []
            },
            {
              "url": "file:///android_asset/html/Apps/Test/index.html",
              "title": "テストアプリ",
              "subtitle": "テスト用アプリです。",
              "description": "テスト用アプリの説明です。",
              "icon": "data:image/png;base64,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",
              "devices": [{
                "name": "Aircon",
                "protocol": "ECHONET Lite",
                "deviceType": "0x0130",
                "description": "エアコン",
                "access": {
                  "0x80": {
                    "name": "Power",
                    "polling": 15
                  },
                  "0xb0": {
                    "name": "Mode",
                    "polling": 16
                  },
                  "0xb3": {
                    "name": "Temp",
                    "polling": 17
                  }
                }
              }, {
                "name": "Light",
                "protocol": "ECHONET Lite",
                "deviceType": "0x0290",
                "description": "室内の照明",
                "access": {
                  "0x80": {
                    "name": "Power",
                    "polling": 10
                  }
                }
              }, {
                "name": "ExtTemp",
                "protocol": "ECHONET Lite",
                "deviceType": "0x0011",
                "description": "外気温のセンサー。",
                "access": {
                  "0xe0": {
                    "name": "val",
                    "polling": 600,
                    "option": "echoTemperature"
                  }
                }
              }]
            }]
      }.data;
      var saved_manifest = getSavedManifest();
      for (var si = 0; si < saved_manifest.length; ++si) {
        kHAPI.app._manifests.push(saved_manifest[si]);
      }
      kHAPI.app.addManifest = function(manifs_dat) {
        // Update manifest!!
        var mfs = manifs_dat;
        for (var i = 0; i < kHAPI.app._manifests.length; i++) {
          if (kHAPI.app._manifests[i].url === mfs.url) {
            kHAPI.app._manifests[i] = mfs;
            return;
          }
        }
        kHAPI.app._manifests.push(mfs);
        var s = getSavedManifest();
        s.push(manifs_dat);
        setSavedManifest(s);
      };
    }
    onReadManifs(kHAPI.app._manifests);
  },
  addManifest: function() {
  }

  ,
  openAppPageByManifestIndex: function(index) {
    var clone = function(obj) {
      if (null == obj || "object" != typeof obj) return obj;
      var copy = obj.constructor();
      for ( var attr in obj) {
        if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
      }
      return copy;
    }
    if (this.running.cleanup !== undefined) this.running.cleanup(true);
    var manifest = clone(this._manifests[index]);
    this.running.manifest = manifest;
    // may depends on Snap server.if the server is disabled, alert.
    if (manifest.url.indexOf("%KADECOT_IP_ADDR%") != -1) {
      if (!kHAPI.app.isSnapEnabled()) {
        alert("Snap server is disabled! Please turn it on in the settings panel!");
      }
      manifest.url = manifest.url.replace("%KADECOT_IP_ADDR%", kHAPI.app
              .getKadecotIPAddr());
    }
    if (kHAPI.isOnAndroid) {
      UserApp.openAppView(manifest.url);
    } else
      this.running.win = window.open(manifest.url, 'kadecot_app');
    // Wait until 'reqMyPageConnected' is called
  }

  // manifestを必要に応じて認証してアプリを開く関数
  ,
  postOnMyPageConnected: function(manif, win, origin) {
    // manifest認証後に実際に開く。(ここでしか使わない)
    function postMain() {
      this.running.manifest = manif;
      if (win !== undefined) this.running.win = win;
      if (origin !== undefined) this.running.origin = origin;

      var ao = {};
      var ds = (manif.devices instanceof Array ? manif.devices : []);

      manif.varNameMap = {};

      var getcalls = [];
      var stopPollFuncs = [];

      for (var di = 0; di < ds.length; ++di) {
        (function() {
          var d = ds[di];
          // real device
          var rd = kHAPI.dev.findDeviceByNickname(d.assignedDevName);
          if (rd === undefined) {
            rd = kHAPI.dev.findAssignableDevices(d.protocol, d.deviceType);
            if (rd.length == 0)
              rd = undefined;
            else {
              rd = rd[0];
              d.assignedDevName = rd.nickname;
            }
          }

          manif.varNameMap[d.name] = {};

          // Set current values

          ao[d.name] = {
            'active': rd.active
          };
          for ( var prop in d.access) {
            (function() {
              if (typeof prop !== 'string') return;

              var _prop = prop;
              var d_access_prop = d.access[_prop];
              var option = d_access_prop.option;
              if (option !== undefined) option = option.toLowerCase();
              manif.varNameMap[d.name][d_access_prop.name] = (option === undefined
                      ? [rd.nickname, _prop] : [rd.nickname, _prop, option]);
              getcalls.push(function() {

                kHAPI.app.postMsgToApp('showMessage', ['Accessing '
                        + rd.nickname + ' for ' + _prop]);

                kHAPI.get([rd.nickname, _prop], function(result, success) {
                  if (!success) {
                    kHAPI.app.postMsgToApp('showMessage', ['Failed : '
                            + rd.nickname + '.' + _prop]);
                    ao[d.name][d_access_prop.name] = null;
                    if (--get_total == 0) {
                      kHAPI.app.postMsgToApp('showMessage', []);
                      kHAPI.app.postMsgToApp('onMyPageConnected', [ao]);
                    }
                    return;
                  }
                  kHAPI.app.postMsgToApp('showMessage', [rd.nickname + '.'
                          + _prop + ' successfully obtained']);

                  result = result.property[0].value;

                  var varname = d_access_prop.name;
                  if (typeof varname !== 'string') {
                    --get_total;
                    return;
                  }

                  if (option !== undefined) {
                    if (option === 'echotemperature')
                      result = getTempFromECHONETLite2ByteArray(result);
                    else if (option === 'echomultibyteint')
                      result = echoByteArrayToInt(result);
                  }

                  ao[d.name][varname] = (result === undefined ? null : result);

                  if (--get_total == 0) {
                    kHAPI.app.postMsgToApp('showMessage', []);
                    kHAPI.app.postMsgToApp('onMyPageConnected', [ao]);
                  }
                });
              });

              // ポーリング設定
              if (typeof d_access_prop.polling === 'number') {
                var poll_stopper = false;
                stopPollFuncs.push(function() {
                  poll_stopper = true;
                });

                var varName = d_access_prop.name;
                var poll_time = d_access_prop.polling * 1000;
                var poll_func = function() {
                  if (poll_stopper) return;

                  kHAPI.get([rd.nickname, _prop], function(result, success) {
                    if (success) {
                      result = result.property[0].value;
                      if (poll_stopper) return;

                      if (option !== undefined) {
                        if (option === 'echotemperature')
                          result = [getTempFromECHONETLite2ByteArray(result)];
                        else if (option === 'echomultibyteint')
                          result = [echoByteArrayToInt(result)];
                      } // else result = [result] ;

                      kHAPI.app.postMsgToApp('onPropertyChanged', [d.name,
                          varName, result]);
                    }
                    setTimeout(poll_func, poll_time);
                  });
                };

                setTimeout(poll_func, poll_time);
              }
            })();

          }
        })();
      }

      this.running.cleanup = function(bWinclose) {
        // ポーリングを全停止するメソッドを追加
        for (var pi = 0; pi < stopPollFuncs.length; ++pi)
          stopPollFuncs[pi]();
        if (bWinclose === true) {
          if (kHAPI.isOnAndroid)
            UserApp.closeAppView();
          else if (kHAPI.app.running.win !== undefined)
            kHAPI.app.running.win.close();
        }
        // kHAPI.app.running.manifest = undefined ;
        // kHAPI.app.running.cleanup = undefined ;

        // if( bWinclose === true )
        kHAPI.app.running = {};
      };

      this.running.onPropertyChanged = function(info) {
        var nickname = info.nickname;
        for (var di = 0; di < ds.length; ++di) {
          var d = ds[di];
          if (d.assignedDevName !== nickname) continue;
          if (d.access[propname] === undefined) return; // No need to tell.
          for (var pi = 0; pi < info.property.length; ++pi) {
            var prop = info.property[pi];
            var propname = prop.name;
            var newval = prop.value;

            kHAPI.app.postMsgToApp('onPropertyChanged', [d.name,
                d.access[propname].name, newval]);
          }
          break;
        }
      };

      // getコールをまとめないと、get_totalが何度も０になってしまう。
      var get_total = getcalls.length;
      if (get_total === 0)
        kHAPI.app.postMsgToApp('onMyPageConnected', [ao]);
      else
        for (var gi = 0; gi < getcalls.length; ++gi)
          getcalls[gi]();
    }
    ;

    postMain.call(this);
  }

  ,
  appAPI: { // this = kHAPI.app
    'reqMyPageConnected': function(origin, manif) { // appがinitを呼んだら呼び出される。
      this.postOnMyPageConnected(JSON.parse(manif), undefined, origin);
    },
    'cleanup': function(origin, winclose) {
      if (typeof this.running.cleanup === 'function')
        this.running.cleanup(winclose);
    }
  },
  getKadecotIPAddr: function() {
    var netinfo = kHAPI.getNetInfo();
    if (netinfo.network === undefined) return "";
    var ip = netinfo.network.ipv4;
    if (ip === undefined || ip === null) return "";
    return ip;
  },
  isSnapEnabled: function() {
    var netinfo = kHAPI.getNetInfo();
    if (netinfo.snap) return true;
    return false;
  }
};
