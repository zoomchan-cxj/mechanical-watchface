/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wearclan.mechanicalwatchface;

import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Listens to DataItems and Messages from the local node.
 */
public class DataLayerListenerService extends WearableListenerService {

    private static final String TAG = "AppDataLayer";
    private static boolean connected;

    private GoogleApiClient mGoogleApiClient;
    private WearableAPIHelper mWearApiHelper;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mobile", "DataLayerListenerService onCreate");
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mWearApiHelper = new WearableAPIHelper(this, new WearableAPIHelper.WearableAPIHelperListener() {
            @Override
            public void onWearableAPIConnected(GoogleApiClient apiClient) {
                mGoogleApiClient = apiClient;
                LOGD(TAG, "onWearableAPIConnected");
            }

            @Override
            public void onWearableAPIConnectionSuspended(int cause) {

            }

            @Override
            public void onWearableAPIConnectionFailed(ConnectionResult result) {

            }
        });
    }

    @Override
    public void onDestroy() {
        Log.d("mobile", "DataLayerListenerService onDestroy");
        super.onDestroy();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        connected = true;
        LOGD(TAG, "onDataChanged: " + dataEvents);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived: " + messageEvent);
        connected = true;
        String path = messageEvent.getPath();
        if (path.equals(MessagePaths.SEND_EVENT)) {
            boolean enable = true;
            if (enable) {
                String jsonStr = new String(messageEvent.getData());
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    String category = json.getString("c");
                    String action = json.getString("a");
                    String label = json.getString("l");
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .build());
                } catch (JSONException e) {
                }
            }
        } else if (path.equals(MessagePaths.SEND_SCREEN_VIEW)) {

                mTracker.setScreenName(new String(messageEvent.getData()));
                mTracker.send(new HitBuilders.AppViewBuilder().build());

        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        connected = true;

    }

    @Override
    public void onPeerDisconnected(Node peer) {
        connected = false;

    }

    public static boolean isConnectedWithWear() {
        return connected;
    }

    public static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}
