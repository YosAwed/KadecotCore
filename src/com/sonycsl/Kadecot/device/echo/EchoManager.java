
package com.sonycsl.Kadecot.device.echo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sonycsl.Kadecot.call.ErrorResponse;
import com.sonycsl.Kadecot.core.Dbg;
import com.sonycsl.Kadecot.device.AccessException;
import com.sonycsl.Kadecot.device.DeviceData;
import com.sonycsl.Kadecot.device.DeviceDatabase;
import com.sonycsl.Kadecot.device.DeviceInfo;
import com.sonycsl.Kadecot.device.DeviceManager;
import com.sonycsl.Kadecot.device.DeviceProperty;
import com.sonycsl.Kadecot.device.DeviceProtocol;
import com.sonycsl.Kadecot.device.echo.generator.EchoDeviceAgent;
import com.sonycsl.Kadecot.device.echo.generator.EchoDeviceGenerator;
import com.sonycsl.Kadecot.log.Logger;
import com.sonycsl.echo.Echo;
import com.sonycsl.echo.EchoFrame;
import com.sonycsl.echo.EchoProperty;
import com.sonycsl.echo.EchoSocket;
import com.sonycsl.echo.EchoUtils;
import com.sonycsl.echo.eoj.EchoObject;
import com.sonycsl.echo.eoj.device.DeviceObject;
import com.sonycsl.echo.eoj.device.housingfacilities.PowerDistributionBoardMetering;
import com.sonycsl.echo.eoj.device.managementoperation.Controller;
import com.sonycsl.echo.eoj.device.sensor.HumiditySensor;
import com.sonycsl.echo.eoj.device.sensor.TemperatureSensor;
import com.sonycsl.echo.eoj.profile.NodeProfile;
import com.sonycsl.echo.node.EchoNode;

public class EchoManager implements DeviceProtocol {
    @SuppressWarnings("unused")
    private static final String TAG = EchoManager.class.getSimpleName();

    private final EchoManager self = this;

    public static final String PROTOCOL_TYPE_ECHO = "ECHONET Lite";

    private static EchoManager sInstance = null;

    private final Context mContext;

    private final MyNodeProfile mNodeProfile;

    private final MyController mController;

    private final Map<String, Callback> mCallbacks;

    static final long CALLBACK_TIME_OUT = 1000 * 10;

    static final long ACCESS_INTERVAL_TIME = 0;

    private final Map<String, EchoDeviceGenerator> mGenerators;

    private final EchoDiscovery mEchoDiscovery;

    private EchoDeviceDatabase mEchoDeviceDatabase;

    private DeviceManager mDeviceManager;

    private DeviceDatabase mDeviceDatabase;

    private final Map<InetAddress, Long> mLastAccessTimes;

    private EchoManager(Context context) {
        mContext = context.getApplicationContext();

        mNodeProfile = new MyNodeProfile();
        mController = new MyController();

        mCallbacks = new ConcurrentHashMap<String, Callback>();

        mGenerators = new ConcurrentHashMap<String, EchoDeviceGenerator>();

        mEchoDiscovery = new EchoDiscovery(mContext);

        mLastAccessTimes = new ConcurrentHashMap<InetAddress, Long>();

        setup();
    }

