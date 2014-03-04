// Remember!escape HTML.
// http://tmlife.net/programming/javascript/javascript-string-format.html
if (String.prototype.format === undefined) {
  String.prototype.format = function(args) {
    var replace_function;
    if (typeof args === "object") {
      replace_function = function(m, k) {
        return args[k];
      };
    } else {
      replace_function = function(m, k) {
        return arguments[parseInt(k)];
      };
    }
    return this.replace(/\{(\w+)\}/g, replace_function);
  };
}
if (String.prototype.removeAllNewline === undefined) {
  String.prototype.removeAllNewline = function() {
    return this.replace(/\r?\n|\r/g, ' ');
  };
}
if (Date.prototype.myLocaleString === undefined) {
  Date.prototype.myLocaleString = function() {
    var ret = "";
    ret += this.getMonth() + 1 + "/";
    ret += this.getDate();
    ret += " ";
    ret += this.getHours() + ":";
    ret += this.getMinutes() + ":";
    ret += this.getSeconds();
    return ret;
  };
}

// do not apply css to settings button.
$.mobile.page.prototype.options.keepNative = '.jqmNone';
$.mobile.page.prototype.options.domCache = true;

// if we refresh list before it's created.
var devlist_page_has_created = false;
var app_page_has_created = false;
var pagecreated = false;

$(document)
        .on(
                "pagecreate",
                function() {
                  $.mobile.defaultPageTransition = "none";
                  $.mobile.defaultDialogTransition = "none";
                  $.mobile.useFastClick = true;
                  if (pagecreated) return;
                  pagecreated = true;

                  kHAPI
                          .init(function() {
                            kHAPI.onServerConnected = function() {
                              toast("connected");
                              $("#server_ip").parent().css("background-color",
                                      "lightgreen");
                              $("#connect_button").val("Disconnect").button(
                                      "refresh");
                            };
                            kHAPI.onServerDisconnected = function() {
                              toast("disconnected");
                              $("#server_ip").parent().css("background-color",
                                      "transparent");
                              $("#connect_button").val("Connect").button(
                                      "refresh");
                            };
                            kHAPI.onServerConnectionFailed = function() {
                              toast("Failed to connect");
                            };
                            kHAPI.devListHandlers.onUpdateList = function(devs) {
                              if (!devlist_page_has_created
                                      || !(devs instanceof Array))
                                return false;
                              var device_list_view = $("#device_list_view");
                              device_list_view.empty();
                              kHAPI.dev.sortDevices();

                              for (var i = 0; i < devs.length; i++) {
                                var dev = devs[i];
                                if (isControler(dev)) continue;
                                device_list_view.append(makeDeviceLi(dev));
                                refreshDevStatusForIcon(dev.nickname);
                              }
                              device_list_view.listview().listview("refresh");
                              return true;
                            };

                            kHAPI.devListHandlers.onDeviceFound = function(
                                    newdevice, newlist) {
                              if (!devlist_page_has_created
                                      || typeof newdevice !== 'object') { return kHAPI.devListHandlers
                                      .onUpdateList(newlist); }
                              if (isControler(newdevice)) return false;
                              addDeviceForGoodPlace(newdevice);
                              var device_list_view = $("#device_list_view");
                              device_list_view.listview().listview("refresh");
                              refreshDevStatusForIcon(newdevice.nickname);
                              return true;
                            };

                            kHAPI.devListHandlers.onDeviceActivated = function(
                                    newdevice, newlist) {
                              if (!devlist_page_has_created
                                      || typeof newdevice !== 'object') { return kHAPI.devListHandlers
                                      .onUpdateList(newlist); }
                              if (isControler(newdevice)) return false;
                              // addDeviceForGoodPlace(newdevice);
                              // var device_list_view = $("#device_list_view");
                              // device_list_view.listview().listview("refresh");
                              refreshDevStatusForIcon(newdevice.nickname);
                              return true;
                            };

                            kHAPI.devListHandlers.onDeviceDeleted = function(
                                    newdevice, newlist) {
                              return kHAPI.devListHandlers
                                      .onUpdateList(newlist);
                            };

                            kHAPI.onPropertyChanged = function(prop_change) {
                              // console.log(prop_change);
                              update_one_epc(prop_change.nickname,
                                      prop_change.property[0].name,
                                      prop_change.property.value);
                            };

                            kHAPI.onServerStatusUpdated = function(
                                    network_info) {
                              // toast("Network settings was changed.");
                              if (kHAPI.isOnAndroid) {
                                onServerStatusUpdatedOnAndroid(network_info);
                              } else {
                                onServerStatusUpdatedOnBrowser(network_info);
                              }
                            };

                            var oldOnBackBtn = kHAPI.onBackBtn;
                            kHAPI.onBackBtn = function(view) {
                              if (typeof oldOnBackBtn === 'function')
                                oldOnBackBtn();

                              if (view === 'appView') { return; }

                              if (kHAPI.isOnAndroid) {
                                // when device list page is active, exit
                                // application.
                                if ($.mobile.activePage.is($("#devlist_page"))) {
                                  ExitApp.exitActivity();
                                  return;
                                }
                              }

                              if ($.mobile.activePage.is($("#app_page"))
                                      || $.mobile.activePage.is($("#log_page"))) {
                                $.mobile.changePage("#devlist_page");
                                return;
                              }
                              $.mobile.back();
                            };

                            kHAPI.reqDevListHandlers_onUpdateList();
                            kHAPI.readManifests(refreshManifests);
                          });
                });

