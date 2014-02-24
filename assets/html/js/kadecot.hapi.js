/*
 kadecotのホーム画面を作るためのAPI。index.html内でjquery,jquerymobileの次に読み込まなければならない。

 ホーム画面UIの開発においては、基本的にkHAPIに直接含まれるメソッドのみを用い、それ以外の構成要素、
 例えばkHAPI.netなどのメンバーは直接呼び出すことはないものとする。(ラップしたメソッドがkHAPI内に
 含まれるのでそこから間接的に呼び出すものとする。ちなみに、ラップメソッドはその呼び出し先のクラス名
 は指定しなくてよいこととしている。メソッドの名前から、どのクラスに属しているかを判断する）

 kHAPI.init()の引数が初期化終了時に呼び出されるコールバック関数であることと、kHAPI.isOnAndroidが
 そのまんま真理値であることを除外すれば、kHAPIのメソッドの引数はすべて同一である。

 kHAPI.メソッド名(引数を表す配列,callbackfunction);

 となる。一つ目の引数は、配列でない場合はそれを要素として一つだけ含む配列として解釈される。
 例：kHAPI.connectServer('localhost');はkHAPI.connectServer(['localhost']);と同じ意味になる

 引数がすべて使われるわけではない。例えば、kHAPI.log()は、ログのメッセージとなる文字列のみ
 を引数にとり、istrustedとか、結果を受け取るためのコールバックは必要ない（引数に与えても無視される）

*/

function l(val,msg){ console.log((msg===undefined?'none':msg)+' : '+JSON.stringify(val)); } ;

