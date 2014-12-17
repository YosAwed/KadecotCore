/*
 * Copyright (C) 2013-2014 Sony Computer Science Laboratories, Inc. All Rights Reserved.
 * Copyright (C) 2014 Sony Corporation. All Rights Reserved.
 */

package com.sonycsl.Kadecot.server.http.response;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;

import com.sonycsl.Kadecot.preference.AccountSettingsPreference;
import com.sonycsl.Kadecot.provider.KadecotCoreStore;
import com.sonycsl.Kadecot.service.KadecotService;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

public class OauthResponseFactory extends ResponseFactory {

    private static final String OAUTH2 = "/oauth2";
    private static final String AUTH = "/auth";
    private static final String NAME = "/name";

    private static final String CHALLENGE_HEADER_KEY = "challenge";
    private static final String RESPONSE_PARAM_KEY = "response";
    private static final String LOCATION_HEADER_KEY = "location";

    private static final String REDIRECT_URI_PARAM_KEY = "redirect_uri";
    private static final String SCOPE_PARAM_KEY = "scope";

    private static final String DEFAULT_SCOPE = "com.sonycsl.kadecot.provider";

    private static final Response BAD_REQUEST = new ResponseBuilder(Status.BAD_REQUEST).build();

    private static final Response FORBIDDEN = new ResponseBuilder(Status.FORBIDDEN).build();

    private final Context mContext;
    private String mChallenge;
    private String mRedirect;
    private String mScope;

    public OauthResponseFactory(Context context) {
        mContext = context;
    }

    private String byte2HexString(byte[] md5) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < md5.length; i++) {
            if ((0xFF & md5[i]) < 16) {
                builder.append("0");
            }
            builder.append(Integer.toHexString(0xFF & md5[i]));
        }
        return builder.toString();
    }

    private String calcMD5() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        String md5 = byte2HexString(digest.digest(new String(mChallenge + ":"
                + AccountSettingsPreference.getPass(mContext)).getBytes()));
        return md5;
    }

    @Override
    protected Response createResponse(IHTTPSession session, String rootPath) {
        int index = rootPath.equals("/") ? 0 : rootPath.length();

        String path = session.getUri().substring(index);

        if (path.equals(OAUTH2 + NAME)) {
            return new ResponseBuilder(Status.OK, NanoHTTPD.MIME_PLAINTEXT,
                    AccountSettingsPreference.getName(mContext)).build();
        }

        if (!path.equals(OAUTH2 + AUTH)) {
            return BAD_REQUEST;
        }

        if (AccountSettingsPreference.getPass(mContext).equals("")) {
            return FORBIDDEN;
        }

        if (session.getParms().containsKey(RESPONSE_PARAM_KEY)) {
            return createOAuthResponse(session);
        }

        Map<String, String> param = session.getParms();
        if (!param.containsKey(REDIRECT_URI_PARAM_KEY)) {
            return createUnauthorized();
        }

        final String redirect = param.get(REDIRECT_URI_PARAM_KEY);
        try {
            new URL(redirect);
        } catch (MalformedURLException e) {
            if (!redirect.startsWith(KadecotService.CUSTOM_URL_SCHEME)) {
                return BAD_REQUEST;
            }
        }
        mRedirect = redirect;

        mScope = param.containsKey(SCOPE_PARAM_KEY) ?
                param.get(SCOPE_PARAM_KEY) + "," + DEFAULT_SCOPE : DEFAULT_SCOPE;

        return createUnauthorized();
    }

    private Response createUnauthorized() {
        mChallenge = UUID.randomUUID().toString();
        return new ResponseBuilder(Status.UNAUTHORIZED)
                .addHeader(CHALLENGE_HEADER_KEY, mChallenge)
                .addHeader("Pragma", "no-cache")
                .addHeader("Cache-Control", "no-store")
                .addHeader("expires", "0").build();
    }

    private Response createOAuthResponse(IHTTPSession session) {
        String clientMD5 = session.getParms().get(RESPONSE_PARAM_KEY);
        if (!clientMD5.equals(calcMD5())) {
            return createUnauthorized();
        }

        Uri uri = Uri.parse(mRedirect);
        String origin = uri.getScheme() + "://" + uri.getAuthority();
        String token = UUID.randomUUID().toString();

        ContentProviderClient client = mContext.getContentResolver()
                .acquireContentProviderClient(KadecotCoreStore.Handshakes.CONTENT_URI);
        try {
            client.delete(KadecotCoreStore.Handshakes.CONTENT_URI,
                    KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN + "=?",
                    new String[] {
                        origin
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            return BAD_REQUEST;
        } finally {
            client.release();
        }

        client = mContext.getContentResolver().acquireContentProviderClient(
                KadecotCoreStore.Handshakes.CONTENT_URI);
        ContentValues values = new ContentValues();
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.ORIGIN, origin);
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.TOKEN, token);
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.STATUS, 1);
        values.put(KadecotCoreStore.Handshakes.HandshakeColumns.SCOPE, mScope);
        try {
            client.insert(KadecotCoreStore.Handshakes.CONTENT_URI, values);
        } catch (RemoteException e) {
            e.printStackTrace();
            return BAD_REQUEST;
        } finally {
            client.release();
        }

        return new ResponseBuilder(Status.FOUND)
                .addHeader(LOCATION_HEADER_KEY, mRedirect + "#" + token)
                .addHeader("Pragma", "no-cache").addHeader("Cache-Control", "no-store")
                .addHeader("expires", "0").build();
    }
}