$("#devlist_page").on("pageinit", function() {
  devlist_page_has_created = true;
  kHAPI.reqDevListHandlers_onUpdateList();
});
$("#app_page").on("pageinit", function() {
  app_page_has_created = true;
});
$("#app_page").on("pageshow", function() {
  kHAPI.readManifests(refreshManifests);
});

// Log initialize
$("#log_page").on(
        "pageshow",
        function() {
          $("#log_area").height(
                  $(window).height()
                          - $("#log_page .ui-header").outerHeight(true)
                          - $("#refresh_log_button").outerHeight(true) - 22);
          getSimpleLog();
        });

$(window).resize(
        function() {
          $("#log_area").height(
                  $(window).height()
                          - $("#log_page .ui-header").outerHeight(true)
                          - $("#refresh_log_button").outerHeight(true) - 22);
        });

var addDeviceForGoodPlace = function(newdev) {
  var device_list_view = $("#device_list_view");
  var devs = kHAPI.getDevices().slice(0);

  kHAPI.dev.sortDevices();
  devs = devs.filter(function(d) {
    return !isControler(d);
  });

  for (var i = 0; i < devs.length; i++) {
    if (devs[i].nickname === newdev.nickname) {
      if (i != 0) {
        $("#" + getNicknameHash(devs[i - 1].nickname)).after(
                makeDeviceLi(newdev));
      } else {
        device_list_view.prepend(makeDeviceLi(newdev));
      }
      return;
    }
  }
  return;
};

var onClickRefreshLogButton = function() {
  getSimpleLog();
  return false;
};

var getSimpleLog = function() {
  // 9/26 21:15:14
  var one_hours_ago = getUNIXTime(getHour(new Date(), -1));
  if (kHAPI.queryLog !== undefined) {
    kHAPI
            .queryLog(
                    [one_hours_ago, -1, {}],
                    function(raw_data) {
                      var str = "";
                      for (var i = raw_data.length - 1; i >= 0; i--) {
                        var message = "{date} {nickname} {type} {epc} {edt} ({success}{message})"
                                .format({
                                  date: new Date(parseInt(raw_data[i].unixtime))
                                          .myLocaleString(),
                                  nickname: raw_data[i].nickname,
                                  type: raw_data[i].access_type,
                                  epc: raw_data[i].property_name,
                                  edt: raw_data[i].property_value,
                                  success: raw_data[i].success === "true"
                                          ? "success" : "fail",
                                  message: (raw_data[i].message !== 'null'
                                          ? (': ' + raw_data[i].message) : '')
                                });
                        str += message + "\n";
                      }
                      $("#log_area").val(str);
                    });
  }
};

// events. ---------------------------------------------------------------------
var onClickGetGeoLocation = function() {
  "use strict";
  areYouSure(
          "Allow using geolocation?",
          "Geolocation will be used to collect air temperature,humidity or weather etc. ",
          "OK", "Cancel", function() {
            navigator.geolocation.getCurrentPosition(function(pos) {
              kHAPI.setServerLocation([pos.coords.latitude,
                  pos.coords.longitude]);
              toast('Success current location(' + pos.coords.latitude + ' , '
                      + pos.coords.longitude + ' )');
            }, function(error) {
              toast('Failed to collect location' + error.code + ':'
                      + error.message);
            }, {
              enableHighAccuracy: true,
              timeout: 30 * 1000
            });
          });
  return false;
};

var onClickConnectButton = function(button) {
  "use strict";
  if (kHAPI.isConnected()) {
    kHAPI.disconnectServer();
  } else {
    kHAPI.connectServer($("#server_ip").val());
  }
};

// also used by first confirm.
var onClickRegisterButton = function(isFirstConfirm) {
  if (isFirstConfirm === undefined) isFirstConfirm = false;
  var netInfo = kHAPI.getNetInfo();
  var isRegistered = netInfo.network.isDeviceAccessible;
  var SSID = netInfo.network.SSID;
  SSID = (SSID == undefined || SSID == null || SSID.length == 0) ? 'network'
          : SSID.split('"').join('');

  if (!isRegistered) {
    areYouSure("Join " + SSID + "?",
            "This allows access to home appliances and sensors on " + SSID
                    + ".", "OK", "Cancel", function() {
              kHAPI.enableServerNetwork({enable:true});
              if (!isFirstConfirm) {
                $("#register_android_button").val("Withdraw from " + SSID)
                        .button("refresh");
              }
              toast("Joined the network");
            });

  } else {
    kHAPI.enableServerNetwork({enable:false});
    if (!isFirstConfirm) {
      $("#register_android_button").val("Join current network").button(
              "refresh");
    }
    toast("withdrew from the network");
  }
};

var onClickSettingsPageButton = function() {
  if (kHAPI.isOnAndroid) {
    $.mobile.changePage("#settings_page_android");
    onSettingsPageAndroidOpen();
  } else {
    $.mobile.changePage("#settings_page_browser");
    onSettingsPageBrowserOpen();
  }
};

