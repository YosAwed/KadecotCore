package com.sonycsl.Kadecot.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.sonycsl.Kadecot.device.DeviceData;
import com.sonycsl.Kadecot.device.DeviceInfo;
import com.sonycsl.Kadecot.device.DeviceProperty;


// フォーマットの仕様は LTSV
// 日ごとに1ファイル
// ファイル名 : {year}_{month}_{day}.log, everyday.log
// センサログ(get:successアリ)と機器操作ログ(set:successアリ)
// 
// 使用電力計測・取得間隔 30分間隔以内
// データ蓄積期間（表示できることを前提とする） 1時間以内の単位　1ケ月以上
// 1日以内の単位　13ヶ月以上
//
//  ● 蓄積データ：
// ・1時間単位の積算電力量、最大2年間
// 測定データ項目・測定間隔：
// ・電力（Ｗ）5秒間隔　積算電力量（Ｗｈ）5秒間隔
//
// 30分以内(1ヶ月過ぎた分は消去)と一日以内(無限?)のファイルは別にいる．
public class Logger {
	@SuppressWarnings("unused")
	private static final String TAG = Logger.class.getSimpleName();
	private final Logger self = this;
	
	
	protected final Context mContext;
	
	private static Logger sInstance = null;
	
	private static final String VERSION = "0.1";
	
	private static final String LABEL_VERSION = "version";
	private static final String LABEL_UNIXTIME = "unixtime";
	
	private static final String LABEL_NICKNAME = "nickname";
	private static final String LABEL_DEVICE_TYPE = "device_type";
	private static final String LABEL_PROTOCOL = "protocol";
	
	private static final String LABEL_ACCESS_TYPE = "access_type"; // set or get(& inform)
	private static final String LABEL_PROPERTY_NAME = "property_name";
	private static final String LABEL_PROPERTY_VALUE = "property_value";	
	private static final String LABEL_SUCCESS = "success";
	private static final String LABEL_MESSAGE = "message";
	
	public static final String ACCESS_TYPE_SET = "set";
	public static final String ACCESS_TYPE_GET = "get";
	
	
	//public static final String LABEL_USER = "user";
	
	
	private Logger(Context context) {
		mContext = context.getApplicationContext();
	}
	
	public static synchronized Logger getInstance(Context context) {
		if(sInstance == null) {
			sInstance = new Logger(context);
		}
		
		return sInstance;
	}


	public void watch(String nickname, String propertyName, long intervalMills, long delayMills) {
		// 定期的にgetする
		
	}
	public void watchEveryday(String nickname, String propertyName, long intervalMills, long delayMills) {
		// 定期的にgetする
		
	}
	
	public void thinOut() {
		
	}
	
	public synchronized void log(DeviceData data, DeviceInfo info, String accessType, DeviceProperty property) {
		LinkedHashMap<String, String> record = new LinkedHashMap<String, String>();
		Date date = new Date();
		record.put(LABEL_VERSION, VERSION);
		record.put(LABEL_UNIXTIME, Long.toString(date.getTime()));
		record.put(LABEL_NICKNAME, data.nickname);
		record.put(LABEL_DEVICE_TYPE, info.deviceType);
		record.put(LABEL_PROTOCOL, data.protocolName);
		record.put(LABEL_ACCESS_TYPE, accessType);
		record.put(LABEL_PROPERTY_NAME, property.name);
		record.put(LABEL_PROPERTY_VALUE, (property.value != null) ? property.value.toString() : "null");
		record.put(LABEL_SUCCESS, Boolean.toString(property.success));
		record.put(LABEL_MESSAGE, property.message!=null?property.message.toString():null);
	
		String fileName = getLogFileName(date);
		
		Log.v(TAG, "log file name:"+fileName);
		
		File file = getLogFile(fileName);
		try {
			LTSVWriter w = new LTSVWriter(new FileOutputStream(file, true));
			try {
				w.write(record);
			} catch (IOException e) {
				e.printStackTrace();
				w.close();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JSONArray queryLog(long beginning, long end) {
		
		JSONArray ret = new JSONArray();
		List<File> fileList = getLogFileList(beginning, end);
		
		if(beginning == -1) {
			beginning = 0;
		}
		
		if(end == -1) {
			end = System.currentTimeMillis();
		}
		for(File logFile : fileList) {
			List<LinkedHashMap<String, String>> list = null;

			LTSVReader reader = null;
			try {
				reader = new LTSVReader(new FileInputStream(logFile));
				list = reader.read();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(reader!=null) {
				reader.close();
			}
			if(list != null) {
				for(LinkedHashMap<String, String> data : list) {
					long unixtime = getUnixTime(data);
					if(unixtime >= beginning && unixtime <= end) {
						ret.put(convertLog(data));
					}
				}
			}
			
		}
		return ret;
	}
	
	public String logview(LinkedHashMap<String, String> data) {
		return null;
	}
	
	public SimpleDateFormat getLogFileNameFormat() {
		return new SimpleDateFormat("yyyy'_'MM'_'dd'.ltsv'", Locale.JAPAN);
	}
	
	public String getLogFileName(Date date) {

		SimpleDateFormat sdf = getLogFileNameFormat();
		String fileName = sdf.format(date);
		
		return fileName;
	}
	
	public File getLogDir() {

		File dir = new File(mContext.getFilesDir(), "log");
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	public File getLogFile(String fileName) {
		File dir = getLogDir();
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File ret = new File(dir, fileName);
		return ret;
	}
	
	public long getUnixTime(LinkedHashMap<String, String> data) {
		String version = data.get(LABEL_VERSION);
		if(version.equals("0.1")) {
			return Long.parseLong(data.get("unixtime"));
		}
		return -2;
	}
	
	public JSONObject convertLog(LinkedHashMap<String, String> data) {
		JSONObject ret = new JSONObject();
		String version = data.get(LABEL_VERSION);
		if(version.equals("0.1")) {
			try {
				ret.put(LABEL_UNIXTIME, data.get("unixtime"));
				ret.put(LABEL_NICKNAME, data.get("nickname"));
				ret.put(LABEL_DEVICE_TYPE, data.get("device_type"));
				ret.put(LABEL_PROTOCOL, data.get("protocol"));
				ret.put(LABEL_ACCESS_TYPE, data.get("access_type"));
				ret.put(LABEL_PROPERTY_NAME, data.get("property_name"));
				ret.put(LABEL_PROPERTY_VALUE, data.get("property_value"));
				ret.put(LABEL_SUCCESS, data.get("success"));
				ret.put(LABEL_MESSAGE, data.get("message"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	public List<File> getLogFileList(long beginning, long end) {
		File dir = getLogDir();
		
		File[] logFiles = dir.listFiles();
		Arrays.sort(logFiles);

		ArrayList<File> ret = new ArrayList<File>();
		if(beginning == -1) {
			beginning = 0;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(beginning);
			Calendar c1 = Calendar.getInstance();
			c1.clear();
			c1.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			beginning = c1.getTimeInMillis();
		}
		
		if(end == -1) {
			end = System.currentTimeMillis();
		}
		SimpleDateFormat sdf = getLogFileNameFormat();
		for(File f : logFiles) {
			try {
				long time = sdf.parse(f.getName()).getTime();
				if(time >= beginning && time <= end) {
					ret.add(f);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;
		
	}
}
