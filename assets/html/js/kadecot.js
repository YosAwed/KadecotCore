/*
 * kadecot.jsは宅内Androidで動作するKadecotホームサーバと通信するためのライブラリです。
 * 
 * 使い方はまず、このファイルをscriptタグで読みこんでください。以下、この読み込み元となる
 * Webアプリのことを「クライアントアプリ」と呼ぶことにします。クライアントアプリには一つ
 * 大きな制約があります。それは、他フレームやウィンドウからPostMessageを受け取る
 * EventListenerを使ってはいけないということがあります。このEventListerは、クライアント
 * アプリとkadecotマイページとの通信に占有されます。
 * 
 * さて、kadecot.jsを読み込んだら、適当なところでkadecot.init()を呼び出してください。
 * (kadecotはkadecot.jsの中で定義されるグローバル変数です）
 * init()を呼び出すと、Kadecotのマイページが開いていればそれと通信を確立し、閉じていれば
 * それを開いてから通信を確立します。この時、過去に当該クライアントアプリを
 * マイページに接続したことがなければ、ユーザーがマイページ上でアプリ認証を行う必要が あります。
 * 
 * kadecot.initは二つの引数を取ります。どちらも省略不可です。
 * 一つ目はmanifestオブジェクトです。manifestオブジェクトはクライアントアプリが
 * ホームサーバーに対して要求する機能の定義や、kadecot内に表示されるアプリの 説明文などを設定するものです。
 * 二つ目の引数は初期化完了時に呼ばれるコールバック関数です。このコールバックが呼ばれる
 * ときには引数としてデバイスにアクセスするためのオブジェクト（アクセスオブジェクト）が
 * 一つ渡されてきます。このオブジェクトの内容については、manifestオブジェクトの仕様を説明した 上で説明します。
 * 
 * ○manifestオブジェクト
 * 
 * kadecot.init()の最初の引数として与えられるmanifestオブジェクトは以下の情報を含んでいます。 +クライアントアプリそのものの説明
 * +クライアントアプリがアクセスする機器の種類と、その中で関心があるプロパティ（機能名）
 * の情報。さらに、そのプロパティの値をどういう名前の変数でアクセスするか、何秒周期で アップデートするかの情報。
 * 
 * 例えば、次のようになります。
 * 
 * {"title":"かっこいいアプリ" ,"subtitle":"かっこよさに気を失いそうなアプリです。"
 * ,"description":"かっこよさとは何かについて、まじめに考えてみたアプリです。" ,"icon":"./icon.png"
 * ,"devices":[ { "name" : "Aircon", "protocol" : "echonetlite", "deviceType" :
 * "0x0130", "description":"エアコン" ,"access":{
 * "0x80":{"name":"Power","polling":15} ,"0xb0":{"name":"Mode","polling":16}
 * ,"0xb3":{"name":"Temp","polling":17} } } ,{ "name" : "Light", "protocol" :
 * "echonetlite", "deviceType" : "0x0290", "description":"室内の照明"
 * ,"access":{"0x80":{"name":"Power","polling":10}} } ] }
 * 
 * ※日本語で与える場合は、文字コードがutf8nであることをご確認ください。
 * 
 * 各メンバーについて説明します。
 * 
 * title : アプリのタイトルです。特に制限はありませんが、アプリアイコンの下に表示 される関係で、8文字以内推奨です。全てのタグは削除されます。
 * subtitle : アプリを一行で説明する文です。こちらは、アイコンを選択状態にした時にトーストで 表示される文です。全てのタグは削除されます。
 * description : アプリの設定画面を開いた時に表示されるもので、こちらは長さに制限はありません。 <image>以外のタグは削除されます。
 * 
 * icon : アイコン画像のURLです。64x64にしてください。絶対パスでもいいし、相対パスの場合は
 * このクライアントアプリがあるURLからの相対になります
 * 
 * devices : このアプリが操作対象とする機器を表しています。上の例では、エアコンと室内の 照明を操作対象にしていることがわかります。
 * また、各デバイス内のaccessというオブジェクト、これはデバイスごとに最低一つは
 * 必須のもので、機器のプロパティを変数名に割り当てます。細かい内容は次項で説明します。
 * 
 * 
 * ○初期化後のコールバックとアクセスオブジェクト
 * 
 * kadecot.init()の二つ目の引数は、kadecotの初期化が完了した後に呼び出されるコールバック関数
 * となります。省略できません。初期化の完了とは、クライアントアプリとホームサーバ間の接続確立だけ
 * でなく、宅内に存在する実機器との対応づけや、次に説明するアクセスオブジェクトの構築なども含まれます。
 * はじめて接続するクライアントアプリの場合は、ユーザーによるアプリの承認なども含まれますので、
 * 一般的に初期化完了コールバックが呼ばれるのはかなり後になります。
 * 
 * このコールバック関数は一つだけ引数を持っています。この引数は、アクセスオブジェクトと呼び、
 * manifestオブジェクトに基づいて作られた機器アクセス用の変数や関数を含んでいます。
 * 
 * 以下の説明では、このアクセスオブジェクトをaoと名付けておきます。
 * 
 * 前項でのmanifestオブジェクト例では、devicesの二つ目の機器は照明で、accessとして
 * "0x80"というメンバを含んでいました。その部分を抜き出してみます。 ,{ "name" : "Light", "protocol" :
 * "echonetlite", "deviceType" : "0x0290", "description":"室内の照明"
 * ,"access":{"0x80":{"name":"Power","polling":10}} }
 * 
 * これは、アクセスオブジェクトaoの中に、"Light"という名前の照明を表すオブジェクトが作られ、その中に
 * Powerという変数が作られ、これに照明の"0x80"というプロパティを紐付けることを意味しています。
 * こうしておくと、この変数のへの参照と代入を用いて、機器制御を行うことができます。この機器操作のための
 * 変数を、アクセス変数と呼ぶことにします。アクセス変数は例えば
 * 
 * ao.Light.Power = [0x30] ;
 * 
 * とすることで、照明をONすることができます。 0x30というのは、プロトコル上規定された値です。ECHONET
 * Liteでは、EDTと呼ばれている16進数に なります。
 * 
 * では、具体的に今回ao.Lightの中にどのようなものが作られているのか見てみましょう。
 * 
 * ao.Light = { active : value // true | false | undefined 現在の機器の状態。代入不可。
 * 
 * ,Power : value // 電源状態の参照や代入のための変数。ただし、参照の場合、その値はポーリング等 // によって得られた最後の値。
 * 
 * ,setPower : function(newval, onsuccessfunc, bCallChangePower) //
 * Powerへの代入と同じく、電源の値をセットするための関数だが、成功時の // コールバック関数を設定できる。一つ目の引数としてセットする値、二つ目として //
 * セットに成功したときに呼ばれるコールバック関数、三つ目として、この関数の //
 * 結果値が変化したときに、onChangePower(後述)を呼び出すかどうかの真理値 // (省略可。default=false)
 * 
 * 
 * ,getPower : function(onsuccessfunc, bCallChangePower) //
 * 現在のリアルタイムの電源の値をサーバーから得るための関数。 // 引数として、得られた後のコールバック関数、この関数の結果値が //
 * 変化したときに、onChangePower(後述)を呼び出すかどうかの真理値 // (省略可。default=false) } ;
 * 
 * LightオブジェクトにはonChangePowerという関数を追加することもできます。こうしておくと、Powerの値が
 * 変化したときに自動的にその関数が呼ばれるようになります。setPowerやgetPowerでも値が変化するので、
 * その時にこのonChangePowerを呼ぶかどうかはsetPower/getPowerの呼び出し時に、最後の引数として真理値で
 * 設定しておきます。Powerへの代入の場合はonChangePowerは呼ばれません。
 * 
 * このように、Lightの中にはPowerというプロパティだけでなく、activeという変数と、(set|get)Powerという関数が 追加されています。
 * 
 * activeとは、現在その機器にアクセスできるかどうかを示します。この値がtrueでなければ、機器情報を取得したり
 * 設定することはできません。実際に使う前にチェックしておくことをおすすめします。
 * falseの時は、過去に接続できたことはあるが今は返答が返ってこないことを示し、undefinedの
 * 時は、機器がサーバのデータベースに載っていない（消去された）ことを示します。
 * 
 * Powerはアクセス変数で、先程説明したように機器のプロパティに紐付けられており、代入や参照によって機器の情報の
 * 設定や取得ができます。ただし、アクセス変数の使い方にはいくつか留意して頂きたいことがあります。 1.
 * アクセス変数に代入しても、その直後に参照して得られる値は古いままです。なぜかというと、代入により
 * セットされる制御情報がネットを通じて機器に伝達されるまでには時間がかかりますし、途中で何かエラーが起こる
 * 可能性もあるからです。成功したときにのみ、参照で得られる値の更新がされます。 2.
 * アクセス変数は、基本的にはポーリングによって値が更新されるので、必ずしも実際の機器の最新の状態と一致
 * しないことがあります（ポーリング周期は、manifestオブジェクトのpollingプロパティで設定します（秒単位）。
 * ただし、プロトコル上値が変化したことが自動的に通知されるような場合に限り、これが常に最新の値であることを 信じることができます。（ECHONET
 * Liteで状変通知が必須となっているプロパティはそうなります） 3.
 * センサーの値など、取得はできても代入はできないプロパティもあります。この場合はいくら代入しても値が 変化することはありません。
 * 
 * getPowerというメンバは、各アクセス変数に対して作られる関数プロパティで、この関数を呼び出すことにより、
 * ポーリングに頼らず現在の値を得ることができます。ただし、通信には一般に時間がかかるため、引数としてコールバック
 * 関数を与えておくと、値が取得できたタイミングでその関数が呼ばれます。もちろんこの時点で、Powerプロパティ参照に
 * より得られる値も新しい値になっているはずです。
 * 
 * setPowerというメンバも各アクセス変数に対して自動的に作られる関数プロパティです。
 * 機器制御はPowerへの代入により行うことができますが、Powerの値は実際のデバイスの状態変化後に値が変化するので、
 * そのタイミングをはかりたいこともあると思います。そのようなときにはPowerへの代入ではなくsetPowerを呼び出して、
 * 引数にコールバック関数を指定しておけば、実際の値の設定コマンドの結果タイミングがわかります。
 * 
 * LightオブジェクトにonChangePowerという関数を追加すると、Powerの値が変化した時点で呼び出されるコールバックに
 * なります。例えば、赤外線リモコンなどを用いて電源が変更された場合など、それがネットワークに通知されれば呼び
 * 出されます。アクセス変数の値が変更されるのは、以下の4通りのケースがあります。 1. ポーリングの結果変化していた時 2. 状変通知があった時 3.
 * getPower呼び出しにより最新の値を取得したら前と違う値だったとき 4. setPower呼び出しにより、プログラムから値を変更したとき
 * このうち、3.と4.については、(get|set)Powerを呼び出したのはクライアントアプリそのものなので、わざわざコール
 * バックを使わなくてもアプリは値の変更を知っていると考えられます。そこで、3.と4.の場合にはデフォルトでは
 * onChangePowerがあってもそれは呼ばないようになっています。この場合も呼ばせるには、getPower,setPowerの呼び出し
 * の時に、最後の引数としてtrueを与えてください。
 * 
 * 
 * 
 * ここまで見てきたように、manifestオブジェクトのdevicesに含まれる機器はそれぞれnameメンバの名前を持った
 * 機器オブジェクトが作られ、さらにaccessを設定しておくと、そのnameメンバの名前を持ったアクセス変数が作られ、 get*,
 * set*、それに、必要に応じてonChange*というコールバックを設定できることがわかりました。
 * 
 * 
 * さらにもう一つ、アプリが終了したことを通知するstop()という関数があります。
 * これは、ポーリングの終了、その他クリーンナップを行います。もう機器操作を使わないときに 引数なしで呼び出すとその後のパフォーマンスが向上します。
 * また、引数にtrueを入れると、アプリウィンドウが閉じる処理を行います。アプリのonunloadの中などで強制的に
 * 引数trueで呼び出していただけると助かります。絶対呼び出さなければいけないというものでもありませんが、 終了後の無駄なパフォーマンス低下を抑制できます。
 * 一度終了した後の再開は再度kadecot.initを呼び出すことにより可能…（だと思われますが、あまりちゃんとチェック していません）
 * 
 * 
 * エアコンも含めると、この例でのアクセスオブジェクトの中身全体はこうなります。
 * 
 * ao = { Aircon : { active : boolean
 * 
 * ,Power : object ,getPower: function(onsuccessfunc, bCallChangePower)
 * ,setPower: function(newval, onsuccessfunc, bCallChangePower)
 * 
 * ,Mode : object ,getMode : function(onsuccessfunc, bCallChangePower) ,setMode :
 * function(newval, onsuccessfunc, bCallChangePower)
 * 
 * ,Temp : object ,getTemp : function(onsuccessfunc, bCallChangePower) ,setTemp :
 * function(newval, onsuccessfunc, bCallChangePower) } ,Light : { active :
 * boolean
 * 
 * ,Power : object ,getPower: function(onsuccessfunc, bCallChangePower)
 * ,setPower: function(newval, onsuccessfunc, bCallChangePower) } , stop :
 * function }
 * 
 * 
 * 説明はここまでになります。 今回説明しきれなかったものとして、manifestのaccessの各プロパティ定義中に書ける
 * optionという設定があります。これは、例えばECHONET Liteの温度センサーは2バイトの
 * 配列で温度を表現していますが、これを浮動小数値一つで扱えるようにするためのもの
 * です。その他、プロトコルで流れてくる値そのままを使うとわかりづらいものを、より 直観に即した操作ができるように変換します。
 * そのうちきちんと説明します。ごめんなさい。
 */