var onSettingsPageAndroidOpen = function() {
  // network : {"isConnected" : true , "type" : "wifi", "SSID" : "aaa",
  // "ip" : "1.1.1.1", "isDeviceAccessible" : true} // network
  // location : []
  // jsonp : true
  // websocket: true
  // persistence: true // persistence

  var netInfo = kHAPI.getNetInfo();

  $("#server_android_ip").val(netInfo.network.ipv4).textinput();
  if (netInfo.network.isDeviceAccessible) {
    $("#register_android_button").val("Withdrow from current network").button(
            "refresh");
  } else {
    $("#register_android_button").val("Join current network").button("refresh");
  }
  initializeCheckBox($("#enable_websocket_checkbox"), netInfo.websocket,
          function() {
            kHAPI.enableWebSocketServer({enable:true});
          }, function() {
            kHAPI.enableWebSocketServer({enable:false});
          });

  initializeCheckBox($("#enable_persistent_android_checkbox"),
          netInfo.persistence, function() {
            kHAPI.enablePersistentMode({enable:true});
          }, function() {
            kHAPI.enablePersistentMode({enable:false});
          });

  initializeCheckBox($("#enable_jsonp_android_checkbox"), netInfo.jsonp,
          function() {
            kHAPI.enableJSONPServer({enable:true});
          }, function() {
            kHAPI.enableJSONPServer({enable:false});
          });

  initializeCheckBox($("#enable_snap_android_checkbox"), netInfo.snap,
          function() {
            kHAPI.enableSnapServer({enable:true});
          }, function() {
            kHAPI.enableSnapServer({enable:false});
          });
};

var onSettingsPageBrowserOpen = function() {
  "use strict";
  var netInfo = kHAPI.getNetInfo();
  initializeCheckBox($("#enable_persistent_browser_checkbox"),
          netInfo.persistence, function() {
            kHAPI.enablePersistentMode({enable:true});
          }, function() {
            kHAPI.enablePersistentMode({enable:false});
          });

  initializeCheckBox($("#enable_snap_browser_checkbox"), netInfo.snap,
          function() {
            kHAPI.enableSnapServer({enable:true});
          }, function() {
            kHAPI.enableSnapServer({enable:false});
          });

  initializeCheckBox($("#enable_jsonp_browser_checkbox"), netInfo.jsonp,
          function() {
            kHAPI.enableJSONPServer({enable:true});
          }, function() {
            kHAPI.enableJSONPServer({enable:false});
          });
};

var initialNotifyServerSettings = true;
var onServerStatusUpdatedOnAndroid = function(settings) {
  // console.log(JSON.stringify(settings));

  $("#server_android_ip").val(settings.network.ip).textinput();
  checkCheckBox($("#enable_websocket_checkbox"), settings.websocket);
  checkCheckBox($("#enable_persistent_android_checkbox"), settings.persistence);
  checkCheckBox($("#enable_snap_android_checkbox"), settings.snap);
  checkCheckBox($("#enable_jsonp_android_checkbox"), settings.jsonp);

  if (initialNotifyServerSettings && settings.network.type === "WIFI"
          && !settings.network.isDeviceAccessible) {
    setTimeout(function() {
      return onClickRegisterButton(true);
    }, 500);
  }
  initialNotifyServerSettings = false;
};

var onServerStatusUpdatedOnBrowser = function(settings) {
  checkCheckBox($("#enable_persistent_browser_checkbox"), settings.persistence);
  checkCheckBox($("#enable_snap_browser_checkbox"), settings.snap);
  checkCheckBox($("#enable_jsonp_browser_checkbox"), settings.jsonp);
};

// checkbox is $(hoge),initial_value is weather true or false.
var initializeCheckBox = function(checkbox, initial_value, on_function,
        off_function) {
  "use strict";
  if (initial_value === undefined) {
    initial_value = false;
  }
  checkbox.unbind("click");
  checkCheckBox(checkbox, initial_value);
  checkbox.click(function() {
    if (!$(this).is(":checked")) {
      off_function();
    } else {
      on_function();
    }
    ;
  });
};

var checkCheckBox = function(checkbox, checked) {
  checkbox.checkboxradio().prop("checked", checked).checkboxradio("refresh");
};

