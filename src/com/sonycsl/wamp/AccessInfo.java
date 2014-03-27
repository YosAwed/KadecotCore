
package com.sonycsl.wamp;


class AccessInfo<E> {
    private E mMessage;
    private WampMessenger mMessenger;

    AccessInfo(WampMessenger messenger, E Message) {
        mMessenger = messenger;
        mMessage = Message;
    }

    WampMessenger getMessenger() {
        return mMessenger;
    }

    E getReceivedMessage() {
        return mMessage;
    }
}
