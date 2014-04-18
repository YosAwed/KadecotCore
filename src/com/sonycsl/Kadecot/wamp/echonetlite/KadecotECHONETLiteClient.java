
package com.sonycsl.Kadecot.wamp.echonetlite;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.role.WampCallee;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.role.WampSubscriber;

public class KadecotECHONETLiteClient extends WampClient {

    private ECHONETLiteCallee mCallee;
    private ECHONETLitePublisher mPublisher;
    private ECHONETLiteSubscriber mSubscriber;

    public KadecotECHONETLiteClient() {
        super();
    }

    @Override
    protected WampRole getClientRole() {
        mCallee = new ECHONETLiteCallee();
        mPublisher = new ECHONETLitePublisher(mCallee);
        mSubscriber = new ECHONETLiteSubscriber(mPublisher);
        return mSubscriber;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
    }

    private class ECHONETLiteCallee extends WampCallee {

        public ECHONETLiteCallee() {
            super();
        }

        @Override
        protected WampMessage invocation(String procedure, WampMessage msg) {
            return null;
        }
    }

    private class ECHONETLitePublisher extends WampPublisher {

        public ECHONETLitePublisher(WampRole next) {
            super(next);
        }
    }

    private class ECHONETLiteSubscriber extends WampSubscriber {

        public ECHONETLiteSubscriber(WampRole next) {
            super(next);
        }

        @Override
        protected void event(String topic, WampMessage msg) {
        }
    }
}