// read current device info.
var onDeviceDetailPageOpen = function(nickname, device) {
  $.mobile.changePage("#device_detail_page");
  if (device === undefined) {
    device = kHAPI.findDeviceByNickname(nickname);
  }

  var description = ("<big>{nickname}</big><br>" + "Type {deviceName} <br>"
          + "Protocol {protocol} <br>" + "{active}</div>").format({
    nickname: escapeHTML(device.nickname)
            + (device.isEmulation === true ? ' (Emulation)' : ''),
    deviceName: escapeHTML(device.deviceName),
    protocol: escapeHTML(device.protocol),
    active: (device.active ? 'Active' : 'Inactive')
  });

  $("#device_description").html(description);
  $("#device_remocon").html(makeDeviceRemocon(nickname));
  $("#device_remocon").trigger("create");
  $("#nickname_input").val(escapeHTML(nickname));
  $("#nickname_button").unbind("click");
  $("#nickname_button").click(function() {
    var new_nickname = $("#nickname_input").val();
    onClickChangeNickname(nickname, new_nickname);
  });

  // Power logger
  var powerLogerOptionEnabled = false;
  if (device.protocol !== 'ECHONET Lite' || (device.deviceType !== '0x0130' // Aircon
          && device.deviceType !== '0x03b7' // Refridge
  && device.deviceType !== '0x03c5' // Washer
  )) {
    device.powersensor = undefined;
    $("#device_power_logger").html('<option value="none">None</option>');
    $("#device_power_logger").selectmenu('refresh', true);
  } else {
    var powerDists = kHAPI.dev.findAssignableDevices('ECHONET Lite', '0x0287');
    var powerDevOpts = '<option value="none">None</option>';
    for (var pdi = 0; pdi < powerDists.length; ++pdi) {
      var powerDist = powerDists[pdi];
      if (powerDist.isEmulation === true) continue;
      powerLogerOptionEnabled = true;
      for (var pdich = 1; pdich < 33; ++pdich) {
        powerDevOpts += '<option value="c' + pdich + '_' + powerDist.nickname
                + '">ch' + pdich + '/' + powerDist.nickname + '</option>';
      }
    }
    $("#device_power_logger").html(powerDevOpts);

    if (device.powersensor === undefined
            || device.powersensor.indexOf('_') == -1) {
      device.powersensor = undefined;
      $("#device_power_logger").val('none');
    } else {
      var _idx = device.powersensor.indexOf('_');
      var ts = [device.powersensor.substring(0, _idx),
          device.powersensor.substring(_idx + 1)];
      if (ts[0].charAt(0) !== 'c' || parseInt(ts[0].substring(1)) <= 0
              || parseInt(ts[0].substring(1)) > 32
              || kHAPI.findDeviceByNickname(ts[1]) === undefined) {
        device.powersensor = undefined;
        $("#device_power_logger").val('none');
      } else
        $("#device_power_logger").val(device.powersensor);
    }
    $("#device_power_logger").selectmenu('refresh', true);
    $("#device_power_logger").unbind('change').bind('change', function(e, u) {
      device.powersensor = (this.value === 'none' ? undefined : this.value);
    });
  }
  $("#device_power_logger").prop("disabled", !powerLogerOptionEnabled);

  $("#remove_device").unbind("click");
  $("#remove_device").click(function() {
    onClickRemoveDevice(nickname);
  });
  $("#property_table_body").html("");
  if (device.protocol === 'ECHONET Lite') {
    var jsfnam = parseInt(device.deviceType).toString(16).toUpperCase();
    while (jsfnam.length < 4)
      jsfnam = '0' + jsfnam;
    $.ajax({
      url: 'devices/0x' + jsfnam + '.json',
      dataType: 'json',
      success: function(d) {
        if ((typeof d) === 'string') d = JSON.parse(d);
        for (var mi = 0; mi < d.methods.length; mi++) {
          var m = d.methods[mi];
          $("#property_table_body").append(
                  "<tr>" + "<th>" + m.epc + "</th>" + "<td>" + m.name + "</td>"
                          + "<td>" + m.get + "/" + m.set + "</td>" + "<td>"
                          + "---" + "</td>" + "</tr>");
        }
      }
    });
    $("#get_property_button").unbind("click");
    $("#get_property_button").click(
            function() {
              // get get property.
              myget([nickname, "0x9f"], function(data, success) {
                if (!success || data.property === undefined) { return; }
                var table = kHAPI
                        .propertyMapToProperties(data.property[0].value);
                var send = table.slice(0);
                send.unshift(nickname);
                myget(send, function(data, success) {
                  if (!success) {
                    toast("Failed to properties");
                    return;
                  }
                  for (var j = 0; j < data.property.length; j++) {
                    var tr = $("#property_table tbody tr");

                    for (var i = 0; i < tr.length; i++) {
                      var cells = tr.eq(i).children();
                      var epc = cells.eq(0).text();

                      if (epc.toUpperCase() === data.property[j].name
                              .toUpperCase()
                              && data.property[j].value !== undefined) {
                        cells.eq(3).text(
                                data.property[j].value.map(function(x) {
                                  var s = x.toString(16);
                                  while (s.length < 2)
                                    s = "0" + s;
                                  return "0x" + s;
                                }));
                      }
                    }
                  }
                });
              });
            });
  }
};

var deviceRemocons = {
  // Aircon
  // http://www.survivingnjapan.com/2012/07/use-air-conditioner-japan.html
  "0x0130": [{
    epc: "0x80",
    edt: [0x30],
    text: "PowerOn"
  }, {
    epc: "0x80",
    edt: [0x31],
    text: "PowerOff"
  }, {
    epc: "0xB0",
    edt: [0x41],
    text: "Auto mode"
  }, {
    epc: "0xB0",
    edt: [0x42],
    text: "Cool mode"
  }, {
    epc: "0xB0",
    edt: [0x43],
    text: "Heat mode"
  }, {
    epc: "0xB0",
    edt: [0x44],
    text: "Dehumidify mode"
  }],

  // GeneralLighting
  "0x0290": [{
    epc: "0x80",
    edt: [0x30],
    text: "PowerOn"
  }, {
    epc: "0x80",
    edt: [0x31],
    text: "PowerOff"
  }],
  // curtain
  "0x0262": [{
    epc: "0xe0",
    edt: [0x41],
    text: "Open"
  }, {
    epc: "0xe0",
    edt: [0x42],
    text: "Close"
  }],
  // ElectricallyOperatedShade remember there is no stop.
  "0x0260": [{
    epc: "0xe0",
    edt: [0x41],
    text: "Open"
  }, {
    epc: "0xe0",
    edt: [0x42],
    text: "Close"
  }],
  // Water heater
  "0x026b": [{
    epc: "0xb0",
    edt: [0x41],
    text: "Auto boil"
  }, {
    epc: "0xb0",
    edt: [0x42],
    text: "Manual boil"
  }, {
    epc: "0xb0",
    edt: [0x43],
    text: "Manual boil stop"
  }, {
    epc: "0xd1",
    edt: [40],
    text: "Supply 40℃"
  }, {
    epc: "0xd1",
    edt: [42],
    text: "Supply 42℃"
  }, {
    epc: "0xd1",
    edt: [44],
    text: "Supply 44℃"
  }, {
    epc: "0xd3",
    edt: [38],
    text: "Bath 38℃"
  }, {
    epc: "0xd3",
    edt: [40],
    text: "Bath 40℃"
  }, {
    epc: "0xd3",
    edt: [42],
    text: "Bath 42℃"
  }, {
    epc: "0xe3",
    edt: [0x41],
    text: "Bath water auto"
  }, {
    epc: "0xe3",
    edt: [0x42],
    text: "Bath water manual"
  }, {
    epc: "0xe4",
    edt: [0x41],
    text: "Heat refill on"
  }, {
    epc: "0xe4",
    edt: [0x42],
    text: "Heat refill off"
  }, {
    epc: "0xe6",
    edt: [0x41],
    text: "Warm on"
  }, {
    epc: "0xe6",
    edt: [0x42],
    text: "Warm off"
  }]
};

