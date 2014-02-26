
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
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class is base class of servers.
public abstract class HTTPServer {
    // private static Server instance;
    private boolean isRunning = false;

    protected int runningPort = -1;

    private static ServerSocket socket;

    private String access_origin = "";

    private ArrayList<HttpGet> httpGetList = new ArrayList<HttpGet>();

    protected HTTPServer(String access_origin) {
        this.access_origin = access_origin;
    }

    // add httpget and so on.
    // abstract protected void initialize();

    // abstract protected Server getInstance();

    protected void addHttpGet(HttpGet hg) {
        httpGetList.add(hg);
    }

    private void service() throws IOException {
        for (Socket sock = accept(); sock != null; sock = accept()) {
            // why single thread?
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new HttpServerService(sock));
        }
    }

    private Socket accept() throws IOException {
        try {
            return socket.accept();
        } catch (SocketException e) {
        }
        return null;
    }

    public void start(int port) throws IOException {
        if (isRunning())
            return;
        socket = new ServerSocket();
        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(port));
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    service();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    stop();
                }
            }
        });
        runningPort = port;
        isRunning = true;
    }

    public void stop() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isRunning = false;
            runningPort = -1;
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    class Request {
        public String method;

        public String path = null;

        // public Map<String, String> params;
        public Map<String, String> query;

        public List<String> requests;

        Pattern REQUEST_LINE_PATTERN = Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");

        public Request(InputStream in) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            query = new HashMap<String, String>();
            requests = new ArrayList<String>();
            String line;
            try {
                line = reader.readLine();
                while (line != null && line.length() != 0) {
                    // System.out.println(line);
                    requests.add(line);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (requests.size() < 1)
                return;
            line = requests.get(0);
            Matcher m = REQUEST_LINE_PATTERN.matcher(line);
            if (m.matches()) {
                method = m.group(1);
                String p = m.group(2);
                String[] ss = p.split("\\?");
                path = ss[0];
                if (ss.length == 1) {
                    return;
                } else {
                    String q = p.substring(path.length() + 1);
                    ss = q.split("&");
                    for (String s : ss) {
                        String[] r = s.split("=");
                        if (r.length != 2)
                            continue;
                        query.put(r[0], r[1]);
                    }
                }
            } else {
                return;
            }
        }
    }

    class Response {
        private OutputStream out;

        private String access_origin = "";

        public Response(OutputStream out, String access_origin) {
            this.out = out;
            this.access_origin = access_origin;
        }

        public Response(OutputStream out) {
            this(out, "");
        }

        public void success(String type, String content) {
            // System.out.println("success");

            PrintWriter writer = new PrintWriter(out);
            writer.print("HTTP/1.1 200 OK\r\n");
            writer.print("Connection: close\r\n");
            writer.print("ETag: \"" + UUID.randomUUID().toString().replaceAll("-", "") + "\"\r\n");
            if (access_origin != "") {
                writer.print("Access-Control-Allow-Origin: " + access_origin + "\r\n");
            }
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
            // System.out.println("failure");
            PrintWriter writer = new PrintWriter(out);
            writer.print("HTTP/1.0 404 Not Found\r\n");
            writer.print("Connection: close\r\n");
            writer.print("ETag: \"" + UUID.randomUUID().toString().replaceAll("-", "") + "\"\r\n");
            if (access_origin != "") {
                writer.print("Access-Control-Allow-Origin: " + access_origin + "\r\n");
            }
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
            failure("text", "Not Found");
        }
    }

    abstract class HttpGet {
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

    private class HttpServerService implements Runnable {
        Socket socket;

        HttpServerService(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            OutputStream out = null;
            InputStream in = null;
            try {
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
            try {
                in = socket.getInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            try {
                Request req = new Request(in);
                Response res = new Response(out, access_origin);

                if (req.path == null) {
                    res.sendNotFound();
                    return;
                }

                for (HttpGet httpGet : httpGetList) {
                    if (httpGet.match(req.path)) {
                        httpGet.run(req, res);
                        return;
                    }
                }
                res.sendNotFound();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static String urldecode(String s) {
        if (s == null)
            return null;
        try {
            return URLDecoder.decode(s, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }
}
