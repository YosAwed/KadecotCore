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
				,'queryLog'
			] ;

			// kHAPI.onDevListUpdated() is called
			//	when updated with new devices as the argument
			kHAPI.updateDevList = function(args){
				this.net.callServerFunc( 'list',undefined,function(re){
					kHAPI.onDevListUpdated(
						kHAPI.dev.setUpdatedDevices( re,true )
					) ;
				} ) ;
			} ;
			kHAPI.findDeviceByNickname = function(nickname){
				return kHAPI.dev.findDeviceByNickname(nickname) ;
			} ;
			// Get device status
			// args = [nickname,prop1,prop2]
			// Success result example: callback(
			//		  { "nickname" :  "hoge"
			//			 , "property" : [
			//			 {"name" : "0x80", "value" : [0x41, 0x31],
			//				  "success" : true}
			//			,{...} , {...}
			//			  ]
			//			} , true ) ;
			// Fail result example: callback(
			//		{ "data" :	"nickname not found"
			//		  ,"message" : "Invalid code""
			//		  ,"code" : -32602 } , false ) ;
			kHAPI.get = function( args , callback ){
				this.net.callServerFunc('get' ,args ,callback);
			} ;

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

			//kHAPI.local.init() ;
			kHAPI.net.init() ;
			kHAPI.dev.init() ;
			kHAPI.app.init() ;

			if( kHAPI.isOnAndroid ){
				// After this,notificatoin will come from server.
				ServerCall.onPageLoadFinished();
			}
			if( typeof oncomplete_func === 'function' ) {
				oncomplete_func() ;
			}
		} ;

		// Pre-include js required files.
		var incfiles = [ 'net','dev','app','local'] ;
		var incfilenum = incfiles.length ;
		$.ajaxSetup({async: false});
		for( var i=0;i<incfiles.length;++i ){
			$.getScript('js/kadecot.hapi.'+incfiles[i] + '.js'
			,function(){ if(--incfilenum === 0 ) init_real.call(kHAPI); } );
		}
		$.ajaxSetup({async: true});
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
	, onDevListUpdated : function(devices){
		// var devs = kHAPI.dev.devices ;
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
	}
} ;