var makeDeviceRemocon = function(nickname) {
  // helper function. prop is string,val is list.

  var makeButton = function(text, func) {
    return "<input type='button' onclick='{func}' value='{text}' />".format({
      func: func,
      text: text
    });
  };

  var makeButtonIN = function(text, func) {
    return "<input type='button' onclick='{func}' value='{text}' data-inline='true' />"
            .format({
              func: func,
              text: text
            });
  };

  var makeECHONETLiteDeviceRemocon = function(device) {
    var makeSetFunction = function(nickname, prop, val) {
      return "myset([\"{nickname}\",[\"{prop}\",[{val}]]],function(msg){});"
              .format({
                nickname: nickname,
                prop: prop,
                val: val
              });
    };
    var makeSetButton = function(nickname, prop, val, text) {
      return makeButton(text, makeSetFunction(nickname, prop, val));
    };
    var ret = "";
    if (device.deviceType in deviceRemocons) {
      var re = deviceRemocons[device.deviceType];
      for (var i = 0; i < re.length; i++) {
        ret += makeSetButton(device.nickname, re[i].epc, re[i].edt, re[i].text);
      }
    }

    var makeSetButtonIN = function(nickname, prop, val, text) {
      return makeButtonIN(text, makeSetFunction(nickname, prop, val));
    };
    if (device.deviceType === '0x0130') {
      ret += 'Temperature<br>';
      for (var t = 18; t <= 30; t++) {
        ret += makeSetButtonIN(device.nickname, '0xb3', [t], t + '℃');
      }
    } else if (device.deviceType === '0x0260') {
      ret += "Close Level<br>";
      for (var l = 0; l < 8; l++) {
        ret += makeSetButtonIN(device.nickname, '0xe1', [0x31 + l], l + "");
      }
      ret += "<br> Shade Angle <br>";
      ret += '<label for="blindAngleSlider">Input slider:</label>'
              // +'<input type="range" name="slider" id="blindAngleSlider"
              // min="0" max="180" onchange="console.log(this.value); " />' ;
              + '<input type="range" name="slider" id="blindAngleSlider" min="0" max="180" '
              + 'onchange="myset([\'{nickname}\',[\'0xe2\',[this.value]]],function(msg){}); " />'
                      .format({
                        nickname: device.nickname
                      });
      // for(var l=0;l<=180/20;l++){
      // ret += makeSetButtonIN(device.nickname,'0xe2',[20*l],20*l+"′");
      // }
    }

    return ret;
  };

  var device = kHAPI.findDeviceByNickname(nickname);

  return makeECHONETLiteDeviceRemocon(device);
};

// arg is index of manifests
var onAppPageOpen = function(index) {
  kHAPI.openAppPageByManifestIndex(index);
  return false;
};

var onAppSettingPageOpen = function(index) {
  var manifest = manifests[index];
  var description = ("<img src='{image}'/>"
          + "<font size=+4>{appname}</font><br>" + "{subtitle} <br><hr>"
          + "{descript}").format({
    appname: escapeHTML(manifest.title),
    subtitle: escapeHTML(manifest.subtitle),
    descript: escapeHTML(manifest.description),
    image: manifest.icon
  });

  var matching_html = (manifest.devices.length > 0
          ? "<big>Associate devices for this app.</big><br>" : "");
  var select_prefix = "device_choice";
  var selected_values = [];
  for (var i = 0; i < manifest.devices.length; i++) {
    var dev = manifest.devices[i];
    var select_name = select_prefix + i;

    var template_p = "<label for={select_name} class='select'>{description}</label>"
            + "<select name={select_name}>";
    var template_a = "</select>";
    var template_option = "<option value='{value}'>{text}</option>";

    var output = template_p.format({
      select_name: select_name,
      description: dev.description
    });

    var real_devices = kHAPI.getDevices();

    output += template_option.format({
      value: "none",
      text: "---"
    });

    var selected_value = "none";
    for (var j = 0; j < real_devices.length; j++) {
      var rv = real_devices[j];
      if (rv.deviceType === dev.deviceType && rv.protocol === dev.protocol
              && rv.active === true) {
        output += template_option.format({
          value: rv.nickname,
          text: escapeHTML(rv.nickname)
        });
        if (rv.nickname === dev.assignedDevName) {
          selected_value = rv.nickname;
        }
      }
    }
    selected_values.push(selected_value);

    output += template_a;
    matching_html += output;
  }

  $("#app_description").html(description);
  $("#device_matching").html(matching_html);

  $("#app_description").trigger("create");
  $("#device_matching").trigger("create");

  for (var i = 0; i < manifest.devices.length; i++) {
    var bind_i = i;
    // to bind environment.
    var changed_generator = function(manifest_index, index) {
      return function(event, ui) {
        // can overwrite?
        manifests[manifest_index].devices[index].assignedDevName = $(this)
                .val();
        kHAPI.addManifest(manifests[manifest_index]);
      };
    };
    $("*[name=" + select_prefix + i + "]").bind("change",
            changed_generator(index, i));
    $("*[name=" + select_prefix + i + "]").val(selected_values[i]).selectmenu(
            "refresh");
  }
  $.mobile.changePage("#app_settings_page");
};