var kadecot = {

  init : function( manif , initcb ){ 
    return this._wa.init( manif,initcb );
  }

  ,
_wa : {
    init : function( manif ,init_callback ){
      if (this.isInited !== undefined) {
        console.log('Cannot initialize twice.');
        return false;
      }
      var wa = kadecot._wa ; // === this
      wa.isOnAndroid = ( 'MyPageCall' in window ) ;

      wa.manif = manif ;
      wa.initcb = init_callback ;

      wa.manif.url = location.href ;

      wa.isInited = true ;
      wa.invokeWaitList = {} ;

      // start postmessage receiver
      wa.onMsgFromServer = function(origin,json_rpc){
	// console.log( 'message by addEventListener_message : ' +
  // JSON.stringify(arguments) ) ;
        // 全てのサーバーからのメソッド呼び出しのルート

        if (json_rpc.method === 'onGetValue'
                || json_rpc.method === 'onSetValue'
                || json_rpc.method === 'onQueryLog'
                || json_rpc.method === 'onInvoke'
                || json_rpc.method === 'onEvent') {
	  if( wa.invokeWaitList[ json_rpc.id ] === undefined ){
	    console.log('No matching getsetValue call found for onGetSetValue/onQueryLog') ;
	  } else {
	    wa.invokeWaitList[ json_rpc.id ].call( wa , json_rpc.params[0] ) ;
	  }
	} else {
		var method = wa.myPageAPI[json_rpc.method] ;
		if( method === undefined )
			console.log('No method named '+json_rpc.method) ;
		else 
          method.apply( wa , [origin].concat(json_rpc.params) ) ;
	}
      }

      if( wa.isOnAndroid ){
        wa.postMsgToMyPage('reqMyPageConnected',[JSON.stringify(manif)] );
      } else {
        window.addEventListener("message",function(e){
          kadecot._wa.onMsgFromServer(e.origin,JSON.parse(e.data));
        }, false);


        wa.postMsgToMyPage('reqMyPageConnected',[JSON.stringify(manif)] );
      return true ;
    } // kadecot._wa.init end

    ,
    genRandStr : function (){
	  if( typeof this.getRandStrSS === 'undefined' ){
	    this.getRandStrSS = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'.split('');
	  }
	  var ret = '';
	  for (var i = 0; i < 30; i++) {
	    ret += this.getRandStrSS[Math.floor(Math.random() * this.getRandStrSS.length)];
	  }
	return ret;
    },
    invokeGetValue: function(devname, propname, args_for_get, cbfunc) {
      var key = this.genRandStr();
      this.invokeWaitList[key] = cbfunc;
      this.postMsgToMyPage('getValue', (args_for_get === undefined ? [devname,
          propname] : [devname, propname, args_for_get]), key);
    },
    invokeSetValue: function(devname, propname, args_for_set, cbfunc) {
      var key = this.genRandStr();
      this.invokeWaitList[key] = cbfunc;
      this.postMsgToMyPage('setValue', (args_for_set === undefined ? [devname,
          propname] : [devname, propname, args_for_set]), key);
    },
    invoke: function(devname, methodName, args, cbfunc) {
	  var key = this.genRandStr() ;
	  this.invokeWaitList[key] = cbfunc ;
      console.log('invoke: device=' + devname + ", methodName=" + methodName
              + ", args=" + args + ", key=" + key);
      this.postMsgToMyPageNew('invoke', devname, methodName, args, key);
    },
    subscribe: function(devname, methodName, cbfunc) {
      var key = this.genRandStr();
      this.invokeWaitList[key] = cbfunc;
      console.log('invoke: device=' + devname + ", methodName=" + methodName
              + ", key=" + key);
      this.postMsgToMyPageNew('subscribe', devname, methodName, key);
    },
    invokeQueryLog: function(starttime, endtime, cbfunc) {
      var key = this.genRandStr();
      this.invokeWaitList[key] = cbfunc;
      this.postMsgToMyPage('queryLog', [starttime, endtime], key);
    },
    postMsgToMyPage: function(methodName, argsarray, key) {
      var msgToPost = JSON.stringify({
        'method': methodName,
        'params': (argsarray instanceof Array ? argsarray : null),
        'id': key
      });
      if (kadecot._wa.isOnAndroid) {
        MyPageCall.postMessage(msgToPost);
      } else {
        this.myPageWnd.postMessage(msgToPost, kadecot.myPageURL.substring(0,
        　　　　　　　　kadecot.myPageURL.lastIndexOf('/') + 1));
      }
    },
    postMsgToMyPageNew: function(method, deviceName, apiName, argsObj, key) {
      var msgToPost = JSON.stringify({
        'method': method,
        'deviceName': deviceName,
        'apiName': apiName,
        'params': argsObj,
        'id': key
      });
      if (kadecot._wa.isOnAndroid) {
        MyPageCall.postMessage(msgToPost);
      } else {
        // console.log('postMsgToMyPage : '+JSON.stringify( {'method':methodName
        // , 'params':(argsarray instanceof Array ? argsarray : null) , 'id':key
        // } )) ;

        this.myPageWnd.postMessage(msgToPost, kadecot.myPageURL.substring(0,
        　　　　　　　 kadecot.myPageURL.lastIndexOf('/') + 1));
      }
    }

    ,
    myPageAPI: { // this is kadecot._wa
      // Call from MyPage
      onMyPageConnected: function(origin, aosrc) {
        // Setup setter/getter etc.
        var memval = {};
        var ao = {};
        for ( var devname in aosrc) {
          if (typeof devname !== 'string') continue;
          (function() {
            var dname = devname;

            var mv = memval[dname] = {
              active: true
            };
            var av = ao[dname] = {};

            av.__defineGetter__('active', function() {
              return mv.active;
            });
            // set prohibited.(nothing happens)
            av.__defineSetter__('active', function() {});

            for ( var propname in aosrc[dname]) {
              if (propname === 'active') {
                // delete av.active ;
                continue;
              }
              (function() {
                var pname = propname;
                mv[pname] = aosrc[dname][pname]; // Initialize

                ao[dname][pname] = function(args, callback) {
                  console.log("Call ao: dname=" + dname + ", pname=" + pname);
                  kadecot._wa.invoke(dname, pname, args, function(newVal) {
                    callback.call(kadecot._wa, newVal);
                  });
                }

                ao[dname]['on' + pname] = function(callback) {
                  console.log("Subscribe ao: dname=" + dname + ", pname="
                          + pname);
                  kadecot._wa.subscribe(dname, pname, function(newVal) {
                    callback.call(kadecot._wa, newVal);
                  });
                }
              })();
            }
          })();
        }

		kadecot._wa.myPageAPI.onPropertyChanged = function(origin, devicename,
                propname, newval){
			if(memval[devicename] === undefined
                   || memval[devicename][propname]===undefined ){
				console.log('Undefined property change is notified') ;// Never happens?
				return ;
			}
			var oldVal = memval[devicename][propname] ;
			memval[devicename][propname] = newval ;
			var onChangeFunc = ao[devicename]['onChange'+propname] ;
			if( typeof onChangeFunc === 'function' 
                    && JSON.stringify(oldVal) !== JSON.stringify(newval))
				onChangeFunc.call(ao[devicename] , newval) ;
		} ;

		ao.stop = function(bWinClose){
			kadecot._wa.postMsgToMyPage('cleanup',[bWinClose] );
		} ;

		ao.queryLog = function(){
          kadecot._wa.invokeQueryLog.apply(kadecot._wa,arguments);
        };

		this.initcb( ao );
	}

	,
    onMyPageReady: function(){
		this.postMsgToMyPage('reqMyPageConnected',[JSON.stringify(this.manif)]);
	},
    showMessage : function( origin,msgStr ){
		if( typeof msgStr !== 'string' || msgStr.length <= 0 ){
			if( this.showMessageDiv !== undefined ){
				this.showMessageDiv.parentNode.removeChild(this.showMessageDiv) ;
				this.showMessageDiv = undefined ;
			}
			// console.log('Msg ed.') ;
		} else {
			var div = this.showMessageDiv ;
			if( div == undefined ){
				div = this.showMessageDiv = document.createElement('div');
				div.style.position = 'absolute' ;
				div.style.top = '0px' ;
				div.style.left = '0px' ;
				document.body.appendChild(div) ;
				div.innerHTML = '' ;
			}
			div.innerHTML += msgStr+'<br>' ;
			console.log(msgStr) ;
		}
	}

	// Dummy function. overwritten in onMyPageConnected
	,
    onPropertyChanged : function(){}

    }
 // myPageAPI end.

  }
// kadecot._wa end.
} ; // kadecot end.
