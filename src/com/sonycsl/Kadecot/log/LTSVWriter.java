
package com.sonycsl.Kadecot.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;

public class LTSVWriter {
    @SuppressWarnings("unused")
    private static final String TAG = LTSVWriter.class.getSimpleName();

    private final LTSVWriter self = this;

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    protected BufferedWriter mWriter;

    public LTSVWriter(OutputStream os) {
        mWriter = new BufferedWriter(new OutputStreamWriter(os));
    }

    public void write(LinkedHashMap<String, String> data) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String label : data.keySet()) {
            sb.append(label);
            sb.append(":");
            sb.append(data.get(label));
            sb.append("\t");
        }
        sb.append(LINE_SEPARATOR);
        String str = new String(sb);
        mWriter.write(str);
        mWriter.flush();
    }

    public void close() {
        try {
            mWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
