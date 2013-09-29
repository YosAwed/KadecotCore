KadecotCore
===========

オープンソースのAndroid用ホームサーバーアプリです。以下の機能を持っています。

*[ECHONET Lite][]プロトコルをサポートした家電やセンサーをJSONPやWebSocketのWebAPIから制御・情報取得できる
*ライブラリプロジェクトとして構築されているので、単体動作も他プロジェクトからリンクして用いることも可能
*機器連携WebアプリをJavaScriptで作りやすくするためのラッピングライブラリがある(kadecot.js)。簡単なものですがサンプルがassets/html/Apps/Test/index.htmlにあります。assets/html/js/kadecot.jsの最初の方のコメントもご参照ください。

KadecotCoreを用いた実証実験版アプリ[Kadecot][]がGoogle Playから配布されています。
いくつかの機能が追加されていますので、ホームサーバーの開発に興味がなく単に利用されたい場合はそちらをご利用ください。（ただし、2013年9月30日現在、Kadecotの方にはKadecotCoreは反映されておらず、次期バージョンでの対応となります。ご了承ください。Kadecotの次期アップデートは2013年10月中を予定しています）

本アプリは内部で[OpenECHO][]を用いています。こちらも独立したオープンソースとして配布していますので、WebAPI等が不要だという方はそちらをご参照ください。なお、KadecotCoreでは、プロトコル上詳細規定が存在しない電動カーテンオブジェクトが追加されています。

※本ソフトウェアの著作権は[株式会社ソニーコンピュータサイエンス研究所][]が保持しており、[MITライセンス][]で配布されています。ライセンスに従い，自由にご利用ください。

バグレポート等お待ちしています！
また、開発者MLみたいなものも作りたいと思っています。ご興味のある方は、ひとまずinfo@kadecot.netまで空メールを送って頂ければ幸いです。

[ECHONET Lite]: http://www.echonet.gr.jp/ "ECHONET Lite"
[Kadecot]: http://kadecot.net/ "Kadecot"
[OpenECHO]: https://github.com/SonyCSL/OpenECHO "OpenECHO"
[株式会社ソニーコンピュータサイエンス研究所]: http://www.sonycsl.co.jp/ "株式会社ソニーコンピュータサイエンス研究所"
[MITライセンス]: http://opensource.org/licenses/mit-license.php "MITライセンス"