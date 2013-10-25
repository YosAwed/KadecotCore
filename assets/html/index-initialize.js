// Remember!escape HTML.
// http://tmlife.net/programming/javascript/javascript-string-format.html
if(String.prototype.format === undefined){
  String.prototype.format = function(args){
    var replace_function;
    if(typeof args === "object"){
      replace_function = function(m,k){
        return args[k];
      };
    }else{
      replace_function = function(m,k){
        return arguments[parseInt(k)];
      };
    }
    return this.replace(/\{(\w+)\}/g,replace_function);
  };
}
if(String.prototype.removeAllNewline === undefined){
  String.prototype.removeAllNewline = function(){
    return this.replace(/\r?\n|\r/g,' ');
  };
}
if(Date.prototype.myLocaleString === undefined){
  Date.prototype.myLocaleString = function(){
    var ret = "";
    ret += this.getMonth()+1 + "/";
    ret += this.getDate();
    ret += " ";
    ret += this.getHours() + ":";
    ret += this.getMinutes() +":";
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

$(document).on("pagecreate",function(){
  $.mobile.defaultPageTransition = "none";
  $.mobile.defaultDialogTransition = "none";
  $.mobile.useFastClick = true;
  if(pagecreated) return;
  pagecreated = true;

  kHAPI.init(function(){
    kHAPI.onServerConnected = function(){
      toast("connected");
      $("#server_ip").parent().css("background-color","lightgreen");
      $("#connect_button").val("Disconnect").button("refresh");
    };
    kHAPI.onServerDisconnected = function(){
      toast("disconnected");
      $("#server_ip").parent().css("background-color","transparent");
      $("#connect_button").val("Connect").button("refresh");
    };
    kHAPI.onServerConnectionFailed= function(){
      toast("Failed to connect");
    };
    kHAPI.devListHandlers.onUpdateList = function(devs){
      if(!devlist_page_has_created || !(devs instanceof Array)) return false;
      var device_list_view = $("#device_list_view");
      device_list_view.empty();

      for(var i=0;i<devs.length;i++){
        var dev = devs[i];

        if(isControler(dev)) continue;
        device_list_view.append(makeDeviceLi(dev));
      }
      device_list_view.listview().trigger("create");
      device_list_view.listview().listview("refresh");
      return true;
    };


	kHAPI.devListHandlers.onDeviceFound = function(newdevice , newlist){
		if(!devlist_page_has_created || typeof newdevice !== 'object')
			return kHAPI.devListHandlers.onUpdateList(newlist) ;

		if(isControler(newdevice)) return false ;

		var device_list_view = $("#device_list_view");

		device_list_view.prepend(makeDeviceLi(newdevice));

		device_list_view.listview().trigger("create");
		device_list_view.listview().listview("refresh");

		queryDevStatusForIcon( newdevice.nickname ) ;

		return true;
	} ;

	kHAPI.devListHandlers.onDeviceActivated = function(newdevice , newlist){
		if(!devlist_page_has_created || typeof newdevice !== 'object')
			return kHAPI.devListHandlers.onUpdateList(newlist) ;

		if(isControler(newdevice)) return false ;
		queryDevStatusForIcon( newdevice.nickname ) ;
		return true ;
	} ;


    /*  // ToDo : update only necessary icons
	kHAPI.devListHandlers.onNicknameChanged = function(oldnickname , newnickname , newlist){
		//var id = getNicknameHash(oldnickname) ;
		//queryDevStatusForIcon(oldnickname) ;
	} ;
	kHAPI.devListHandlers.onDeviceDeleted = function(delNickname , newlist){
	} ;
    */


    kHAPI.onPropertyChanged = function(prop_change){
      if(isDeviceOnDetail(prop_change.nickname)){
        onPropertyChangedOnDetail(prop_change);
      } else {
		queryDevStatusForIcon( prop_change.nickname ) ;
      }
    };

    kHAPI.onNotifyServerSettings = function(network_info){
      if(kHAPI.isOnAndroid){
        onNotifyServerSettingsOnAndroid(network_info);
      }else{
        onNotifyServerSettingsOnBrowser(network_info);
      }
    };

    var oldOnBackBtn = kHAPI.onBackBtn ;
    kHAPI.onBackBtn = function(){
		if( typeof oldOnBackBtn === 'function' )
			oldOnBackBtn() ;
		$.mobile.changePage("#devlist_page");
    } ;


    kHAPI.reqDevListHandlers_onUpdateList() ;
    kHAPI.readManifests(refreshManifests);
  });
});

$("#devlist_page").on("pageinit",function(){
  devlist_page_has_created = true;
  kHAPI.reqDevListHandlers_onUpdateList() ;
});
$("#app_page").on("pageinit",function(){
  app_page_has_created = true;
});
$("#app_page").on("pageshow",function(){
  kHAPI.readManifests(refreshManifests);
});

// Log initialize
$("#log_page").on("pageshow",function(){
  $("#log_area").height($(window).height()
                        - $("#log_page .ui-header").outerHeight(true)
                        - $("#refresh_log_button").outerHeight(true)-22);
  getSimpleLog();
});

$(window).resize(function(){
  $("#log_area").height($(window).height()
                        - $("#log_page .ui-header").outerHeight(true)
                        - $("#refresh_log_button").outerHeight(true)-22);
});

var onClickRefreshLogButton = function(){
  getSimpleLog();
  return false;
};

var getSimpleLog = function(){
  // 9/26 21:15:14
  var one_hours_ago = getUNIXTime(getHour(new Date(),-1));
    if(kHAPI.queryLog !== undefined){
      kHAPI.queryLog([one_hours_ago,-1],function(raw_data){
        var str = "";
        for(var i=raw_data.length-1;i>=0;i--){
          var message = "{date} {nickname} {type} {epc} {edt} ({success} : {message})".format(
            {date:new Date(parseInt(raw_data[i].unixtime)).myLocaleString(),
             nickname:raw_data[i].nickname,
             type:raw_data[i].access_type,
             epc:raw_data[i].property_name,
             edt:raw_data[i].property_value,
             success:raw_data[i].success==="true"?"success":"fail",
             message:(typeof raw_data[i].message==='string'?raw_data[i].message:'null')});
            str += message + "\n";
        }
        $("#log_area").val(str);
    });
  }
};

// events. ---------------------------------------------------------------------
var onClickGetGeoLocation = function(){
  "use strict";
  areYouSure("Allow using geolocation?",
             "Geolocation will be used to collect air temperature,humidity or weather etc. ",
             "OK",
             "Cancel",function(){
               navigator.geolocation.getCurrentPosition(
                 function(pos,t){
                   kHAPI.setLocation([pos.coords.latitude,pos.coords.longitude]);
                   toast('Refreshed location('
                         +pos.coords.latitude+' , '
                         +pos.coords.longitude+' )') ;
                 }
                 ,function(error){
                   toast('Failed to collect location'+error.code+':'+error.message) ;
                 }
                 , {enableHighAccuracy:true,timeout:30*1000}
               );
             }) ;
  return false;
};

var onClickConnectButton = function(button){
  "use strict";
  if(kHAPI.isConnected()){
    kHAPI.disconnectServer();
  }else{
    kHAPI.connectServer($("#server_ip").val());
  }
};

// used by first confirm.
var onClickRegisterButton = function(isFirstConfirm){
  if(isFirstConfirm === undefined) isFirstConfirm = false;
  var netInfo = kHAPI.getNetInfo();
  var isRegistered = netInfo.network.isDeviceAccessible;
  var SSID = netInfo.network.SSID.split('"').join('');

  if(!isRegistered){
    areYouSure("Register " + SSID + " as homenetwork?",
               "Registering network will enable access to devices.",
               "OK",
               "Cancel",function(){
                 kHAPI.enableServerNetwork(true);
                 if(!isFirstConfirm){
                   $("#register_android_button").val("Unregister network").button("refresh");
                 }
                 toast("registered");
               });

  }else{
    kHAPI.enableServerNetwork(false);
    if(!isFirstConfirm){
      $("#register_android_button").val("Register this network as homenetwork").button("refresh");
    }
    toast("unregistered");
  }
};

// TODO: refactoring not to use global variable.(to support opening in tab.)
var isDeviceOnDetail = function(nickname){
  return false;
};

var onClickSettingsPageButton = function(){
  if(kHAPI.isOnAndroid){
    $.mobile.changePage("#settings_page_android");
    onSettingsPageAndroidOpen();
  }else{
    $.mobile.changePage("#settings_page_browser");
    onSettingsPageBrowserOpen();
  }
};

var onSettingsPageAndroidOpen = function(){
  //   network : {"isConnected" : true , "type" : "wifi", "SSID" : "aaa",
  //                "ip" : "1.1.1.1", "isDeviceAccessible" : true}  // network
  //   location : []
  //   jsonp : true
  //   websocket: true
  //   persistence: true // persistence

  var netInfo = kHAPI.getNetInfo() ;

  $("#server_android_ip").val(netInfo.network.ip).textinput();
  if(netInfo.network.isDeviceAccessible){
    $("#register_android_button").val("Unregister this network")
                                 .button("refresh");
  }else{
    $("#register_android_button").val("Register this network as homenetwork")
                                 .button("refresh");
  }
  initializeCheckBox($("#enable_websocket_checkbox"),netInfo.websocket,
                     function(){kHAPI.enableWebSocketServer([true]);},
                     function(){kHAPI.enableWebSocketServer([false]);});

  initializeCheckBox($("#enable_persistent_android_checkbox"),netInfo.persistence,
                     function(){kHAPI.enablePersistentMode([true]);},
                     function(){kHAPI.enablePersistentMode([false]);});

  initializeCheckBox($("#enable_jsonp_android_checkbox"),netInfo.jsonp,
                     function(){kHAPI.enableJSONPServer([true]);},
                     function(){kHAPI.enableJSONPServer([false]);});
};

var onSettingsPageBrowserOpen = function(){
  "use strict";
  var netInfo = kHAPI.getNetInfo();
  initializeCheckBox($("#enable_persistent_browser_checkbox"),netInfo.persistence,
                    function(){kHAPI.enablePersistentMode([true]);},
                    function(){kHAPI.enablePersistentMode([false]);});

  initializeCheckBox($("#enable_jsonp_browser_checkbox"),netInfo.jsonp,
                    function(){kHAPI.enableJSONPServer([true]);},
                    function(){kHAPI.enableJSONPServer([false]);});
};

var initialNotifyServerSettings = true;
var onNotifyServerSettingsOnAndroid = function(settings){
  checkCheckBox($("#enable_websocket_checkbox"),settings.websocket);
  checkCheckBox($("#enable_persistent_android_checkbox"),settings.persistence);
  checkCheckBox($("#enable_jsonp_android_checkbox"),settings.jsonp);

  if(initialNotifyServerSettings &&
     settings.network.type === "WIFI" &&
     !settings.network.isDeviceAccessible){
    onClickRegisterButton(true);
  }
  initialNotifyServerSettings = false;
};

var onNotifyServerSettingsOnBrowser = function(settings){
  checkCheckBox($("#enable_persistent_browser_checkbox"),settings.persistence);
  checkCheckBox($("#enable_jsonp_browser_checkbox"),settings.jsonp);
};

// checkbox is $(hoge),initial_value is weather true or false.
var initializeCheckBox = function(checkbox,initial_value,on_function,off_function){
  "use strict";
  if(initial_value === undefined){
    initial_value = false;
  }
  checkbox.unbind("click");
  checkCheckBox(checkbox,initial_value);
  checkbox.click(function(){
    if(!$(this).is(":checked")){
      off_function();
    }else{
      on_function();
    };
  });
};

var checkCheckBox = function(checkbox,checked){
  checkbox.checkboxradio().prop("checked",checked).checkboxradio("refresh");
};

// read current device info.
//  if the device is added by iRemocon,device is not undefined.
var onDeviceDetailPageOpen = function(nickname,device){
  $.mobile.changePage("#device_detail_page");
  if(device === undefined){
    device = kHAPI.findDeviceByNickname(nickname);
  }

  var description = ("<big>{nickname}</big><br>" +
                     "Type {deviceName} <br>" +
                     "Protocol {protocol} <br>"+
                     "{active}</div>").format({
                       nickname:escapeHTML(device.nickname)+(device.isEmulation===true?' (Emulation)':''),
                       deviceName:escapeHTML(device.deviceName),
                       protocol:escapeHTML(device.protocol),
                       active:(device.active?'Active':'Inactive')
                     });

  // if propertyChanged occured,
  isDeviceOnDetail = function(n) {return nickname == n;};

  $("#device_description").html(description);
  $("#device_remocon").html(makeDeviceRemocon(nickname));
  $("#device_remocon").trigger("create");
  $("#nickname_input").val(escapeHTML(nickname));
  $("#nickname_button").unbind("click");
  $("#nickname_button").click(function() {
    var new_nickname = $("#nickname_input").val();
    onClickChangeNickname(nickname,new_nickname);
  });

  // Power logger
  var powerLogerOptionEnabled = false ;
  if(   device.protocol !== 'ECHONET Lite'
    || ( device.deviceType !== '0x0130'         // Aircon
         && device.deviceType !== '0x03b7'      // Refridge
         && device.deviceType !== '0x03c5'      // Washer
       ) ){
    device.powersensor = undefined ;
    $("#device_power_logger").html('<option value="none">None</option>') ;
    $("#device_power_logger").selectmenu('refresh',true) ;
  } else {
    var powerDists = kHAPI.dev.findAssignableDevices('ECHONET Lite','0x0287') ;
    var powerDevOpts = '<option value="none">None</option>' ;
    for( var pdi = 0 ; pdi < powerDists.length ; ++pdi ){
      var powerDist = powerDists[pdi] ;
      if( powerDist.isEmulation === true ) continue ;
      powerLogerOptionEnabled = true ;
      for( var pdich = 1 ; pdich < 33 ; ++pdich ){
        powerDevOpts += '<option value="c'+pdich+'_'+powerDist.nickname+'">ch'+pdich+'/'+powerDist.nickname+'</option>' ;
      }
    }
    $("#device_power_logger").html(powerDevOpts) ;

    if( device.powersensor === undefined || device.powersensor.indexOf('_')==-1 ){
      device.powersensor = undefined ;
      $("#device_power_logger").val('none') ;
    } else {
      var _idx = device.powersensor.indexOf('_') ;
      var ts = [ device.powersensor.substring(0,_idx) , device.powersensor.substring(_idx+1) ] ;
      if( ts[0].charAt(0) !== 'c' || parseInt(ts[0].substring(1))<=0 || parseInt(ts[0].substring(1))>32
        || kHAPI.findDeviceByNickname(ts[1]) === undefined ){
       device.powersensor = undefined ;
       $("#device_power_logger").val('none') ;
        } else
      $("#device_power_logger").val(device.powersensor) ;
    }
    $("#device_power_logger").selectmenu('refresh',true) ;
    $("#device_power_logger").unbind('change').bind('change',function(e,u){
      device.powersensor = (this.value === 'none' ? undefined : this.value) ;
    }) ;
  }
  $("#device_power_logger").prop("disabled", !powerLogerOptionEnabled);

  $("#remove_device").unbind("click");
  $("#remove_device").click(function() {
    onClickRemoveDevice(nickname);
  });
  $("#property_table_body").html("");
  if(device.protocol === 'ECHONET Lite'){
    var jsfnam = parseInt(device.deviceType).toString(16).toUpperCase() ;
    while(jsfnam.length < 4) jsfnam = '0' + jsfnam;
    $.ajax({url:'devices/0x'+jsfnam+'.json' ,dataType:'json' ,success : function(d){
      if((typeof d) === 'string') d = JSON.parse(d);
      for(var mi=0;mi<d.methods.length;mi++){
        var m = d.methods[mi];
        $("#property_table_body").append(
          "<tr>"+
            "<th>" + m.epc + "</th>" +
            "<td>" + m.name + "</td>" +
            "<td>" + m.get + "/" + m.set + "</td>" +
            "<td>" + "---" + "</td>" +
            "</tr>");
      }
      onPropertyChangedOnDetail = function(prop_change){

      };
    }});
    $("#get_property_button").unbind("click");
    $("#get_property_button").click(function(){
      // get get property.
      kHAPI.get([nickname,"0x9f"],function(data){
        var table = kHAPI.propertyMapToProperties(data.property[0].value);
        var send = table.slice(0);
        send.unshift(nickname);
        kHAPI.get(send,function(data){
          for(var j=0;j<data.property.length;j++){
            var tr = $("#property_table tbody tr");
            for(var i=0;i<tr.length;i++){
              var cells = tr.eq(i).children();
              var epc = cells.eq(0).text();
              if(epc === data.property[j].name && data.property[j].value !== undefined){
                cells.eq(3).text(data.property[j].value.map(
                  function(x){
                    var s = x.toString(16);
                    while(s.length < 2) s = "0" + s;
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
  // Aircon http://www.survivingnjapan.com/2012/07/use-air-conditioner-japan.html
  "0x0130":[{epc:"0x80",edt:[0x30],text:"PowerOn"},
            {epc:"0x80",edt:[0x31],text:"PowerOff"},
            {epc:"0xB0",edt:[0x41],text:"Auto mode"},
            {epc:"0xB0",edt:[0x42],text:"Cool mode"},
            {epc:"0xB0",edt:[0x43],text:"Heat mode"},
            {epc:"0xB0",edt:[0x44],text:"Dehumidify mode"}
            ],

  // GeneralLighting
  "0x0290":[{epc:"0x80",edt:[0x30],text:"PowerOn"},
            {epc:"0x80",edt:[0x31],text:"PowerOff"}],
  // curtain
  "0x0262":[{epc:"0xe0",edt:[0x41],text:"Open"},
            {epc:"0xe0",edt:[0x42],text:"Close"}]
};
var makeDeviceRemocon = function(nickname){
  // helper function. prop is string,val is list.

  var makeButton = function(text,func){
    return "<input type='button' onclick='{func}' value='{text}' />"
      .format({func:func,text:text});
  };

  var makeECHONETLiteDeviceRemocon = function(device){
    var makeSetFunction = function(nickname,prop,val){
      return "kHAPI.set([\"{nickname}\",[\"{prop}\",[{val}]]]);"
           .format({nickname:nickname,prop:prop,val:val});
    };
    var makeSetButton = function(nickname,prop,val,text){
      return makeButton(text,makeSetFunction(nickname,prop,val));
    };
    var ret = "";
    if(device.deviceType in deviceRemocons){
      var re = deviceRemocons[device.deviceType];
      for(var i=0;i<re.length;i++){
        ret += makeSetButton(device.nickname,re[i].epc,re[i].edt,re[i].text);
      }
    }

    var makeSetButtonIN = function(nickname,prop,val,text){
      return makeButtonIN(text,makeSetFunction(nickname,prop,val));
    };
    if( device.deviceType === '0x0130' ){
	ret += 'Temperature<br>' ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[18],'18C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[19],'19C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[20],'20C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[21],'21C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[22],'22C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[23],'23C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[24],'24C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[25],'25C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[26],'26C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[27],'27C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[28],'28C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[29],'29C') ;
	ret += makeSetButtonIN(device.nickname,'0xb3',[30],'30C') ;
    }

    return ret;
  };


  var device = kHAPI.findDeviceByNickname(nickname);

    return makeECHONETLiteDeviceRemocon(device);
};

// arg is index of manifests
var onAppPageOpen = function(index){
  kHAPI.openAppPageByManifestIndex(index);
  return false;
};

var onAppSettingPageOpen = function(index){
  var manifest = manifests[index];
  var description = ("<img src='{image}'/>" +
                     "<font size=+4>{appname}</font><br>" +
                     "{subtitle} <br><hr>" +
                      "{descript}").format({
                       appname:escapeHTML(manifest.title),
                       subtitle:escapeHTML(manifest.subtitle),
                       descript:escapeHTML(manifest.description),
                      image:manifest.icon
                     });

  var matching_html = (manifest.devices.length>0?"<big>Associate devices for this app.</big><br>":"");
  var select_prefix = "device_choice";
  var selected_values = [];
  for(var i=0;i<manifest.devices.length;i++){
    var dev = manifest.devices[i];
    var select_name =  select_prefix + i;

    var template_p = "<label for={select_name} class='select'>{description}</label>"+
                      "<select name={select_name}>";
    var template_a = "</select>";
    var template_option = "<option value='{value}'>{text}</option>";

    var output = template_p.format({select_name:select_name,
                                    description:dev.description});

    var real_devices = kHAPI.getDevices();

    output += template_option.format({value:"none",text:"---"});

    var selected_value = "none";
    for(var j=0;j<real_devices.length;j++){
      var rv = real_devices[j];
      if(rv.deviceType === dev.deviceType && rv.protocol === dev.protocol && rv.active === true){
        output += template_option.format({value:rv.nickname,
                                          text:escapeHTML(rv.nickname)});
        if(rv.nickname === dev.assignedDevName){
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

  for(var i=0;i<manifest.devices.length;i++){
    var bind_i = i;
    // to bind environment.
    var changed_generator = function(manifest_index,index){
      return function(event,ui){
        // can overwrite?
        manifests[manifest_index].devices[index].assignedDevName = $(this).val();
        kHAPI.addManifest(manifests[manifest_index]);
      };};
    $("*[name=" + select_prefix + i + "]").bind( "change",changed_generator(index,i));
    $("*[name=" + select_prefix + i + "]").val(selected_values[i]).selectmenu("refresh");
  }
  $.mobile.changePage("#app_settings_page");
};

var onPropertyChangedOnDetail = function(prop_change){
};

var onClickChangeNickname = function(from,to){
  kHAPI.changeNickname([from,to]);
};

var onClickRemoveDevice = function(nickname){
  kHAPI.deleteDevice(nickname);
  kHAPI.reqDevListHandlers_onUpdateList() ;
};

var onClickFullInitializeButton = function(){
  areYouSure("Initialize server settings?",
             "This function will delete nickname settings,apps settings.",
             "OK",
             "Cancel",function(){
               kHAPI.fullInitialize();
             }) ;
  return false;
};

var toast = function(message){
  message = escapeHTML(message);
  var t = $("<div class='ui-loader ui-overlay-shadow " +
             "ui-body-a ui-corner-all'>" + message + "</div>")
        .css({display:"block",opacity:0.9,padding:"10px"});

  var left = ($(window).width() - t.width()) / 2;
  var top = $(window).height() - 100;

  t.css({top:top,left:left})
    .appendTo($.mobile.pageContainer).delay(1000)
    .fadeOut(400,function(){$(this).remove();});
};

var makeDeviceLi = function(device){
  var id = getNicknameHash(device.nickname);
  var template =
        "<li id='{id}'> " +
          "<a onclick='onDeviceIconClick(\"{nickname}\");return false;'> " +
            "<div class='padding-div'></div> " +
            "<img id='devIconImg{id}' src='{imageurl}' onload='queryDevStatusForIcon(\"{nickname}\");' \
                  onerror='this.src=\"index_res/icons/error.png\";'/>"+
            " <div id='statusDiv{id}' style='position:absolute; top:5px; left:5px;'> "+
            "{nickname}<br />"+
            "<img id='statusPowerImg{id}' src='index_res/icons/PowerInactive.png'/> "+
            "<span id='statusDivTxtArea{id}'></span> "+
            "</div>"+
            "<div id='devIconBottomTxt{id}'style='position:absolute;bottom:0px;right:5px;'></div>" +
          "</a>"+

        "<input type='button' value='Settings' class='jqmNone'" +
        "onclick='onDeviceDetailPageOpen(\"{nickname}\");' style='width:100%' ></input>"+
        "</li>";
  return template.format({id:id,nickname:escapeHTML(device.nickname),
                          imageurl:getImageUrl(device)}).removeAllNewline();
};

var manifests = [];
function refreshManifests(manifs){
  if(!app_page_has_created) return ;
  manifests = manifs ;
  var app_list_view = $("#app_list_view");
  app_list_view.empty();


  var template = "<li id='{id}'>"
                  +"<a href='#app_iframe_page' onclick='return onAppPageOpen({index});'>"
                   + "<div class='padding-div'></div>"
                   + "<img src='{image}' /> "
                   + "<div style='position:absolute; top:0px; left:0px;'>{title}</div>"
                  +"</a>"
                  +"<input type='button' value='Settings' class='jqmNone'" +
                           "onclick='onAppSettingPageOpen({index})' style='width:100%' ></input>"
                 +"</li>";

  for( var ai=0;ai<manifs.length;++ai )
    app_list_view.append(
      template.format({
        id:getNicknameHash(manifs[ai].title)
        ,index:ai
        ,image:manifs[ai].icon
        ,title:escapeHTML(manifs[ai].title)
      }).removeAllNewline()
    );

  try{
    app_list_view.listview("refresh") ;
    app_list_view.trigger("create");
  }catch(e){ /* alert(e) ; */ }
}

// utility
var isECHONETLite = function(device){
  return device.protocol === "ECHONET Lite";
};
var isControler = function(device){
  return isECHONETLite(device) && parseInt(device.deviceType) == 0x5ff;
};

var getImageUrl = function(device){
  var imgurl = 'index_res/icons/' + device.deviceType +'.png' ;
  var bStatusRequested = false ;

  if( device.protocol === 'ECHONET Lite' ){
    if( device.deviceType === '0x0011' || device.deviceType === '0x0012'
        || device.deviceType === '0x0262'|| device.deviceType === '0x0290' ){
      imgurl = 'index_res/icons/' + device.deviceType.substring(0,6) + ".png";
    } else {
      imgurl = 'index_res/icons/' + device.deviceType.substring(0,4) + ".png";
    }
  }
  return imgurl ;
};

function queryDevStatusForIcon( nickname , varHash){
  var d ;
  if( typeof nickname === 'string' ){
    d = kHAPI.findDeviceByNickname(nickname) ;
  } else {
    d = nickname ;
    nickname = d.nickname ;
  }
  var id = getNicknameHash(nickname);
  if(d.isEmulation===true) {
    $('#devIconBottomTxt'+id).html('Emulation') ;
  }

  function setIcon( propName , cbFunc ){
    if( typeof varHash === 'object' && varHash[propName] !== undefined )
      cbFunc( varHash[propName] ) ;
    else
      kHAPI.get( [nickname,propName] , function(ret,success){
        if( !success || ret.property[0].value === undefined ) return ;
        cbFunc(ret.property[0].value) ;
      } ) ;
  } ;

  if( d.protocol === 'ECHONET Lite' ){
    var bCheckPower = true ;
    var dtNum = parseInt(d.deviceType) ;
    // PowerDistributionBoardMetering
    if( dtNum == 0x0287 ){
      // Instaneous watts
      if(d.isEmulation !== true){
        setIcon('0xc6',function(newval){
          var watts = echoByteArrayToInt(newval) ;
          $('#devIconBottomTxt'+id).html(watts+'W') ;
        }) ;
      }
    } else if( dtNum == 0x0130 ){   // Aircon
      // Temperature
      setIcon( '0xb3',function(newval){
        var cTS = newval[0] ;
        if( cTS >= 0x80 ) cTS = cTS-0x100 ;
        $('#statusDivTxtArea'+id).html('  '+cTS+' deg C') ;
      }) ;

      if( d.isEmulation !== true && typeof d.powersensor === 'string' ){
        var _idx = d.powersensor.indexOf('_') ;
        if( _idx != -1 ){

          var ts = [ d.powersensor.substring(0,_idx) , d.powersensor.substring(_idx+1) ] ;
          var powerDev = kHAPI.findDeviceByNickname(ts[1]) ;
          var chid = parseInt(ts[0].substring(1))-1 ;
          if(  powerDev !== undefined && powerDev.isEmulation !== true
               && chid >= 0 && chid < 32 )
            kHAPI.get( [ts[1],'0x'+(0xd0+chid).toString(16) ] , function(ret,success){
              if( !success ) return ;
              if( ret.property[0].value !== undefined ){
                // R phase only // (0.1A * 100V)
                var watts = echoByteArrayToInt([ret.property[0].value[4],ret.property[0].value[5]]) * 10 ;
                $('#devIconBottomTxt'+id).html(watts+'W') ;
              }
            } ) ;
        }
      }
    } else if( dtNum == 0x0011 ){   // Temperature
      // Temperature
      setIcon('0xe0',function(newval){
        var temp = getTempFromECHONETLite2ByteArray(newval) ;
        $('#statusDivTxtArea'+id).html('  '+temp+' deg C') ;
      }) ;
    } else if( dtNum == 0x0012 ){   // Humidity
      // Humidity
      setIcon('0xe0',function(newval){
        $('#statusDivTxtArea'+id).html('  '+newval[0]+' %') ;
      }) ;
    } else if( (0 < dtNum && dtNum <= 0x000b)              // Gas sensor to Air polution sensor
               || ( 0x000e <= dtNum && dtNum <= 0x0010)    // Sound, Posting, Weight
               || ( 0x0013 <= dtNum && dtNum <= 0x001a)    // Rain to Smoke
               || ( 0x001c <= dtNum && dtNum <= 0x001d)    // Gas, VOC
               || ( 0x0020 <= dtNum && dtNum <= 0x0021)    // Smell, Fire
               || ( 0x0026 == dtNum )                      // Small movement
               || ( 0x0028 <= dtNum && dtNum <= 0x0029)    // Floor, Openclose
               || ( 0x002c == dtNum )                      // Snow
             ){
               setIcon('0xb1',function(newval){
                 $('#statusDivTxtArea'+id).html('  '+(newval[0]==0x41?'<font color="#c00">ON<font>':'OFF')) ;
               }) ;
             } else if( dtNum == 0x0262 ){   // Curtain
               // Curtain open/close
               setIcon('0xe0',function(newval){
                   $('#devIconImg'+id)[0].onload = undefined ;

                   if( newval[0] == 0x41 ) // Open
                     $('#devIconImg'+id)[0].src = 'index_res/icons/0x0262-open.png' ;
                   else if( newval[0] == 0x42 ) // Closed
                     $('#devIconImg'+id)[0].src = 'index_res/icons/0x0262.png' ;
               }) ;

             } else if( dtNum == 0x0290 ){   // Light
               // Power
               setIcon('0x80',function(newval){
                   $('#devIconImg'+id)[0].onload = undefined ;
                   if( newval[0] == 48 ){
                     $('#statusPowerImg'+id)[0].src = 'index_res/icons/PowerOn.png' ;
                     $('#devIconImg'+id)[0].src = 'index_res/icons/0x0290-on.png' ;
                   } else if( newval[0] == 49 ){
                     $('#statusPowerImg'+id)[0].src = 'index_res/icons/PowerOff.png' ;
                     $('#devIconImg'+id)[0].src = 'index_res/icons/0x0290.png' ;
                   }
               }) ;

               bCheckPower = false ;
             }

    if(bCheckPower){
      // Power
      setIcon('0x80',function(newval){
          if( newval[0] == 48 )
            $('#statusPowerImg'+id)[0].src = 'index_res/icons/PowerOn.png' ;
          else if( newval[0] == 49 )
            $('#statusPowerImg'+id)[0].src = 'index_res/icons/PowerOff.png' ;
      }) ;
    }


	}
} ;

var onDeviceIconClick = function(nickname){
  var d = kHAPI.findDeviceByNickname(nickname);
  // if null is substituted to handler, the power is changed.
  // if this value remains false, refresh device info.

  var bTogglePower = false ;
  var newVal ;
  if( d.protocol === 'ECHONET Lite' ){
    if( d.deviceType === '0x0262' ){// Curtain
      // Curtain open/close
      kHAPI.get([nickname,'0xe0'],function(ret,success){
        if(success == true && ret.property[0].value !== undefined)
          newVal = [ret.property[0].value[0] == 0x41?0x42:0x41] ;
        else newVal = [0x41] ;

        kHAPI.set([nickname,['0xe0',newVal]],
              function(){
                queryDevStatusForIcon(d,{'0xe0':newVal}) ;
              }) ;
      }) ;
      return ;
    } else if(d.deviceType === '0x0130'){     // Aircon
      bTogglePower = true ;
    } else if(d.deviceType === '0x0290'){     // Light
      bTogglePower = true ;
    }
  }

  if( bTogglePower ){
    // Power
    kHAPI.get([nickname,'0x80'],function(ret,success){
      if(success == true && ret.property[0].value !== undefined )
        newVal = [ret.property[0].value[0] == 48?49:48] ;
      else newVal = [48] ;

      kHAPI.set([nickname,['0x80',newVal]]
                ,function(){
                  queryDevStatusForIcon(d,{'0x80':newVal}) ;
                });
    });
  } else{
    queryDevStatusForIcon(d) ;
    //toast('No function is assigned.') ;
  }
};

var areYouSure = function(big,small,yestext,notext,yescallback,nocallback){
  $("#sure .big-message").text(escapeHTML(big));
  $("#sure .small-message").text(escapeHTML(small));
  $("#sure .sure-do .ui-btn-text").text(escapeHTML(yestext));
  $("#sure .no .ui-btn-text").text(escapeHTML(notext));
  $("#sure .sure-do").unbind("click");
  $("#sure .no").unbind("click");

  $("#sure .sure-do").click(function(){
    if(yescallback !== undefined){
      yescallback();
    }
    $(this).off("click.sure");
  });
  $("#sure .no").click(function(){
    if(nocallback !== undefined){
      nocallback();
    }
    $(this).off("click.no");
  });
  $.mobile.changePage("#sure",{transition:"slidedown"});
};

//ID and NAME tokens must begin with a letter ([A-Za-z])
//     and may be followed by any number of letters,
// digits ([0-9]), hyphens ("-"), underscores ("_"), colons (":"), and periods (".").
// http://www.w3.org/TR/REC-html40/types.html#type-name
// use rolling hash.
var getNicknameHash = function(nickname){
  var prefix = "dev_";
  // Math.pow(2,64).
  var mod = 18446744073709552000;
  var b = 1000000007;
  var r = 0;
  for(var i=0;i<nickname.length;i++){
    r = r * b + nickname.charCodeAt(i);
    r = r % mod;
  }
  return prefix + r;
};

var getUNIXTime = function(date){
  if(date === undefined) return new Date() / 1;
  else return date / 1;
};

var UNIXTimeToDate = function(unixtime){
  return new Date(unixtime);
};

var getToday = function(date){
  if(date === undefined) date = new Date();
  date.setHours(0);
  date.setMinutes(0);
  date.setSeconds(0);
  return date;
};

var getHour = function(data,hours){
  if(data === undefined) data = new Date();
  if(hours === undefined) hours = -1;
  return new Date(getUNIXTime(data)+3600000*hours);
};

// http://stackoverflow.com/questions/6020714/escape-html-using-jquery
var escapeHTML = function(string){
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

  return ('' + string)
	.split('<br>').join('%%_BR_%%')
	.replace(htmlEscaper, function(match) { return htmlEscapes[match]; })
	.split('%%_BR_%%').join('<br>')
	;
};
