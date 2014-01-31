package com.sonycsl.Kadecot.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sonycsl.Kadecot.call.RequestProcessor;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


// See original python code for protocol.
public class KadecotSnapServer extends HTTPServer{
    private static KadecotSnapServer instance;
    private final static String ACCESS_ORIGIN = "http://snap.berkeley.edu";
    private final static String BLOCK_XML = "snap/block.xml";
    
    private static final String TYPE_XML = "text/xml";
    private static final String TYPE_PLAIN = "text/plain";
    private static final String IP_ADDRESS_PLACEHOLDER = "localhost";
    private static final String PORT_NUMBER_PLACEHOLDER = "31338";
    private Context context;

    private KadecotSnapServer(Context context) {
        super(ACCESS_ORIGIN);
        initialize(context);
    }

    protected void initialize(final Context context){
        this.context = context;
        addHttpGet(new HttpGet("/block"){
            @Override
            public void run(Request req,Response res) throws IOException{
                // read block.xml here. should buffer?
                // replace localhost with IP Address.
                //  also port number.default is 31338.
                AssetManager am = context.getAssets();
                InputStream is = am.open(BLOCK_XML);
                BufferedReader in= new BufferedReader(new InputStreamReader(is));
                StringBuffer buf = new StringBuffer();
                String str;
                while ((str=in.readLine()) != null) {
                    buf.append(str);
                }
                in.close();
                str = buf.toString();
                
                str = str.replaceAll(IP_ADDRESS_PLACEHOLDER,ServerNetwork.getInstance(context).getIPAddress());
                str = str.replaceAll(PORT_NUMBER_PLACEHOLDER,runningPort+"");
                res.success(TYPE_XML,str);
            }
        });
        addHttpGet(new HttpGet("/list"){
            @Override
            public void run(Request req,Response res) throws IOException{
                try {
                    JSONObject response = new RequestProcessor(context, 1)
                                              .process("list",new JSONArray()).toJSON();
                    JSONArray ja = response.getJSONArray("result");
                    // JSONArray is not iterable :<
                    ArrayList<String> nicknames = new ArrayList<String>();
                    ArrayList<String> devtype = new ArrayList<String>();
                    for(int i=0;i<ja.length();i++){
                        JSONObject dev = ja.getJSONObject(i);
                        nicknames.add(dev.getString("nickname"));
                        devtype.add(dev.getString("deviceType"));
                    }
                    String ret = string_join(":",nicknames)+";"
                                +string_join(":",devtype);
                    res.success(TYPE_PLAIN, ret);
                } catch (JSONException e) {
                    e.printStackTrace();
                    res.failure(TYPE_PLAIN,"fail;can't get list(internal)");
                }
            }
        });
        addHttpGet(new HttpGet("/get"){
            @Override
            public void run(Request req,Response res) throws IOException{
                if(req.query.containsKey("nickname") && req.query.containsKey("epc")){
                    String nickname = urldecode(req.query.get("nickname"));
                    String epc = urldecode(req.query.get("epc"));
                    JSONArray args = new JSONArray();
                    args.put(nickname);args.put(epc);
                    try {
                        JSONObject response = new RequestProcessor(context, 1)
                                                      .process("get",args).toJSON();
                        JSONArray js = response.getJSONObject("result")
                                                .getJSONArray("property")
                                                .getJSONObject(0)
                                                .getJSONArray("value");
                        ArrayList<String> edt = new ArrayList<String>();
                        for(int i=0;i<js.length();i++){
                            edt.add(String.valueOf(js.getInt(i)));
                        }
                        res.success(TYPE_PLAIN,"success;"+string_join(":", edt));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        res.failure(TYPE_PLAIN, "fail;get internal error");
                    }
                }else{
                    res.failure(TYPE_PLAIN, "fail;nickname or epc not found");
                }
            }
        });
        addHttpGet(new HttpGet("/set"){
            @Override
            public void run(Request req,Response res) throws IOException{
                if(req.query.containsKey("nickname") && req.query.containsKey("epc") &&
                   req.query.containsKey("edt")){
                    String nickname = urldecode(req.query.get("nickname"));
                    String epc = urldecode(req.query.get("epc"));
                    String edt = urldecode(req.query.get("edt"));
                    

                    JSONArray epc_edt_pair = new JSONArray();
                    epc_edt_pair.put(epc);
                    JSONArray edtArray = new JSONArray();
                    for(String s : edt.split("-")){
                        edtArray.put(convert_hex_or_decimal_to_int(s));
                    }
                    epc_edt_pair.put(edtArray);
                    JSONArray args = new JSONArray();
                    args.put(nickname);
                    args.put(epc_edt_pair);
                    try {
                        JSONObject response = new RequestProcessor(context, 1)
                                                      .process("set",args).toJSON();
                        boolean success = response.getJSONObject("result")
                                       .getJSONArray("property")
                                       .getJSONObject(0).getBoolean("success");
                        if(!success){
                            Log.d("Snap",response.toString());
                            res.failure(TYPE_PLAIN,"fail;can't operate device?");
                            return;
                        }
                        JSONArray js = response.getJSONObject("result")
                                                .getJSONArray("property")
                                                .getJSONObject(0)
                                                .getJSONArray("value");
                        ArrayList<String> ed = new ArrayList<String>();
                        for(int i=0;i<js.length();i++){
                            ed.add(String.valueOf(js.getInt(i)));
                        }
                        Log.d("Snap",response.toString());
                        Log.d("Snap",js.toString());
                        res.success(TYPE_PLAIN,"success;"+string_join(":", ed));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        res.failure(TYPE_PLAIN, "fail;set internal error");
                    }
                }else{
                    res.failure(TYPE_PLAIN, "fail;nickname or epc or edt not found");
                }
            }
        });

    }

    public static KadecotSnapServer getInstance(Context context){
        if(instance == null){
            instance = new KadecotSnapServer(context);
        }
        return instance;
    }
    
    private static String string_join(String delimiter,ArrayList<String> strs){
        StringBuffer sb = new StringBuffer();
        for(String s : strs){
            if(sb.length() > 0){
                sb.append(delimiter);
            }
            sb.append(s);
        }
        return sb.toString();
    }
    public static int convert_hex_or_decimal_to_int(String num){
        if(num.length() > 2 && num.substring(0,2).equals("0x")){
            return Integer.parseInt(num.substring(2),16);
        }
        return Integer.parseInt(num);
    }
}