var onClickChangeNickname = function(from, to) {
  var args = {
    currentName:from,
    newName:to
  }
  kHAPI.changeNickname(args);
  $.mobile.changePage("#devlist_page");
};

var onClickRemoveDevice = function(nickname) {
  var args = {
    targetName:nickname,
  }
  kHAPI.deleteDevice(args);
  kHAPI.reqDevListHandlers_onUpdateList();
};

var onClickFullInitializeButton = function() {
  areYouSure("Initialize server settings?",
          "This function will delete nickname settings,apps settings.", "OK",
          "Cancel", function() {
            kHAPI.fullInitialize();
          });
  return false;
};

var toast = function(message) {
  message = escapeHTML(message);
  var t = $(
          "<div class='ui-loader ui-overlay-shadow "
                  + "ui-body-a ui-corner-all'>" + message + "</div>").css({
    display: "block",
    opacity: 0.9,
    padding: "10px"
  });

  var left = ($(window).width() - t.width()) / 2;
  var top = $(window).height() - 100;

  t.css({
    top: top,
    left: left
  }).appendTo($.mobile.pageContainer).delay(1000).fadeOut(400, function() {
    $(this).remove();
  });
};

var makeDeviceLi = function(device) {
  var id = getNicknameHash(device.nickname);
  var template = "<li id='{id}'> "
          + "<a onclick='onDeviceIconClick(\"{nickname}\");return false;'> "
          + "<div class='padding-div'></div> "
          + "<img id='devIconImg{id}' src='{imageurl}' \
                  onerror='this.src=\"index_res/icons/error.png\";'/>"
          + " <div id='statusDiv{id}' style='position:absolute; top:5px; left:5px;'> "
          + "{nickname}<br />"
          + "<img id='statusPowerImg{id}' src='index_res/icons/PowerInactive.png'/> "
          + "<span id='statusDivTxtArea{id}'></span> "
          + "</div>"
          + "<div id='devIconBottomTxt{id}'style='position:absolute;bottom:0px;right:5px;'></div>"
          + "<div id='devLoadingIcon{id}'style='position:absolute;bottom:0px;left:5px;'></div>"
          + "</a>"
          +

          "<input type='button' value='Settings' class='jqmNone'"
          + "onclick='onDeviceDetailPageOpen(\"{nickname}\");' style='width:100%' ></input>"
          + "</li>";
  return template.format({
    id: id,
    nickname: escapeHTML(device.nickname),
    imageurl: getImageUrl(device)
  }).removeAllNewline();
};

var manifests = [];
function refreshManifests(manifs) {
  if (!app_page_has_created) return;
  manifests = manifs;
  var app_list_view = $("#app_list_view");
  app_list_view.empty();

  var template = "<li id='{id}'>"
          + "<a href='#app_iframe_page' onclick='return onAppPageOpen({index});'>"
          + "<div class='padding-div'></div>"
          + "<img src='{image}' /> "
          + "<div style='position:absolute; top:0px; left:0px;'>{title}</div>"
          + "</a>"
          + "<input type='button' value='Settings' class='jqmNone'"
          + "onclick='onAppSettingPageOpen({index})' style='width:100%' ></input>"
          + "</li>";

  for (var ai = 0; ai < manifs.length; ++ai)
    app_list_view.append(template.format({
      id: getNicknameHash(manifs[ai].title),
      index: ai,
      image: manifs[ai].icon,
      title: escapeHTML(manifs[ai].title)
    }).removeAllNewline());

  try {
    app_list_view.listview("refresh");
    app_list_view.trigger("create");
  } catch (e) { /* alert(e) ; */
  }
}

// utility
var isECHONETLite = function(device) {
  return device.protocol === "ECHONET Lite";
};
var isControler = function(device) {
  return isECHONETLite(device) && parseInt(device.deviceType) == 0x5ff;
};

var getImageUrl = function(device) {
  var imgurl = 'index_res/icons/' + device.deviceType + '.png';
  var bStatusRequested = false;

  if (device.protocol === 'ECHONET Lite') {
    if (device.deviceType === '0x0011' || device.deviceType === '0x0012'
            || device.deviceType === '0x0262' || device.deviceType === '0x0290'
            || device.deviceType === "0x0260" || device.deviceType === "0x026b") {
      imgurl = 'index_res/icons/' + device.deviceType.substring(0, 6) + ".png";
    } else {
      imgurl = 'index_res/icons/' + device.deviceType.substring(0, 4) + ".png";
    }
  }
  return imgurl;
};

// all refresh.
var refreshDevStatusForIcon = function(nickname) {
  var d;
  if (typeof nickname === "string") {
    d = kHAPI.findDeviceByNickname(nickname);
  } else { // if d is device type.
    d = nickname;
    nickname = d.nickname;
  }
  var id = getNicknameHash(nickname);
  if (d.isEmulation) {
    $("#devIconBottomTxt" + id).html("Emulation");
  }
  var epc_map = update_map(nickname);

  if (!d.active) {
    $("#devLoadingIcon" + id).html("InActive");
  } else {
    $("#devLoadingIcon" + id).html("");
  }
  for ( var epc in epc_map) {
    myget([nickname, epc], function(ret, success) {
      if (!success || ret.property[0].value === undefined) return;
      epc_map[ret.property[0].name](ret.property[0].value);
    });
  }
};