var kHAPI = {
	APIVer : '1'
	, isOnAndroid : ( 'ServerCall' in window && 'UserApp' in window )
	, init : function(oncomplete_func){
		// Executed after al includes are finished
		function init_real(){
			///////////////////////////////////////
			// Setup APIs (mostly wrap functions)
			// called as kHAPI.methodName(args,function(re){})
			///////////////////////////////////////
			var wrapFuncs = [
				'setServerLocation'
				,'refreshList'
				,'changeNickname'
				,'getDistinctNoun'
				,'deleteDevice'
				,'deleteInactiveDevices'
				,'enablePersistentMode'
				,'enableJSONPServer'
				,'enableSnapServer'
				,'queryLog'
			] ;

			// This refreshes everything. the call number should be minimized.
			kHAPI.reqDevListHandlers_onUpdateList = function(){
				kHAPI.devListHandlers.onUpdateList( kHAPI.dev.devices ) ;
			} ;

			kHAPI.findDeviceByNickname = function(nickname){
				return kHAPI.dev.findDeviceByNickname(nickname) ;
			} ;
          // Get device status
          // args = [nickname,prop1,prop2]
          // Success result example: callback(
          // { "nickname" :	 "hoge",
          //   "property" : [{"name" : "0x80", "value" : [0x41, 0x31],
          //   "success" : true},{...},{...}]
          // , true ) ;
          // Fail result example: callback(
          // { "data" : "nickname not found"
          // ,"message" : "Invalid code""
          // ,"code" : -32602 } , false ) ;
          // this function will wait for 100 msec for new get.
          // deviceAccessQueue["nickname"] = undefined (if there is not previous gets)
          // deviceAccessQueue["nickname"] = [{props:[prop1_1,prop1_2,,,],callback:function(){}},
          //                                  {props:[prop2_1,prop2_2,,,],callback:function(){}},]
          var deviceAccessQueue = Object.create(null);
          var waitingTimeForQueue = 100; // ms
          // kHAPI.get_with_queue = function( args, callback ){
          kHAPI.get = function( args, callback, with_out_queue){
            if(with_out_queue === undefined) with_out_queue = false;
            if(with_out_queue){
              this.net.callServerFunc('get' ,args ,callback);
              return;
            }
            var nickname = args[0];
            var props = args.slice(1);
            if(deviceAccessQueue[nickname] === undefined){
              deviceAccessQueue[nickname] = [{props:props,callback:callback}];
              setTimeout(function(){
                var props_and_callbacks = deviceAccessQueue[nickname];
                deviceAccessQueue[nickname] = undefined;
                var send_args = [nickname];
                // like {{f:1,t:2},{f:2,t:4}}.this is for slice.
                //  {f:1,t:3} means [1,3)
                var start_and_end_index = [];
                var callbacks = [];
                for(var i=0;i<props_and_callbacks.length;i++){
                  // equal to send_args = send_args.concat(props_and_callbacks[i].props);
                  //  but this is bang method.
                  send_args.push.apply(send_args,props_and_callbacks[i].props);
                  callbacks.push(props_and_callbacks[i].callback);
                  start_and_end_index.push({f:send_args.length-1-props_and_callbacks[i].props.length,
                                            t:send_args.length-1});
                }
                kHAPI.net.callServerFunc("get",send_args,function(recv_args,success){
                  for(var i=0;i<callbacks.length;i++){
                    if(callbacks[i] !== undefined){
                      if(!success){
                        callbacks[i](recv_args,false);
                      }else{
                        callbacks[i]({nickname:nickname,
                                      property:recv_args.property.slice(start_and_end_index[i].f,
                                                                        start_and_end_index[i].t)
                                     },true);
                      }
                    }
                  }
                });
              },waitingTimeForQueue);
            }else{
              deviceAccessQueue[nickname].push({props:props,callback:callback});
            }
          };
			// set : Change device states. Can modify multiple properties at once.
			// args = [nickname,[prop1,newval1],[prop2,newval2] ..],
			// result is the same as get.
			kHAPI.set = function( args , callback ){
				this.net.callServerFunc('set' , args , callback ) ;
			} ;

			kHAPI.readManifests = function(callback){
				this.app.readManifests(callback) ;
			} ;
            kHAPI.addManifest = this.app.addManifest;
			kHAPI.openAppPageByManifestIndex = function(index){
				this.app.openAppPageByManifestIndex(index);
			} ;
			kHAPI.getNetInfo = function(){
				return kHAPI.net.info ;
			} ;
			kHAPI.getDevices = function(){
				return kHAPI.dev.devices ;
			} ;
			if( kHAPI.isOnAndroid ){
				/////////////////////////
				// Android mode only methods
				/////////////////////////
				kHAPI.onBackBtn = function(){
					if( typeof kHAPI.app.running.cleanup === 'function' )
						kHAPI.app.running.cleanup(true) ;
					else
						UserApp.closeAppView() ;
				} ;
				wrapFuncs = wrapFuncs.concat(
				  ['fullInitialize',
				   'enableWebSocketServer'
				   ,'enableServerNetwork'
				  ]) ;
			} else {
				/////////////////////////
				// Browser mode only methods
				/////////////////////////
				kHAPI.connectServer = function(ipaddr){
					this.net.WS.connectServer(ipaddr);
				} ;
				kHAPI.disconnectServer = function(){
					if( !this.isServerConnected() ) this.onServerDisconnected() ;
					else this.net.WS.disconnectServer() ;
				} ;
				kHAPI.isServerConnected = function(){
					return this.net.WS.serverConnection !== undefined ;
				} ;
			}

			// console.log(wrapFuncs);
			for( var i=0 ; i < wrapFuncs.length ; ++i ){
				var mtd = wrapFuncs[i] ;
				(function(){
					var _mtd = mtd ;
					kHAPI[mtd] = function(args,callback){
						this.net.callServerFunc( _mtd , args , callback ) ;
					} ;
				})() ;
			}

			kHAPI.local.init() ;
			if( !kHAPI.isOnAndroid )
				Local = kHAPI.local ;

			kHAPI.net.init() ;
			kHAPI.dev.init() ;
			kHAPI.app.init() ;

			kHAPI.addManifest = kHAPI.app.addManifest;

			if( kHAPI.isOnAndroid ){
				// After this,notificatoin will come from server.
				ServerCall.onPageLoadFinished();
			}
			if( typeof oncomplete_func === 'function' ) {
				oncomplete_func() ;
			}
		} ;

		init_real.call(kHAPI);

		/*
		// Pre-include js required files.
		var incfiles = [ 'net','dev','app','local'] ;
		var incfilenum = incfiles.length ;
		$.ajaxSetup({async: false});
		for( var i=0;i<incfiles.length;++i ){
			$.getScript('js/kadecot.hapi.'+incfiles[i] + '.js'
			,function(){ if(--incfilenum === 0 ){init_real.call(kHAPI); }} );
		}
		$.ajaxSetup({async: true});
		*/
	},
	// http://www.echonet.gr.jp/spec/pdf_spec_app_c/SpecAppendixC.pdf
	//	tested with example.Return value is sorted.
	//	 remember,0x9E is getgetPropertyMap,0x9F is getsetPropertyMap
	propertyMapToProperties : function(map){
		if(map === null || map === undefined || map.length == 0) return [];
		var len = map[0];
		var ret = [];
		if(len < 16){
			for(var i=1;i<map.length;i++){
				ret.push(map[i]);
			}
		}else{
			for(var high=8;high<16;high++){
				for(var low=0;low<16;low++){
					if((map[low+1] >> (high-8)) & 1){
						ret.push((high << 4) + low);
					}
				}
			}
		}
		return ret;
	}
	, isConnected : function(){
		return this.isOnAndroid || (kHAPI.net.WS.serverConnection !== undefined);
	}
	////////////////////////////////////////
	// User-defined callbacks
	////////////////////////////////////////
	, onServerConnected : function(){
		var netInfo = kHAPI.net.info ;
		//console.log('Server connected : '+JSON.stringify(devs)) ;
	}
	, onServerConnectionFailed : function() {
		// called when ipaddress is incorrect.
		console.log('Server connection failed.');
	}
	, onServerDisconnected : function(){
		console.log('Server disconnected') ;
	}
	, onPropertyChanged : function( arg ){
		// Propertyの変更通知
	}
	, onNotifyServerSettings : function( arg ){
		// arg.network,arg.location,arg.jsonp,arg.websocket,arg.persistence
		//	 network : {"isConnected" : true , "type" : "wifi", "SSID" : "aaa",
		//				  "ip" : "1.1.1.1", "isDeviceAccessible" : true}  // network
		//	 location : []
		//	 jsonp : true
		//	 websocket: true
		//	 persistence: true // persistence
		//	 snap : true
	}

	, devListHandlers : {
		onUpdateList : function(newlist){
		}
		,onDeviceFound : function( newdevice , newlist ){
			kHAPI.devListHandlers.onUpdateList(newlist) ;
		}
		,onDeviceActivated : function( newdevice , newlist ){
			kHAPI.devListHandlers.onUpdateList(newlist) ;
		}
		,onDeviceDeleted : function( delNickname , newlist ){
			kHAPI.devListHandlers.onUpdateList(newlist) ;
		}
		,onNicknameChanged : function( oldnickname , newnickname , newlist ){
			kHAPI.devListHandlers.onUpdateList(newlist) ;
		}
	}
} ;
