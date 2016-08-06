package Project1;

/**
 * Redes Integradas de Telecomunicações II MIEEC 2013/2014
 *
 * httpThread.java
 *
 * Class that handles client's requests. It must handle HTTP GET, HEAD and POST
 * client requests INCOMPLETE VERSION
 *
 */
import HTTPFormat.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class httpThread extends Thread {

    guiHttpd root;
    ServerSocket ss;
    Socket client;
    DateFormat httpformat;
    Properties fields;
    Properties cookies_prop;
    Properties cookies;
    Properties last_update;
    String last_upd;
    String Etag;

    /**
     * Creates a new instance of httpThread
     */
    public httpThread(guiHttpd root, ServerSocket ss, Socket client) {
        this.root = root;
        this.ss = ss;
        this.client = client;
        httpformat = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        setPriority(NORM_PRIORITY - 1);
        fields = new Properties();
        cookies_prop = new Properties();
        cookies = new Properties();
        last_update = new Properties();
    }

    /**
     * The type for unguessable files
     */
    String guessMime(String fn) {
        String lcname = fn.toLowerCase();
        int extenStartsAt = lcname.lastIndexOf('.');
        if (extenStartsAt < 0) {
            if (fn.equalsIgnoreCase("makefile")) {
                return "text/plain";
            }
            return "unknown/unknown";
        }
        String exten = lcname.substring(extenStartsAt);
        // System.out.println("Ext: "+exten);
        if (exten.equalsIgnoreCase(".htm")) {
            return "text/html";
        } else if (exten.equalsIgnoreCase(".html")) {
            return "text/html";
        } else if (exten.equalsIgnoreCase(".gif")) {
            return "image/gif";
        } else if (exten.equalsIgnoreCase(".jpg")) {
            return "image/jpeg";
        } else {
            return "application/octet-stream";
        }
    }

    public void Log(boolean in_window, String s) {
        if (in_window) {
            root.Log("" + client.getInetAddress().getHostAddress() + ";"
                    + client.getPort() + "  " + s);
        } else {
            System.out.print("" + client.getInetAddress().getHostAddress()
                    + ";" + client.getPort() + "  " + s);
        }
    }

    /**
     * Loads a class code into the VM
     */
    private JavaRESTAPI start_API(String name) {
        name = name.substring(name.lastIndexOf(java.io.File.separatorChar) + 1, name.length());
        JavaRESTAPI api = null;
        try {
            Class apiClass = Class.forName("Project1." + name);
            Object apiObject = apiClass.newInstance();
            api = (JavaRESTAPI) apiObject;
        } catch (ClassNotFoundException e) {
            System.err.println("API class not found:" + e);
        } catch (InstantiationException e) {
            System.err.println("API class instantiation:" + e);
        } catch (IllegalAccessException e) {
            System.err.println("API class access:" + e);
        }
        return api;
    }

    @Override
    public void run() {

        HTTPAnswer ans = null;   // HTTP answer object
        HTTPQuery receivedhttp = null; //HTTP request object
        PrintStream pout = null;
        int keepAlive = root.getKeepAlive();
        boolean persistent = false;
        int etag;

        JavaRESTAPI api = null;

        String campo, valCampo;
        try {

            InputStream in = client.getInputStream();
            BufferedReader bin = new BufferedReader(
                    new InputStreamReader(in, "8859_1"));
            OutputStream out = client.getOutputStream();
            pout = new PrintStream(out, false, "8859_1");

            do {
                //create an object to store the http request
                receivedhttp = new HTTPQuery(root, client.getInetAddress().getHostAddress() + ":"
                        + client.getPort(), root.server.getLocalPort());

                int httpcode = receivedhttp.parse_Query(bin, true); //reads the input http request

                // Prepares an answer object
                ans = new HTTPAnswer(root,
                        client.getInetAddress().getHostAddress() + ":" + client.getPort(),
                        guiHttpd.server_name + " - " + InetAddress.getLocalHost().getHostName() + "-" + root.server.getLocalPort());

                // Get file with contents
                if (receivedhttp.url_txt != null) {
                    String filename = root.getRaizHtml() + receivedhttp.url_txt + (receivedhttp.url_txt.equals("/") ? "index.htm" : "");

                    if (receivedhttp.url_txt.endsWith("rit2API") && receivedhttp.operation.equals("GET")) {
                        api = start_API("rit2API");

                        api.doGet(client, receivedhttp.param, cookies, ans, keepAlive);
                    }
                    if (receivedhttp.operation.equals("POST")) {
                        api = start_API("rit2API");
                        StringTokenizer st = new StringTokenizer(receivedhttp.param.getProperty("CampoMensagem"), "&");
                        while (st.hasMoreTokens()) {
                            StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
                            if (st2.countTokens() != 2) {
                                campo = st2.nextToken();
                                valCampo = "";
                            } else {
                                campo = st2.nextToken();
                                valCampo = st2.nextToken();
                            }
                            fields.setProperty(campo, valCampo);
                        }

                        if (receivedhttp.param.getProperty("CampoMensagem").contains("Submeter")) {
                            fields.setProperty("BotaoSubmeter", "Submeter");
                        } else if (receivedhttp.param.getProperty("CampoMensagem").contains("Apagar")) {
                            fields.setProperty("BotaoApagar", "Apagar");
                        }
                        if (receivedhttp.param.getProperty("Cookie") != null) {
                            //Por a lista de cookies recebida na variavel cookies
                            StringTokenizer st3 = new StringTokenizer(receivedhttp.param.getProperty("Cookie"), "; ");
                            while (st3.hasMoreTokens()) {
                                StringTokenizer st4 = new StringTokenizer(st3.nextToken(), "=");
                                if (st4.countTokens() != 2) {
                                    campo = st4.nextToken();
                                    valCampo = "";
                                } else {
                                    campo = st4.nextToken();
                                    valCampo = st4.nextToken();
                                }
                                cookies.setProperty(campo, valCampo);

                            }
                        }
                        api.doPost(client, receivedhttp.param, cookies, fields, ans, keepAlive, root.last_update);

                        root.last_update.setProperty(fields.getProperty("Grupo"), httpformat.format(new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK).parse(ans.param.getProperty("Date"))));

                    } else if (receivedhttp.operation.equals("HEAD")) {
                        ans.send_Answer(pout, true, true, false, true);
                    } else if (filename.contains("..") == false && filename.contains(".java") == false && filename.contains(".") == true) {
                        File f = new File(filename);

                        if (f.exists() && f.isFile()) {
                            // Define reply contents
                            ans.set_code(HTTPReplyCode.OK);
                            ans.set_version(receivedhttp.version);
                            ans.set_file(new File(filename), guessMime(filename));
                            DateFormat httpformat
                                    = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
                            httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
                            ans.param.setProperty("Last-Modified", httpformat.format(f.lastModified()));
                            ans.param.setProperty("Content-Length", Long.toString(f.length()));
                            try {
                                MessageDigest md = MessageDigest.getInstance("MD5");
                                FileInputStream fis = new FileInputStream(filename);
                                byte[] dataBytes = new byte[1024];

                                int nread = 0;

                                while ((nread = fis.read(dataBytes)) != -1) {
                                    md.update(dataBytes, 0, nread);
                                };

                                byte[] mdbytes = md.digest();

                                //convert the byte to hex format
                                StringBuffer sb = new StringBuffer("");
                                for (int i = 0; i < mdbytes.length; i++) {
                                    sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
                                    Etag = sb.toString();
                                }
                            } catch (NoSuchAlgorithmException na) {

                            }

                            ans.param.setProperty("ETag", Etag);
                            if (receivedhttp.version.equals("HTTP/1.1")) {
                                if (receivedhttp.param.getProperty("Connection").equals("keep-alive")) {
                                    ans.param.setProperty("Connection", "keep-alive");
                                    ans.param.setProperty("Keep-Alive", Integer.toString(keepAlive));
                                } else {
                                    ans.param.setProperty("Connection", "close");
                                }
                            }

                            ans.param.setProperty("Content-Type", "text/html; charset=ISO-8859-1");
                            ans.param.setProperty("Content-Encoding", "ISO-8859-1");
                            if (receivedhttp.param.getProperty("If-Modified-Since") != null) {
                                Date date = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK).parse(receivedhttp.param.getProperty("If-Modified-Since"));
                                long IfModifiedSince = date.getTime();
                                if (IfModifiedSince < f.lastModified()) {
                                    ans.send_Answer(pout, true, true, false, false);
                                } else {
                                    ans.send_Answer(pout, false, true, true, false);
                                }
                            } else if (receivedhttp.param.getProperty("If-None-Match") != null) {
                                if (receivedhttp.param.getProperty("If-None-Match").equals(Etag)) {
                                    ans.send_Answer(pout, true, true, false, false);
                                } else {
                                    ans.send_Answer(pout, false, true, true, false);
                                }

                            } else {
                                ans.send_Answer(pout, true, true, false, false);
                            }
                        } else {
                            ans.set_error(HTTPReplyCode.NOTFOUND, receivedhttp.version);
                            ans.send_Answer(pout, true, true, true, false);
                        }
                    } else {
                        ans.set_error(HTTPReplyCode.UNAUTHORIZED, receivedhttp.version);
                        ans.send_Answer(pout, true, true, true, false);
                    }
                    if (receivedhttp.version.equals("HTTP/1.1") && ans.param.getProperty("Connection") != null && ans.param.getProperty("Connection").equals("keep-alive") && keepAlive > 0) {
                        client.setSoTimeout(keepAlive);
                        persistent = true;
                    } else {
                        persistent = false;
                    }
                }
            } while (persistent);

        } catch (IOException e) {
            try {
                client.close();
            } catch (Exception ex) {
                // Ignore
                System.out.println("Error closing client" + ex);
            }
            if (root.active()) {
                System.out.println("I/O error " + e);
            }
        } catch (ParseException ex) {
            Logger.getLogger(httpThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                client.close();
            } catch (Exception e) {
                // Ignore
                System.out.println("Error closing client" + e);
            }
            root.thread_ended();

            Log(true, "Closed TCP connection\n");

        }
    }

}