// return is like {0x80:function(edt){},0xe0:function(edt){}...}
var update_map = function(nickname) {
  var d;
  if (typeof nickname === "string") {
    d = kHAPI.findDeviceByNickname(nickname);
  } else { // if d is device type.
    d = nickname;
    nickname = d.nickname;
  }
  var id = getNicknameHash(nickname);
  var status_txt_area = $('#statusDivTxtArea' + id);
  var icon_bottom_text = $('#devIconBottomTxt' + id);
  var dev_icon_img = $('#devIconImg' + id)[0];
  var status_power_img = $('#statusPowerImg' + id)[0];

  // shared by many functions.
  var update_power_value = function(newval) {
    if (newval[0] == 48) {
      status_power_img.src = 'index_res/icons/PowerOn.png';
    } else if (newval[0] == 49) {
      status_power_img.src = 'index_res/icons/PowerOff.png';
    }
  };
  var update_power_meter = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xc6'] = function(newval) {
      if (!d.isEmulation) {
        var watts = echoByteArrayToInt(newval);
        icon_bottom_text.html(watts + 'W');
      }
      ;
    };
    return ret;
  };
  // TODO: powersensor.
  var update_echonetlite_aircon = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xb3'] = function(newval) {
      var cTS = newval[0];
      if (cTS >= 0x80) cTS = cTS - 0x100;
      status_txt_area.html('  ' + cTS + ' deg C');
    };
    return ret;
  };
  var update_temperature_sensor = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xe0'] = function(newval) {
      var temp = getTempFromECHONETLite2ByteArray(newval);
      status_txt_area.html('  ' + temp + ' deg C');
    };
    return ret;
  };
  var update_humidity_sensor = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xe0'] = function(newval) {
      status_txt_area.html('  ' + newval[0] + ' %');
    };
    return ret;
  };
  var update_other_sensors = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xb1'] = function(newval) {
      status_txt_area.html('  '
              + (newval[0] == 0x41 ? '<font color="#c00">ON<font>' : 'OFF'));
    };
    return ret;
  };
  var update_illuminance_sensor = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xe0'] = function(newval) {
      status_txt_area.html('  ' + echoByteArrayToInt(newval) + 'lx');
    };
    return ret;
  };
  var update_curtain = function() {
    var ret = Object.create(null);
    ret['0x80'] = update_power_value;
    ret['0xe0'] = function(newval) {
      if (newval[0] == 0x41) {
        dev_icon_img.src = 'index_res/icons/0x0262-open.png';
      } else {
        dev_icon_img.src = 'index_res/icons/0x0262.png';
      }
    };
    return ret;
  };
  var update_light = function() {
    var ret = Object.create(null);
    ret['0x80'] = function(newval) {
      if (newval[0] == 48) {
        status_power_img.src = 'index_res/icons/PowerOn.png';
        dev_icon_img.src = 'index_res/icons/0x0290-on.png';
      } else if (newval[0] == 49) {
        status_power_img.src = 'index_res/icons/PowerOff.png';
        dev_icon_img.src = 'index_res/icons/0x0290.png';
      }
    };
    return ret;
  };

  var update_power_0x80 = function(epc) {
    var ret = Object.create(null);
    ret['0x80'] = function(newval) {
      status_power_img.src = 'index_res/icons/Power'
              + (d.active ? 'On' : 'Off') + '.png';
    };
    return ret;
  };

  if (d.protocol === 'ECHONET Lite') {
    var dtNum = parseInt(d.deviceType);
    // PowerDistributionBoardMetering
    if (dtNum == 0x0287) {
      return update_power_meter();
    } else if (dtNum == 0x0130) { // Aircon
      return update_echonetlite_aircon();
    } else if (dtNum == 0x0011) { // Temperature
      return update_temperature_sensor();
    } else if (dtNum == 0x0012) { // Humidity
      return update_humidity_sensor();
    } else if ((0 < dtNum && dtNum <= 0x000b) // Gas sensor to Air polution
                                              // sensor
            || (0x000e <= dtNum && dtNum <= 0x0010) // Sound, Posting, Weight
            || (0x0013 <= dtNum && dtNum <= 0x001a) // Rain to Smoke
            || (0x001c <= dtNum && dtNum <= 0x001d) // Gas, VOC
            || (0x0020 <= dtNum && dtNum <= 0x0021) // Smell, Fire
            || (0x0026 == dtNum) // Small movement
            || (0x0028 <= dtNum && dtNum <= 0x0029) // Floor, Openclose
            || (0x002c == dtNum) // Snow
            || (0x001f == dtNum) // AirSpeed
    ) {
      return update_other_sensors();
    } else if (dtNum == 0x000d) { // Illuminance sensor
      return update_illuminance_sensor();
    } else if (dtNum == 0x0262 || dtNum == 0x0260) { // Curtain or
                                                      // ElectricallyOperatedShade
      return update_curtain();
    } else if (dtNum == 0x0290) { // Light
      return update_light();
    } else {
      return update_power_0x80();
    }
  } else if (d.protocol == 'AndroidSensors') {
    status_power_img.src = 'index_res/icons/PowerOn.png';
  }
  return Object.create(null);
};

var update_one_epc = function(nickname, epc, edt) {
  var epc_map = update_map(nickname);
  if (epc in epc_map) {
    if (edt === undefined) {
      myget([nickname, epc], function(ret, success) {
        if (!success || ret.property[0].value === undefined) return;
        epc_map[epc](ret.property[0].value);
      });
    } else {
      epc_map[epc](edt);
    }
  }
};

