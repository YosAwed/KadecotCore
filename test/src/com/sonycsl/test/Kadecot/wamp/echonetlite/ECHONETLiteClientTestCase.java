
package com.sonycsl.test.Kadecot.wamp.echonetlite;

import android.test.AndroidTestCase;

import com.sonycsl.Kadecot.wamp.echonetlite.ECHONETLiteClient;
import com.sonycsl.test.mock.MockWampRouter;
import com.sonycsl.test.util.TestableCallback;
import com.sonycsl.test.util.WampTestParam;
import com.sonycsl.test.util.WampTestUtil;
import com.sonycsl.wamp.WampError;

import java.util.Set;

public class ECHONETLiteClientTestCase extends AndroidTestCase {

    ECHONETLiteClient mClient;
    MockWampRouter mRouter;

    @Override
    protected void setUp() throws Exception {
        mClient = new ECHONETLiteClient(getContext());
        mClient.setCallback(new TestableCallback());
        mRouter = new MockWampRouter();
        mRouter.setCallback(new TestableCallback());
        mClient.connect(mRouter);
    }

    public void testCtor() {
        assertNotNull(mClient);
    }

    public void testGetSubscribableTopics() {
        Set<String> topics = mClient.getTopicsToSubscribe();
        assertNotNull(topics);
        assertTrue(topics.size() > 0);
    }

    public void testGetRegisterableProcedures() {
        Set<String> procs = mClient.getRegisterableProcedures().keySet();
        assertNotNull(procs);
        assertTrue(procs.size() > 0);
    }

    public void testHello() {
        WampTestUtil.transmitHelloSuccess(mClient, WampTestParam.REALM, mRouter);
    }

    public void testGoodbye() {
        WampTestUtil.transmitGoodbyeSuccess(mClient, WampError.CLOSE_REALM, mRouter);
    }

    // TODO: モジュールの依存関係を単純化し、テスト可能なクラスへリファクタを行う
}