    public static synchronized EchoManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new EchoManager(context);
        }
        return sInstance;
    }

    private EchoDeviceDatabase getEchoDeviceDatabase() {
        if (mEchoDeviceDatabase == null) {
            mEchoDeviceDatabase = EchoDeviceDatabase.getInstance(mContext);
        }
        return mEchoDeviceDatabase;
    }

    private DeviceDatabase getDeviceDatabase() {
        if (mDeviceDatabase == null) {
            mDeviceDatabase = DeviceDatabase.getInstance(mContext);
        }
        return mDeviceDatabase;
    }

    private DeviceManager getDeviceManager() {
        if (mDeviceManager == null) {
            mDeviceManager = DeviceManager.getInstance(mContext);
        }
        return mDeviceManager;
    }

    private void setup() {
        Echo.addEventListener(new Echo.EventListener() {

            @Override
            public void onNewDeviceObject(DeviceObject device) {
                mEchoDiscovery.onDiscover(device);
            }

            @Override
            public void receiveEvent(EchoFrame frame) {
                // System.err.println(frame);
                EchoObject eoj =
                    Echo.getNode(frame.getSrcEchoAddress()).getInstance(
                        frame.getSrcEchoClassCode(), frame.getSrcEchoInstanceCode());
                EchoProperty[] properties = frame.getProperties();
                short tid = frame.getTID();
                final String callbackId = getCallbackId(tid, eoj);

                switch (frame.getESV()) {
                case EchoFrame.ESV_SET_RES:
                case EchoFrame.ESV_SETI_SNA:
                case EchoFrame.ESV_SETC_SNA:
                case EchoFrame.ESV_GET_RES:
                case EchoFrame.ESV_GET_SNA:
                    Dbg.print("receive:" + callbackId);

                    Callback callback = mCallbacks.get(callbackId);

                    // synchronized(EchoSocket.class) {

                    if (callback != null) {
                        Dbg.print("callback:" + callbackId);

                        mCallbacks.remove(callbackId);

                        callback.run(properties);
                    }

                    // }
                    break;
                case EchoFrame.ESV_INF:
                case EchoFrame.ESV_INF_SNA:
                case EchoFrame.ESV_INFC:
                    onReceiveInformerFrame(eoj, properties);
                    break;
                }
            }

            public void onReceiveInformerFrame(EchoObject eoj, EchoProperty[] properties) {
                EchoDeviceData data = getEchoDeviceDatabase().getDeviceData(eoj);
                if (data == null) {
                    return;
                }
                List<DeviceProperty> list = new ArrayList<DeviceProperty>();
                for (EchoProperty p : properties) {
                    boolean success = p.edt != null;
                    DeviceProperty prop =
                        new DeviceProperty(toPropertyName(p.epc), success ? toPropertyValue(p.edt)
                            : null, success);
                    list.add(prop);
                }
                getDeviceManager().onPropertyChanged(data, list);
            }

            @Override
            public void onGetProperty(EchoObject eoj, short tid, byte esv, EchoProperty property,
                boolean success) {
                super.onGetProperty(eoj, tid, esv, property, success);

                if (success && (property.epc == DeviceObject.EPC_GET_PROPERTY_MAP)) {
                    byte[] properties = EchoUtils.propertyMapToProperties(property.edt);
                    HashSet<DeviceProperty> watchingPropertySet = new HashSet<DeviceProperty>();
                    switch (eoj.getEchoClassCode()) {
                    case PowerDistributionBoardMetering.ECHO_CLASS_CODE:
                        watchingPropertySet
                            .add(new DeviceProperty(
                                toPropertyName(PowerDistributionBoardMetering.EPC_MEASURED_CUMULATIVE_AMOUNT_OF_ELECTRIC_ENERGY_NORMAL_DIRECTION)));
                        watchingPropertySet
                            .add(new DeviceProperty(
                                toPropertyName(PowerDistributionBoardMetering.EPC_MEASURED_CUMULATIVE_AMOUNT_OF_ELECTRIC_ENERGY_REVERSE_DIRECTION)));
                        watchingPropertySet
                            .add(new DeviceProperty(
                                toPropertyName(PowerDistributionBoardMetering.EPC_UNIT_FOR_CUMULATIVE_AMOUNTS_OF_ELECTRIC_ENERGY)));
                        for (byte p : properties) {
                            int i = p & 0xFF;
                            if (i >= (PowerDistributionBoardMetering.EPC_MEASUREMENT_CHANNEL1 & 0xFF)
                                && i <= (PowerDistributionBoardMetering.EPC_MEASUREMENT_CHANNEL32 & 0xFF)) {
                                watchingPropertySet.add(new DeviceProperty(toPropertyName(p)));
                            }
                        }
                        EchoDeviceData data = getEchoDeviceDatabase().getDeviceData(eoj);
                        if (data != null) {
                            long delay =
                                (Logger.DEFAULT_INTERVAL_MILLS)
                                    - (System.currentTimeMillis() % (Logger.DEFAULT_INTERVAL_MILLS));
                            Logger.getInstance(mContext).watch(data.nickname, watchingPropertySet,
                                Logger.DEFAULT_INTERVAL_MILLS, delay);
                        }
                        break;
                    }
                }
            }

        });
    }

    @Override
    public synchronized void start() {
        if (Echo.getSelfNode() != null) {
            try {
                Echo.restart();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        } else {
            ArrayList<DeviceObject> deviceList = new ArrayList<DeviceObject>();

            deviceList.add(mController);

            for (String protocolName : mGenerators.keySet()) {
                EchoDeviceGenerator gen = mGenerators.get(protocolName);
                List<EchoDeviceData> agentDataList =
                    getEchoDeviceDatabase().getDeviceDataList(protocolName);

                gen.onInitGenerator(agentDataList);

                for (EchoDeviceData data : agentDataList) {
                    EchoDeviceAgent agent = new EchoDeviceAgent(data, gen);
                    deviceList.add(agent);
                }
            }

            try {
                Echo.start(mNodeProfile, deviceList.toArray(new DeviceObject[] {}));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mEchoDiscovery.startDiscovering();

    }

    @Override
    public synchronized void stop() {
        try {
            Echo.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void refreshDeviceList() {
        mEchoDiscovery.clearActiveDevices();
        mEchoDiscovery.startDiscovering();
    }

    @Override
    public synchronized void deleteAllDeviceData() {
        try {
            Echo.stop();
            Echo.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEchoDiscovery.clearActiveDevices();
        getEchoDeviceDatabase().deleteAllDeviceData();

        for (String protocolName : mGenerators.keySet()) {
            mGenerators.get(protocolName).onDeleteAllEchoDevice();
        }

        setup();
    }

    // public String getCallbackId(short tid, final EchoObject eoj, final byte
    // epc) {
    // return tid+","+eoj.getNode().getAddress()+","+eoj.getEchoObjectCode();
    // }

    private String getCallbackId(short tid, final EchoObject eoj) {
        return tid + "," + eoj.getNode().getAddress() + "," + eoj.getEchoObjectCode();
    }

    private class Callback {
        public volatile EchoProperty[] properties = null;

        public void run(EchoProperty[] properties) {
            this.properties = properties;
        }
    }

    public static String toPropertyName(byte epc) {
        return "0x" + EchoUtils.toHexString(epc);
    }

    private static JSONArray toPropertyValue(byte[] edt) {
        JSONArray edtAry = new JSONArray();
        if (edt != null) {
            for (int i = 0; i < edt.length; i++) {
                edtAry.put(edt[i] & 0xff);
            }
        }
        return edtAry;
    }

    @Override
    public int getAllowedPermissionLevel() {
        return 1;
    }

    public static JSONObject convertPropertyAsJSON(String nickname, String propertyName,
        Object propertyData) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("nickname", nickname);
            jsonObj.put("property", propertyName);
            jsonObj.put("data", propertyData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObj;

    }

    @Override
    public void deleteDeviceData(long deviceId) {
        Dbg.print("deleteDeviceData:" + deviceId);
        EchoDeviceData data = getEchoDeviceDatabase().getDeviceData(deviceId);
        if (data == null) {
            return;
        }
        mEchoDiscovery.removeActiveDevices(deviceId);
        getEchoDeviceDatabase().deleteDeviceData(deviceId);

        if (data.parentId == null) {
            return;
        }

        DeviceData parentData = getDeviceDatabase().getDeviceData(data.parentId);
        if (parentData == null) {
            return;
        }
        for (String protocolName : mGenerators.keySet()) {
            if (protocolName.equals(parentData.protocolName)) {
                mGenerators.get(protocolName).onDeleteEchoDevice(data);
                break;
            }
        }
        return;
    }

    public EchoObject getEchoObject(long deviceId) throws UnknownHostException {

        EchoDeviceData data = getEchoDeviceDatabase().getDeviceData(deviceId);
        if (data == null) {
            return null;
        }

        String address = null;

        if (data.address.equals(EchoDeviceDatabase.LOCAL_ADDRESS)) {
            // local
            address = Echo.getSelfNode().getAddressStr();
        } else {
            // remote
            address = data.address;
        }

        EchoNode ne = Echo.getNode(address);
        if (ne == null) return null;
        EchoObject eoj = ne.getInstance(data.echoClassCode, data.instanceCode);
        return eoj;
    }

    @Override
    public List<DeviceProperty> set(long deviceId, List<DeviceProperty> propertyList)
        throws AccessException {
        EchoObject eoj = null;
        try {
            eoj = getEchoObject(deviceId);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE,
                "unknown host"));
        }

        if (eoj == null) {
            throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE,
                "Not found echo object"));
        }

        ArrayList<EchoProperty> list = new ArrayList<EchoProperty>();
        try {
            for (DeviceProperty p : propertyList) {
                byte epc = Integer.decode(p.name).byteValue();
                if (p.value instanceof Integer) {
                    JSONArray ja = new JSONArray();
                    ja.put(((Integer)p.value).intValue());
                    p.value = ja;
                }
                if (!(p.value instanceof JSONArray)) {
                    throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE));
                }
                JSONArray value = (JSONArray)p.value;
                byte[] edt = new byte[value.length()];
                for (int i = 0; i < value.length(); i++) {
                    edt[i] = (byte)value.getInt(i);
                }
                list.add(new EchoProperty(epc, edt));
            }
        } catch (Exception e) {
            throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e));
        }
        waitForAccess(eoj.getNode().getAddress());

        return setProperty(eoj, list);
    }

    private List<DeviceProperty> setProperty(EchoObject eoj, List<EchoProperty> propertyList)
        throws AccessException {
        try {
            final Thread current = Thread.currentThread();
            Callback callback = new Callback() {
                @Override
                public void run(EchoProperty[] properties) {
                    super.run(properties);

                    current.interrupt();
                }

            };

            HashMap<Byte, byte[]> map = new HashMap<Byte, byte[]>();

            // send
            // synchronized(EchoSocket.class) {
            EchoObject.Setter setter = eoj.set();
            for (EchoProperty p : propertyList) {
                setter.reqSetProperty(p.epc, p.edt);
                map.put((Byte)p.epc, p.edt);
            }
            String id = send(eoj, setter, callback);

            /*
             * short nextTid = EchoSocket.getNextTIDNoIncrement(); id = getCallbackId(nextTid, eoj);
             * mCallbacks.put(id, callback); short tid = setter.send(); if (nextTid != tid) {
             * mCallbacks.remove(id); Dbg.print("fault"); throw new AccessException(new
             * ErrorResponse( ErrorResponse.INTERNAL_ERROR_CODE, "fault")); }
             */
            // }

            // sleep
            try {
                Dbg.print("send:" + id);
                Thread.sleep(CALLBACK_TIME_OUT);
            } catch (InterruptedException e) {
            }

            if (mCallbacks.containsKey(id)) {
                mCallbacks.remove(id);
                // timeout
                Dbg.print("ECHONET Lite Timeout:" + id);
                throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE,
                    "ECHONET Lite Timeout"));
            } else {
                if (callback.properties != null) {
                    List<DeviceProperty> list = new ArrayList<DeviceProperty>();

                    for (EchoProperty p : callback.properties) {
                        DeviceProperty prop =
                            new DeviceProperty(toPropertyName(p.epc), toPropertyValue(map
                                .get(p.epc)), p.edt == null);
                        list.add(prop);
                    }
                    return list;
                } else {
                    throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e));
        }
    }

    @Override
    public List<DeviceProperty> get(long deviceId, List<DeviceProperty> propertyList)
        throws AccessException {
        EchoObject eoj = null;
        try {
            eoj = getEchoObject(deviceId);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE,
                "unknown host"));
        }

        if (eoj == null) {
            throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE,
                "Not found echo object"));
        }

        ArrayList<Byte> list = new ArrayList<Byte>();
        try {
            for (DeviceProperty dp : propertyList) {
                byte epc = Integer.decode(dp.name).byteValue();
                list.add(epc);
            }
        } catch (Exception e) {
            throw new AccessException(new ErrorResponse(ErrorResponse.INVALID_PARAMS_CODE, e));
        }
        waitForAccess(eoj.getNode().getAddress());
        return getProperty(eoj, list);
    }

    private void waitForAccess(InetAddress address) {

        long currentTime = System.currentTimeMillis();
        Long time = mLastAccessTimes.get(address);
        long lastAccessTime;
        if (time == null) {
            lastAccessTime = 0;
        } else {
            lastAccessTime = time;
        }
        long interval = currentTime - lastAccessTime;
        if (interval < ACCESS_INTERVAL_TIME) {
            Dbg.print("waitForAccess:" + (ACCESS_INTERVAL_TIME - interval));

            try {
                Thread.sleep(ACCESS_INTERVAL_TIME - interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mLastAccessTimes.put(address, System.currentTimeMillis());

    }

    private List<DeviceProperty> getProperty(EchoObject eoj, List<Byte> epcList)
        throws AccessException {
        try {
            final Thread current = Thread.currentThread();
            Callback callback = new Callback() {
                @Override
                public void run(EchoProperty[] properties) {
                    super.run(properties);

                    current.interrupt();
                }

            };

            // send
            // synchronized(EchoSocket.class) {
            EchoObject.Getter getter = eoj.get();
            for (Byte b : epcList) {
                getter.reqGetProperty(b);
            }
            String id = send(eoj, getter, callback);
            /*
             * short nextTid = EchoSocket.getNextTIDNoIncrement(); id = getCallbackId(nextTid, eoj);
             * mCallbacks.put(id, callback); short tid = getter.send(); if (nextTid != tid) {
             * mCallbacks.remove(id); Dbg.print("fault"); throw new AccessException(new
             * ErrorResponse( ErrorResponse.INTERNAL_ERROR_CODE, "fault")); }
             */
            // }

            // sleep
            try {
                Dbg.print("send:" + id + "," + eoj.getClass().getSimpleName());
                Thread.sleep(CALLBACK_TIME_OUT);
            } catch (InterruptedException e) {
            }

            if (mCallbacks.containsKey(id)) {
                mCallbacks.remove(id);
                // timeout
                Dbg.print("ECHONET Lite Timeout:" + id);
                throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE,
                    "ECHONET Lite Timeout"));
            } else {
                if (callback.properties != null) {

                    List<DeviceProperty> list = new ArrayList<DeviceProperty>();

                    for (EchoProperty p : callback.properties) {
                        DeviceProperty prop = new DeviceProperty(toPropertyName(p.epc));

                        if (p.edt != null) {
                            prop.value = toPropertyValue(p.edt);
                        } else {
                            prop.value = null;
                        }

                        prop.success = (p.edt != null);
                        list.add(prop);
                    }
                    return list;
                } else {
                    throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, e));
        }
    }

    private synchronized String send(EchoObject eoj, Object sender, Callback callback)
        throws AccessException, IOException {

        short nextTid = EchoSocket.getNextTIDNoIncrement();
        String id = getCallbackId(nextTid, eoj);
        mCallbacks.put(id, callback);

        short tid = (short)(nextTid - 1);
        if (sender instanceof EchoObject.Setter) {
            tid = ((EchoObject.Setter)sender).send().getTID();
        } else if (sender instanceof EchoObject.Getter) {
            tid = ((EchoObject.Getter)sender).send().getTID();
        }

        if (nextTid != tid) {
            mCallbacks.remove(id);
            Dbg.print("fault");

            throw new AccessException(new ErrorResponse(ErrorResponse.INTERNAL_ERROR_CODE, "fault"));
        }
        return id;
    }

    public void addEchoDeviceGenerator(EchoDeviceGenerator generator) {
        mGenerators.put(generator.getProtocolName(), generator);
    }

    @Override
    public DeviceInfo getDeviceInfo(long deviceId, String locale) {

        EchoDeviceData data = getEchoDeviceDatabase().getDeviceData(deviceId);
        if (data == null) {
            return null;
        }
        boolean active =
            mEchoDiscovery.isActiveDevice(data.address, data.echoClassCode, data.instanceCode);
        JSONObject option = new JSONObject();
        if (data.parentId != null) {
            JSONObject obj = getDeviceManager().getDeviceInfo(data.parentId, 0);

            if (obj != null) {
                try {
                    option.put("parent", obj);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        DeviceInfo info =
            new DeviceInfo(active, EchoDeviceUtils.getClassName(data.echoClassCode), "0x"
                + EchoUtils.toHexString(data.echoClassCode), option);
        Dbg.print("(echo device info)nickname:" + data.nickname + ",address:" + data.address
            + ",instanceCode:" + data.instanceCode + ",active:" + active);
        return info;
    }

    @Override
    public String getProtocolName() {
        return PROTOCOL_TYPE_ECHO;
    }

    public synchronized byte generateDevice(short echoClassCode, long parentId) {
        Dbg.print();
        int instanceCode;
        EchoNode node = Echo.getSelfNode();

        EchoDeviceData data;

        if (node == null) {
            return 0;
        }

        DeviceData parentData = getDeviceDatabase().getDeviceData(parentId);
        if (parentData == null) {
            return 0;
        }

        EchoDeviceGenerator gen = mGenerators.get(parentData.protocolName);
        if (gen == null) {
            return 0;
        }

        // synchronized(mEchoDeviceDatabase) {
        // / instance codeを決める
        List<Integer> instanceCodeList =
            getEchoDeviceDatabase().getLocalDeviceInstanceCodeList(echoClassCode);

        if (instanceCodeList.size() == 0) {
            instanceCode = EchoDeviceDatabase.MIN_INSTANCE_CODE;
        } else {
            instanceCode = instanceCodeList.get(instanceCodeList.size() - 1) + 1;
            if (instanceCode >= EchoDeviceDatabase.MAX_INSTANCE_CODE) {
                instanceCode = EchoDeviceDatabase.MIN_INSTANCE_CODE;
            }

            if (instanceCodeList.contains(instanceCode)) {

                for (int code : instanceCodeList) {
                    instanceCode = code + 1;
                    if (!instanceCodeList.contains(instanceCode)) {
                        break;
                    }
                }
            }

            if (instanceCode >= EchoDeviceDatabase.MAX_INSTANCE_CODE) {
                return 0;
            }
        }
        Dbg.print("new device instance code:" + instanceCode);
        // /
        data =
            getEchoDeviceDatabase().addLocalDeviceData(echoClassCode, (byte)instanceCode, parentId);
        if (data == null) {
            return 0;
        }
        // }
        Dbg.print("nickname:" + data.nickname + ",instanceCode:" + data.instanceCode);
        node.addDevice(new EchoDeviceAgent(data, gen));
        try {
            node.getNodeProfile().inform().reqInformSelfNodeInstanceListS().send();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Dbg.print(instanceCode);

        return (byte)instanceCode;
    }

}
