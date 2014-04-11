package com.sonycsl.Kadecot.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sonycsl.Kadecot.call.RequestProcessor;
import com.sonycsl.Kadecot.call.Response;
import com.sonycsl.Kadecot.device.DeviceManager;


// JSONPで返せるように
public class KadecotJSONPServer {
	@SuppressWarnings("unused")
	private static final String TAG = KadecotJSONPServer.class.getSimpleName();	
	private static KadecotJSONPServer sInstance;
	static final String TYPE_JSON = "application/json";
	
	private ServerSocket mServerSocket;
	private final List<HttpGet> mHttpGetList = new ArrayList<HttpGet>();
	private final HashMap<String,Result> mResults = new HashMap<String,Result>();
	
	private final String[] ACCESSIBLE_METHODS = {"refreshList","list","get","set"};
	private final HashSet<String> callableHomeNetWorkMethod = new HashSet<String>();

	//DeviceInfoDB mDeviceInfoDB;
	private final Context mContext;
	
	private boolean mRunning = false;
	
	private KadecotJSONPServer(Context context) {
		super();
		callableHomeNetWorkMethod.add("list");
		callableHomeNetWorkMethod.add("refreshList");
		callableHomeNetWorkMethod.add("access");
		callableHomeNetWorkMethod.add("getPowerHistory");

		//mDeviceInfoDB = new DeviceInfoDB(context);
		mContext = context;

		mHttpGetList.add(new HttpGet("/call.json"){
			@Override
			public void run(Request req, Response res) throws IOException {
				String callback = null;

				String method = urldecode(req.query.get("method"));

				JSONArray params = null;
				try {
					if(req.query.containsKey("params")) {
						params = new JSONArray(urldecode(req.query.get("params")));
					} else {
						params = new JSONArray();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(req.query.containsKey("callback")) {
					callback = urldecode(req.query.get("callback"));
				} else if(req.query.containsKey("jsoncallback")) {
					callback = urldecode(req.query.get("jsoncallback"));
				}
				//String id = UUID.randomUUID().toString().replaceAll("-", "");

				com.sonycsl.Kadecot.call.Response response = null;
				if(method != null && params != null) {
					for(String m : ACCESSIBLE_METHODS) {
						if(m.equals(method)) {
							response = (new RequestProcessor(mContext, 1)).process(method, params);
						}
					}
				}
				String result = "error";
				try {
					if(response != null) {
						result = response.toJSON().toString();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(callback != null) {
					result = callback + "(" + result + ")";
				}
				res.success(TYPE_JSON, result);
			}
		});
	}
	
	private class Result {
		Thread cur;
		JSONObject result;
		Result(Thread t) {
			cur = t;
		}
		
		void putResult(JSONObject result) {
			this.result = result;
			cur.interrupt();
		}
	}
	
	
	public void onGetResult(JSONObject result) {
		try {
			if(mResults.containsKey(result.get("key"))) {
				//Log.v(TAG, "ok:"+result.getString("key"));
				mResults.get(result.get("key")).putResult(result);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static KadecotJSONPServer getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new KadecotJSONPServer(context.getApplicationContext());
		}
		return sInstance;
	}


	public void start(int port) throws IOException {
		if(mRunning == true) return;
		mServerSocket = new ServerSocket();
		mServerSocket.setReuseAddress(true);
		mServerSocket.bind(new InetSocketAddress(port));
		//Log.v(TAG, "start");
		Executors.newSingleThreadExecutor().execute(new Runnable(){
			@Override
			public void run() {
				try {
					service();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					stop();
				}
			}
			
		});
		//ServerCallHttp.getInstance(mContext).start();
		mRunning = true;
	}
	
	
	public void stop() {
		//Log.v(TAG, "stop");
		//ServerCallHttp.getInstance(mContext).stop();

		try {
			if(mServerSocket!=null) {
				mServerSocket.close();
				mServerSocket = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			mRunning = false;
		}
	}
	
	public boolean isRunning() {
		return mRunning;
	}
	

	// main
	private void service() throws IOException {
		//System.out.println("service");
		for (Socket sock = accept(); sock != null; sock = accept()) {
			Executor executor = Executors.newSingleThreadExecutor();
			executor.execute(new HttpServerService(sock));
		}
	}
	
	private Socket accept() throws IOException {
		//Log.v(TAG, "accept");

		try {
			return mServerSocket.accept();
		} catch (SocketException e) {
			//System.out.println("done");
		}
		return null;
	}
	// assumute encoding is utf-8
	private String urldecode(String s){
	    if(s == null) return null;
        try {
            return URLDecoder.decode(s,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
	}
	
	///////////////////////////////////
	

	public String access(Map<String,String> arguments){
		String nickname_s = arguments.get("nickname");
		String epc_s = arguments.get("epc") ;
		
		if(  nickname_s==null || epc_s==null )
			return "{\"error\":\"Incomplete parameters for callmethod : nickname, epc are mandatory parameters.\"}" ;
		
		//DeviceInfoDB db = new DeviceInfoDB(mContext);
		//DeviceDatabaseManager db = DeviceDatabaseManager.getInstance(mContext);
		//if(!db.containsNickname(nickname_s)) {
			//db.close();
		//	return "{\"error\":\""+nickname_s+" does not exist.\"}" ;
		//}
		//byte epc = Integer.decode(epc_s).byteValue() ; //.byteValue() ;

		String args_sp = arguments.get("arg") ;
		// distinguish set/get by the existence of the parameter 'arg'
		

		//String node_id = db.getEchoDeviceAddress(nickname_s);
		//int obj = db.getEchoDeviceObjectCode(nickname_s);
		//db.close();
		// get method
		if( args_sp == null ) {
			//return mDevManager.callMethod( node_id,obj,epc,null ,null) ;
			//return DeviceManager.getInstance(mContext).callMethod(null, null, nickname_s, epc_s,null) ;
			return "";
		}

		// set method
		// split args_s into byte array 
		String[] args_s = args_sp.split(",") ;
		//byte[] params = new byte[args_s.length] ;
		//for( int ai=0;ai<args_s.length;++ai ) params[ai] = Integer.decode(args_s[ai]).byteValue() ;
		//return mDevManager.callMethod( node_id,obj,epc,params ,null) ;
		//return DeviceManager.getInstance(mContext).callMethod(null, null, nickname_s, epc_s,args_s) ;
		return "";
	}

	///////////////////////////////////
	
	private class Request {
		public String method;
		public String path = null;
		//public Map<String, String> params;
		public Map<String, String> query;
		public List<String> requests;
		Pattern REQUEST_LINE_PATTERN = 
				Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");
		public Request(InputStream in) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			query = new HashMap<String,String>();
			requests = new ArrayList<String>();
			String line;
			try {
				line = reader.readLine();
				while(line != null && line.length() != 0) {
					//System.out.println(line);
					requests.add(line);
					line = reader.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(requests.size() < 1) return;
			line = requests.get(0);
			Matcher m = REQUEST_LINE_PATTERN.matcher(line);
			if (m.matches()) {
				method = m.group(1);
				String p = m.group(2);
				String[] ss = p.split("\\?");
				path = ss[0];
				if(ss.length == 1) {
					return;
				} else {
					String q = p.substring(path.length() + 1);
					ss = q.split("&");
					for(String s : ss) {
						String[] r = s.split("=");
						if(r.length != 2) continue;
						query.put(r[0], r[1]);
					}
				}
			} else {
				return;
			}
		}
	}
	
	private class Response {
		OutputStream out;
		public Response(OutputStream out) {
			this.out = out;
		}

		public void success(String type, String content) {
			//System.out.println("success");

			PrintWriter writer = new PrintWriter(out);
			writer.print("HTTP/1.1 200 OK\r\n");
			writer.print("Connection: close\r\n");
			writer.print("ETag: \""+UUID.randomUUID().toString().replaceAll("-", "")+"\"\r\n");
			writer.print("Content-Length: ");
			writer.print(content.getBytes().length);
			writer.print("\r\n");
			writer.print("Content-Type: ");
			writer.print(type);
			writer.print("\r\n\r\n");
			writer.print(content);
			writer.flush();
		}
		public void failure(String type, String content) {
			//System.out.println("failure");
			PrintWriter writer = new PrintWriter(out);
			writer.print("HTTP/1.0 404 Not Found\r\n");
			writer.print("Connection: close\r\n");
			writer.print("ETag: \""+UUID.randomUUID().toString().replaceAll("-", "")+"\"\r\n");
			writer.print("Content-Length: ");
			writer.print(content.getBytes().length);
			writer.print("\r\n");
			writer.print("Content-Type: ");
			writer.print(type);
			writer.print("\r\n\r\n");
			writer.print(content);
			writer.flush();
		}

		
		public void sendNotFound() {
			failure("text","Not Found");
		}
	}
	
	private abstract class HttpGet {
		
		String path;

		public HttpGet(String path) {
			this.path = path;
		}

		public HttpGet(Pattern path) {
			// 未実装
		}
		
		public boolean match(String path) {
			
			
			return this.path.equalsIgnoreCase(path);
		}
		public abstract void run(Request req, Response res) throws IOException;
		
	}
	
	private class HttpServerService implements Runnable{
		
		Socket socket;
		
		
		HttpServerService(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			List<String> reqList;
			OutputStream out = null;
			InputStream in = null;
			try {
				out = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return;
			}
			try {
				in = socket.getInputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}

			try {
				//System.out.println("receive request");
				Request req = new Request(in);
				//System.out.println("received request");
				Response res = new Response(out);

				if(req.path == null) {
					res.sendNotFound();
					return;
				}

				//Log.v(TAG, "path:"+req.path);
				for(HttpGet httpGet : mHttpGetList) {
					if(httpGet.match(req.path)) {
						httpGet.run(req, res);
						return;
					}
				}
				res.sendNotFound();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					socket.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}	
	}
}
