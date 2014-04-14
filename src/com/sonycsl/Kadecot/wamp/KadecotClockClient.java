
package com.sonycsl.Kadecot.wamp;

import android.util.Log;

import com.sonycsl.wamp.WampBroker;
import com.sonycsl.wamp.WampPublisher;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;

import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KadecotClockClient {

    private static final String TAG = KadecotClockClient.class.getSimpleName();

    private PrivateSearchTask mTimerTask;

    private WampPublisher mSearchPublisher;

    private WampPublisher mPublisher;

    private long mPollingTime;

    private TimeUnit mTimeUnit;

    private ScheduledExecutorService scheduler;

    private ScheduledFuture<?> future;

    private CountDownLatch mHelloLatch;

    private CountDownLatch mGoodbyeLatch;

    private boolean mIsStarted = false;

    private class SearchPublish extends WampPublisher {

        @Override
        protected void onConsumed(WampMessage msg) {
            if (msg.isWelcomeMessage()) {
                mHelloLatch.countDown();
            }
            if (msg.isGoodbyeMessage()) {
                mGoodbyeLatch.countDown();
                stop();
            }
        }
    }

    private static class PrivateSearchTask implements Runnable {

        private WampPublisher mPublisher;

        private int requestId = 0;

        public PrivateSearchTask(WampPublisher publisher) {
            mPublisher = publisher;
        }

        @Override
        public void run() {
            mPublisher.broadcast(WampMessageFactory.createPublish(++requestId,
                    new JSONObject(), KadecotWampTopic.TOPIC_PRIVATE_SEARCH));
        }
    }

    public KadecotClockClient(WampBroker broker, long pollingTime, TimeUnit timeUnit) {
        mPollingTime = pollingTime;
        mTimeUnit = timeUnit;

        mPublisher = new SearchPublish();
        mPublisher.connect(broker);

        mTimerTask = new PrivateSearchTask(mPublisher);
    }

    public synchronized void start() {
        Log.d(TAG, "start timmer");
        if (mIsStarted) {
            return;
        }

        initWamp();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        future = scheduler.scheduleAtFixedRate(mTimerTask, 0, mPollingTime, mTimeUnit);
        mIsStarted = true;
    }

    private void initWamp() {
        mHelloLatch = new CountDownLatch(1);
        mGoodbyeLatch = new CountDownLatch(1);

        broadcastSyncHello();
    }

    public synchronized void stop() {
        Log.d(TAG, "stop timmer");
        if (!mIsStarted) {
            return;
        }

        mIsStarted = false;
        future.cancel(true);
        scheduler.shutdown();
        releaseWamp();
    }

    private void releaseWamp() {
        broadcastSyncGoodbye();

        mHelloLatch = new CountDownLatch(1);
        mGoodbyeLatch = new CountDownLatch(1);
    }

    private void broadcastSyncHello() {
        mPublisher.broadcast(WampMessageFactory.createHello("realm", new JSONObject()));
        try {
            if (!mHelloLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Welcome message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastSyncGoodbye() {
        mPublisher.broadcast(WampMessageFactory.createGoodbye(new JSONObject(), ""));
        try {
            if (!mGoodbyeLatch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Router returns no Goodbye message");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
