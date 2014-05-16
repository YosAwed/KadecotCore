// オンライン時とAndroid WebView内とで動作を入れ替えるためのメソッド

kHAPI.local = {
  init: function() {
    if (kHAPI.isOnAndroid) {
      kHAPI.local = Local;
      return;
    }

    var _this = kHAPI.local;

    var ao = undefined;
    ;
    this.playAudio = function(path) {
      _this.stopAudio();

      ao = new Audio(path);
      ao.play();
    };
    this.stopAudio = function() {
      if (ao === undefined) return;
      ao.pause();
      ao = undefined;
    };
  }

  ,
  openWebBrowser: function(url) {
    window.open(url, '_blank');
  }

// , playAudio : function(path) // defined in init()
// , stopAudio : function() // defined in init()
};
