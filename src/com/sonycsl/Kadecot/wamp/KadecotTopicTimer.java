
package com.sonycsl.Kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KadecotTopicTimer extends WampClient {

    private final String mTopic;
    private final long mPollingTime;
    private final TimeUnit mTimeUnit;
    private TimerPublisher mPublsher;
    private Runnable mRunnable;

    public KadecotTopicTimer(String topic, long pollingTime, TimeUnit timeUnit) {
        mTopic = topic;
        mPollingTime = pollingTime;
        mTimeUnit = timeUnit;
    }

    @Override
    protected WampRole getClientRole() {
        mRunnable = new Runnable() {

            @Override
            public void run() {
                transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                        new JSONObject(), mTopic));
            }
        };
        mPublsher = new TimerPublisher(mRunnable, mPollingTime, mTimeUnit);
        return mPublsher;
    }

    @Override
    protected void onReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mPublsher.start();
            return;
        }

        if (msg.isGoodbyeMessage()) {
            mPublsher.stop();
            return;
        }
    }

    private static class TimerPublisher extends WampPublisher {

        private final long mPollingTime;
        private final TimeUnit mTimeUnit;
        private final Runnable mRunnable;
        private ScheduledExecutorService mScheduler;
        private ScheduledFuture<?> mFuture;

        public TimerPublisher(Runnable runnable, long pollingTime, TimeUnit timeUnit) {
            super();
            mRunnable = runnable;
            mPollingTime = pollingTime;
            mTimeUnit = timeUnit;
        }

        public synchronized void start() {
            if (mFuture != null) {
                return;
            }
            mScheduler = Executors.newSingleThreadScheduledExecutor();
            // mFuture = mScheduler.scheduleAtFixedRate(mRunnable, 0,
            // mPollingTime, mTimeUnit);
        }

        public synchronized void stop() {
            if (mFuture == null) {
                return;
            }

            mFuture.cancel(true);
            mFuture = null;
            mScheduler.shutdown();
        }
    }
}
