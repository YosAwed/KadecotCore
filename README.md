KadecotCore
===========

[![Build Status](https://travis-ci.org/SonyCSL/KadecotCore.svg?branch=master)](https://travis-ci.org/SonyCSL/KadecotCore)

オープンソースのAndroid用ホームサーバーアプリです。  
以下の機能を持っています。  

* [ECHONET Lite][] プロトコルをサポートした家電やセンサーを JSONP や WebSocket の WebAPI から制御・情報取得できる  
* ライブラリプロジェクトとして構築されているので、単体動作も他プロジェクトからリンクして用いることも可能  
* プラグインを開発することにより、自分の好きなプロトコルを自由に追加できる
- [Hueプラグイン by ajimitei](https://github.com/ajimitei/huePlugin)

KadecotCoreを用いた実証実験版アプリ [Kadecot][] が Google Play から配布されています。  
いくつかの機能が追加されていますので、ホームサーバーの開発に興味がなく単に利用されたい場合はそちらをご利用ください。  

本アプリは内部で [OpenECHO][] を用いています。こちらも独立したオープンソースとして配布していますので、WebAPI 等が不要だという方はそちらをご参照ください。  

**本ソフトウェアの著作権は[株式会社ソニーコンピュータサイエンス研究所][]が保持しており、[MITライセンス][]で配布されています。**  
**ライセンスに従い，自由にご利用ください。**

バグレポート等お待ちしています！  

また、開発者 ML みたいなものも作りたいと思っています。  
ご興味のある方は、ひとまず info@kadecot.net まで空メールを送って頂ければ幸いです(特に返答システムはありませんのでご了承ください)。  

## 必要なもの

* [repo](https://source.android.com/source/using-repo.html)
* [android-sdk](http://developer.android.com/sdk/index.html)


## コードの取得方法

    $ mkdir work
    $ cd work
    $ repo init -u https://github.com/SonyCSL/manifest.git -b master
    $ repo sync
    $ repo start master --all


## ビルド及びインストール方法

**一時的にKadecotSampleの使用ができません。**

### eclipse

work 以下を import し KadecotSample を Run As -> Android Application してください。

### ant

    $ cd work/sample
    $ ant debug
    $ adb install bin/KadecotSample.apk


以下本プロジェクトで用いているソフトウェアとライセンス文章です：

===========
**Java-WebSocket**

https://github.com/TooTallNate/Java-WebSocket


MIT License (Copyright (C) 2010-2012 Nathan Rajlich)

Copyright (c) 2010-2012 Nathan Rajlich

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

===========
**NanoHttpd**

https://github.com/NanoHttpd/nanohttpd


Copyright (c) 2012-2013 by Paul S. Hawke, 2001,2005-2013 by Jarno Elonen, 2010 by Konstantinos Togias
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

* Neither the name of the NanoHttpd organization nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


===========
**JmDNS**

   Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

[ECHONET Lite]: http://www.echonet.gr.jp/ "ECHONET Lite"
[Kadecot]: http://kadecot.net/ "Kadecot"
[OpenECHO]: https://github.com/SonyCSL/OpenECHO "OpenECHO"
[株式会社ソニーコンピュータサイエンス研究所]: http://www.sonycsl.co.jp/ "株式会社ソニーコンピュータサイエンス研究所"
[MITライセンス]: http://opensource.org/licenses/mit-license.php "MITライセンス"
