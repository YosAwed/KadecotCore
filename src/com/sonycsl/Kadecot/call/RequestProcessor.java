
package com.sonycsl.Kadecot.call;

import android.content.Context;
import android.util.Log;

import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.device.DeviceProperty;
import com.sonycsl.Kadecot.log.Logger;
import com.sonycsl.Kadecot.server.ServerSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestProcessor {
    @SuppressWarnings("unused")
    private static final String TAG = RequestProcessor.class.getSimpleName();

    private static final String POLLING_METHOD_NAME = "pollProperty";
    private final RequestProcessor self = this;

    protected final Context mContext;

    protected final int mPermissionLevel;

    protected DeviceManager mDeviceManager;

    protected ServerSettings mServerSettings;

    protected Logger mLogger;

    public RequestProcessor(Context context, int permissionLevel) {
        mContext = context.getApplicationContext();
        mPermissionLevel = permissionLevel;

        mDeviceManager = DeviceManager.getInstance(mContext);
        mServerSettings = ServerSettings.getInstance(mContext);
        mLogger = Logger.getInstance(mContext);
    }

    /**
     * This method adds clientId to params for pollProperty API.
     * 
     * @param methodName
     * @param params
     * @param clientId This is used to identify client in pollProperty API
     * @return
     */
    public Response process(final String methodName, JSONArray params, UUID clientId) {
        if (methodName.equals(POLLING_METHOD_NAME)) {
            try {
                params.put(0, clientId);
            } catch (JSONException e) {
                Log.e(TAG, "JSONException for pollProperty.");
                e.printStackTrace();
            }
        }
        return process(methodName, params);
    }

    public Response process(final String methodName, final JSONArray params) {
        Log.v(TAG, methodName);
        Log.v(TAG, params.toString());
        try {
            Method method = getClass().getMethod(methodName, new Class[] {
                    JSONArray.class
            });

            try {
                return (Response) method.invoke(this, new Object[] {
                        params
                });
            } catch (CannotProcessRequestException e) {
                return e.getErrorResponse();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.METHOD_NOT_FOUND_CODE, e);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.METHOD_NOT_FOUND_CODE, e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e);
        }
    }

    // devices

    public Response set(JSONArray params) {
        String nickname;
        ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();
        if (params == null || params.length() <= 0) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            nickname = params.getString(0);
            for (int i = 1; i < params.length(); i++) {
                JSONArray prop = params.getJSONArray(i);
                if (prop == null || prop.length() != 2) {
                    return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
                }
                DeviceProperty dp = new DeviceProperty(prop.getString(0), prop.get(1));
                propertyList.add(dp);
            }
            return mDeviceManager.set(nickname, propertyList, mPermissionLevel);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response get(JSONArray params) {
        String nickname;
        ArrayList<DeviceProperty> propertyList = new ArrayList<DeviceProperty>();
        if (params == null || params.length() <= 0) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            nickname = params.getString(0);
            for (int i = 1; i < params.length(); i++) {
                JSONArray prop = params.getJSONArray(i);
                if (prop == null) {
                    return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
                }
                DeviceProperty dp = new DeviceProperty(prop.getString(0), prop.get(1));
                propertyList.add(dp);
            }
            return mDeviceManager.get(nickname, propertyList, mPermissionLevel);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response pollProperty(JSONArray params) {
        if (params == null || params.length() <= 0) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            UUID clientId = (UUID) params.get(0);
            String nickname = params.getString(1);
            JSONObject prop = params.getJSONObject(2);
            DeviceProperty dp = new DeviceProperty(prop.getString("proprtyName"),
                    prop.getJSONObject("propertyValue"));
            int pollingIntervalSec = params.getInt(3);
            return mDeviceManager.pollProperty(clientId, nickname, dp, pollingIntervalSec,
                    mPermissionLevel);
        } catch (JSONException e) {
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }

    }

    public Response queryLog(JSONArray params) {
        long beginning;
        long end;
        if (params == null || params.length() <= 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            beginning = params.getLong(0);
            end = params.getLong(1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }

        JSONArray logList;
        if (params.length() >= 3) {
            try {
                final JSONObject obj = params.getJSONObject(2);

                logList = mLogger.queryLog(beginning, end, new Logger.LogFilter() {
                    @Override
                    public boolean predicate(LinkedHashMap<String, String> data) {
                        try {
                            Iterator keys = obj.keys();
                            boolean flag = true;
                            while (keys.hasNext()) {
                                String key = (String) keys.next();
                                String reg = obj.getString(key);
                                String dataStr = data.get(key);
                                Pattern p = Pattern.compile(reg);
                                Matcher m = p.matcher(dataStr);
                                if (!m.find()) {
                                    flag = false;
                                    break;
                                }
                            }
                            return flag;
                            // if(!obj.isNull("nickname")) {
                            // String nicknameReg = obj.getString("nickname");
                            // String dataNickname = Logger.getNickname(data);
                            // Pattern p = Pattern.compile(nicknameReg);
                            // Matcher m = p.matcher(dataNickname);
                            // if (m.find()){
                            // } else {
                            // return false;
                            // }
                            // }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return false;
                        }
                        // return true;
                    }
                });
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                logList = mLogger.queryLog(beginning, end);

            }
        } else {
            logList = mLogger.queryLog(beginning, end);
        }

        return new Response(logList);
    }

    public Response refreshList(JSONArray params) {
        mDeviceManager.refreshList(mPermissionLevel);
        return new Response(null);
    }

    public Response list(JSONArray params) {
        return new Response(mDeviceManager.list(mPermissionLevel));
    }

    public Response changeNickname(JSONArray params) {
        return mDeviceManager.changeNickname(params);
    }

    public Response deleteDevice(JSONArray params) {
        return mDeviceManager.deleteDeviceData(params);
    }

    public Response deleteInactiveDevices(JSONArray params) {
        return mDeviceManager.deleteInactiveDevices(mPermissionLevel);
    }

    // server settings
    public Response fullInitialize(JSONArray params) {
        mServerSettings.fullInitialize();
        return new Response(null);
    }

    public Response setServerLocation(JSONArray params) {
        if (params == null || params.length() < 2) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            String lat = params.getString(0);
            String lng = params.getString(1);
            mServerSettings.setLocation(lat, lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Response(null);
    }

    public Response enableServerNetwork(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            if (enabled) {
                return mServerSettings.registerNetwork();
            } else {
                return mServerSettings.unregisterNetwork();
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }

    }

    public Response enableWebSocketServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableWebSocketServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableJSONPServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableJSONPServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enableSnapServer(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enableSnapServer(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

    public Response enablePersistentMode(JSONArray params) {
        if (params == null || params.length() < 1) {
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE);
        }
        try {
            boolean enabled = params.getBoolean(0);
            mServerSettings.enablePersistentMode(enabled);
            return new Response(true);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e);
        }
    }

}
