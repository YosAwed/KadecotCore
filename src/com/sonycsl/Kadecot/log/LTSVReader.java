package com.sonycsl.Kadecot.log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class LTSVReader {
	@SuppressWarnings("unused")
	private static final String TAG = LTSVReader.class.getSimpleName();
	private final LTSVReader self = this;

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	protected BufferedReader mReader;
	
	
	public LTSVReader(InputStream is) {
		mReader = new BufferedReader(new InputStreamReader(is));
	}
	
	
	public List<LinkedHashMap<String, String>> read() throws IOException {
		
		List<LinkedHashMap<String, String>> ret = new ArrayList<LinkedHashMap<String, String>>();
		String line;
		while ((line = mReader.readLine()) != null) {
			//2番目の引数に0:後続の空の文字列は破棄される
			String[] fields = line.split("\t", 0);
			LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
			for(String f : fields) {
				String[] map = f.split(":", 2);
				if(map.length != 2) {
					continue;
				}
				data.put(map[0], map[1]);
			}
			ret.add(data);
		}
		
		return ret;
	}
	
	public void close() {
		try {
			mReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
