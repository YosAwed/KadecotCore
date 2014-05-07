/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.kadecot.wamp;

import com.sonycsl.wamp.WampClient;
import com.sonycsl.wamp.WampPeer;
import com.sonycsl.wamp.message.WampMessage;
import com.sonycsl.wamp.message.WampMessageFactory;
import com.sonycsl.wamp.role.WampPublisher;
import com.sonycsl.wamp.role.WampRole;
import com.sonycsl.wamp.util.WampRequestIdGenerator;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class KadecotTopicTimer extends WampClient {

    private final String mTopic;
    private final long mPollingTime;
    private final TimeUnit mTimeUnit;
    private TimerPublisher mPublsher;

    public KadecotTopicTimer(String topic, long pollingTime, TimeUnit timeUnit) {
        mTopic = topic;
        mPollingTime = pollingTime;
        mTimeUnit = timeUnit;
    }

    @Override
    protected Set<WampRole> getClientRoleSet() {
        mPublsher = new TimerPublisher();
        Set<WampRole> roleSet = new HashSet<WampRole>();
        roleSet.add(mPublsher);
        return roleSet;
    }

    @Override
    protected void OnConnected(WampPeer peer) {
    }

    @Override
    protected void OnTransmitted(WampPeer peer, WampMessage msg) {
    }

    @Override
    protected void OnReceived(WampMessage msg) {
        if (msg.isWelcomeMessage()) {
            mPublsher.start(new Runnable() {
                @Override
                public void run() {
                    transmit(WampMessageFactory.createPublish(WampRequestIdGenerator.getId(),
                            new JSONObject(), mTopic));
                }
            }, mPollingTime, mTimeUnit);
            return;
        }

        if (msg.isGoodbyeMessage()) {
            mPublsher.stop();
            return;
        }
    }

    private static class TimerPublisher extends WampPublisher {

        private ScheduledExecutorService mScheduler;
        private ScheduledFuture<?> mFuture;

        public synchronized void start(Runnable runnable, long pollingTime, TimeUnit timeUnit) {
            if (mFuture != null) {
                return;
            }
            mScheduler = Executors.newSingleThreadScheduledExecutor();
            mFuture = mScheduler.scheduleAtFixedRate(runnable, 0, pollingTime, timeUnit);
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