var onDeviceIconClick = function(nickname) {
  var d = kHAPI.findDeviceByNickname(nickname);
  // if null is substituted to handler, the power is changed.
  // if this value remains false, refresh device info.

  var bTogglePower = false;
  var newVal;
  if (d.protocol === 'ECHONET Lite') {
    // Curtain or ElectricallyOperatedShade
    if (d.deviceType === '0x0262' || d.deviceType === "0x0260") {
      // Curtain open/close
      myget([nickname, '0xe0'], function(ret, success) {
        if (success == true && ret.property[0].value !== undefined)
          newVal = [ret.property[0].value[0] == 0x41 ? 0x42 : 0x41];
        else
          newVal = [0x41];
        myset([nickname, ['0xe0', newVal]], function() {
          update_one_epc(d, '0xe0', newVal);
        });
      });
      return;
    } else if (d.deviceType === '0x0130') { // Aircon
      bTogglePower = true;
    } else if (d.deviceType === '0x0290') { // Light
      bTogglePower = true;
    }
  }

  if (bTogglePower) {
    // Power
    myget([nickname, '0x80'], function(ret, success) {
      if (success == true && ret.property[0].value !== undefined)
        newVal = [ret.property[0].value[0] == 48 ? 49 : 48];
      else
        newVal = [48];

      myset([nickname, ['0x80', newVal]], function() {
        update_one_epc(d, '0x80', newVal);
      });
    });
  } else {
    refreshDevStatusForIcon(d);
  }
};

var access_queuing = function(nickname) {
  var id = getNicknameHash(nickname);
  $("#devLoadingIcon" + id).html("<img src='index_res/icons/loading.gif' />");
};

var access_ending = function(nickname) {
  var id = getNicknameHash(nickname);
  $("#devLoadingIcon" + id).html("");
};

var access_count = Object.create(null);
// to count access.make wrapper.
var myget = function(args, callback, with_out_queue) {
  var nickname = args[0];
  var dev = kHAPI.findDeviceByNickname(nickname);
  if (dev === undefined || dev === null || !dev.active) { return; }
  if (!(nickname in access_count)) {
    access_count[nickname] = 0;
  } else if (access_count[nickname] == 0) {
    access_queuing(nickname);
  }
  access_count[nickname]++;

  kHAPI.get(args, function(ret, success) {
    access_count[nickname]--;

    if (callback !== undefined) {
      callback(ret, success);
    }
    if (access_count[nickname] == 0) {
      access_ending(nickname);
    }
  }, with_out_queue);
};

var myset = function(args, callback) {
  var nickname = args[0];
  var dev = kHAPI.findDeviceByNickname(nickname);
  if (dev === undefined || dev === null || !dev.active) { return; }
  if (!(nickname in access_count)) {
    access_count[nickname] = 0;
  } else if (access_count[nickname] == 0) {
    access_queuing(nickname);
  }
  access_count[nickname]++;

  kHAPI.set(args, function(ret, success) {

    if (callback !== undefined) {
      callback(ret, success);
    }
    access_count[nickname]--;
    if (access_count[nickname] == 0) {
      access_ending(nickname);
    }
  });
};

var areYouSure = function(big, small, yestext, notext, yescallback, nocallback) {
  $("#sure .big-message").text(escapeHTML(big));
  $("#sure .small-message").text(escapeHTML(small));

  $("#sure .sure-do .ui-btn-inner .ui-btn-text").text(yestext);
  $("#sure .no .ui-btn-inner .ui-btn-text").text(notext);

  $("#sure .sure-do").unbind("click");
  $("#sure .no").unbind("click");

  $("#sure .sure-do").click(function() {
    if (yescallback !== undefined) {
      yescallback();
    }
    $(this).off("click.sure");
  });
  $("#sure .no").click(function() {
    if (nocallback !== undefined) {
      nocallback();
    }
    $(this).off("click.no");
  });
  // $.mobile.changePage("#sure",{transition:"slidedown"});
  $.mobile.changePage("#sure", {
    transition: "none"
  });
};

// ID and NAME tokens must begin with a letter ([A-Za-z])
// and may be followed by any number of letters,
// digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods
// (".").
// http://www.w3.org/TR/REC-html40/types.html#type-name
// use rolling hash.
var getNicknameHash = function(nickname) {
  var prefix = "dev_";
  var mod = 4294967296;
  var b = 1007;
  var r = 0;
  for (var i = 0; i < nickname.length; i++) {
    r = r * b + nickname.charCodeAt(i);
    r = r % mod;
  }
  return prefix + r;
};

var getUNIXTime = function(date) {
  if (date === undefined)
    return new Date() / 1;
  else
    return date / 1;
};

var UNIXTimeToDate = function(unixtime) {
  return new Date(unixtime);
};

var getToday = function(date) {
  if (date === undefined) date = new Date();
  date.setHours(0);
  date.setMinutes(0);
  date.setSeconds(0);
  return date;
};

var getHour = function(data, hours) {
  if (data === undefined) data = new Date();
  if (hours === undefined) hours = -1;
  return new Date(getUNIXTime(data) + 3600000 * hours);
};

// http://stackoverflow.com/questions/6020714/escape-html-using-jquery
var escapeHTML = function(string) {
  var htmlEscapes = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#x27;',
    '/': '&#x2F;'
  };

  // Regex containing the keys listed immediately above.
  var htmlEscaper = /[&<>"'\/]/g;

  return ('' + string).split('<br>').join('%%_BR_%%').replace(htmlEscaper,
          function(match) {
            return htmlEscapes[match];
          }).split('%%_BR_%%').join('<br>');
};
