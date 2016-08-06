package HTTPFormat;

/**
 * Redes Integradas de Telecomunicações II MIEEC 2015/2016
 *
 * HTTPAnswer.java
 *
 * Class that stores all information about a HTTP reply INCOMPLETE VERSION
 *
 */
import Project1.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class HTTPAnswer {

    /**
     * Reply code information
     */
    public HTTPReplyCode code;
    /**
     * Reply headers data
     */
    public Properties param;                // Header fields
    public String set_cookies;   // Set cookies header fields
    /**
     * Reply contents They are stored either in a text buffer or in a file
     */
    public String text; // buffer with reply contents 
    public File file;   // file used if text == null   
    Log log;
    String id_str;  // Thread id - for logging purposes

    /**
     * Creates a new instance of HTTPAnswer
     */
    public HTTPAnswer(Log log, String id_str, String server_name) {
        this.code = new HTTPReplyCode();
        this.id_str = id_str;
        this.log = log;
        param = new Properties();
        set_cookies = null;
        text = null;
        file = null;
        /**
         * define Server header field name
         */
        param.setProperty("Server", server_name);
    }

    void Log(boolean in_window, String s) {
        if (in_window) {
            log.Log(id_str + "  " + s);
        } else {
            System.out.print(id_str + "  " + s);
        }
    }

    public void set_code(int _code) {
        this.code.set_code(_code);
    }

    public void set_version(String v) {
        code.set_version(v);
    }

    public void set_property(String name, String value) {
        param.setProperty(name, value);
    }

    public void set_cookie(String setcookie_line) {
        set_cookies = setcookie_line;
    }

    /**
     * Sets the reply contents with file content
     *
     * @param _f
     * @param mime_enc
     */
    public void set_file(File _f, String mime_enc) {
        file = _f;
        // header lines not set in 'param'!
        // ...
     }

    /**
     * Sets the reply text contents of type HTML
     *
     * @param _text
     */
    public void set_text(String _text) {
        text = _text;
        // header lines not set in 'param'!
        // ...
        }

    /**
     * Returns the answer code
     *
     * @return
     */
    public int get_code() {
        return code.get_code();
    }

    /**
     * Returns a string with the first line of the answer
     *
     * @return
     */
    public String get_first_line() {
        return code.toString();
    }

    /**
     * Returns an iterator over all header names
     *
     * @return
     */
    public Iterator<Object> get_Iterator_parameter_names() {
        return param.keySet().iterator();
    }

    /**
     * Returns the array list with all set_cookies
     *
     * @return
     */
    public String get_set_cookies() {
        return set_cookies;
    }

    /**
     * Sets the "Date" header field with the local date in HTTP format
     */
    void set_Date() {
        DateFormat httpformat
                = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
        httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        param.setProperty("Date", httpformat.format(new Date()));
    }

    /**
     * Prepares an HTTP answer with an error code
     *
     * @param _code
     * @param version
     */
    public void set_error(int _code, String version) {
        set_version(version);
        set_Date();
        code.set_code(_code);
        if (code.get_code_txt() == null) {
            code.set_code(HTTPReplyCode.BADREQ);
        }

        if (!version.equalsIgnoreCase("HTTP/1.0")) {
            param.setProperty("Connection", "close");
        }
        // Prepares a web page with an error description
        String txt = "<HTML>\r\n";
        txt = txt + "<HEAD><TITLE>Error " + code.get_code() + " -- " + code.get_code_txt()
                + "</TITLE></HEAD>\r\n";
        txt = txt + "<H1> Error " + code.get_code() + " : " + code.get_code_txt() + " </H1>\r\n";
        txt = txt + "  by " + param.getProperty("Server") + "\r\n";
        txt = txt + "</HTML>\r\n";

        // Set the header properties
        set_text(txt);
    }

    /**
     * Sends the HTTP reply to the client using 'pout' text device
     *
     * @param pout
     * @param send_data
     * @param echo
     * @throws java.io.IOException
     */
    public void send_Answer(PrintStream pout, boolean send_data, boolean echo, boolean error, boolean head) throws IOException {
        set_Date();
        if (code.get_code_txt() == null) {
            code.set_code(HTTPReplyCode.BADREQ);
        }
        if (echo) {
            Log(true, "Answer: " + code.toString() + "\n");
            Log(true, "Date: " + param.getProperty("Date") + "\n");
            Log(true, "Server: " + param.getProperty("Server") + "\n");
            Log(true, "Last-Modified: " + param.getProperty("Last-Modified") + "\n");
            Log(true, "ETag: " + param.getProperty("ETag") + "\n");
            Log(true, "Content-Length: " + param.getProperty("Content-Length") + "\n");

            if (code.get_version().equals("HTTP/1.1")) {
                Log(true, "Connection: " + param.getProperty("Connection") + "\n");
                if (param.getProperty("Connection").equals("keep-alive")) {
                    Log(true, "Keep-Alive: " + param.getProperty("Keep-Alive") + "\n");
                }
            }
            Log(true, "Content-Type: " + param.getProperty("Content-Type") + "\n");
            Log(true, "Content-Encoding: " + param.getProperty("Content-Encoding") + "\n");
            Log(true, "Set-Cookie: " + set_cookies + "\n");
        }
        pout.print(code.toString() + "\r\n");

        /**
         * Send all headers except set_cookie
         */
        // ...
        if (error == false && head == false) {
            pout.printf("Date: %s\r\n", param.getProperty("Date"));
            pout.printf("Server: %s\r\n", param.getProperty("Server"));
            pout.printf("Last-Modified: %s\r\n", param.getProperty("Last-Modified"));
            pout.printf("ETag: %s\r\n", param.getProperty("ETag"));
            pout.printf("Content-Length: %s\r\n", param.getProperty("Content-Length"));
            if (code.get_version().equals("HTTP/1.1")) {
                pout.printf("Connection: %s\r\n", param.getProperty("Connection"));
                if (param.getProperty("Connection").equals("keep-alive")) {
                    pout.printf("Keep-Alive: %s\r\n", param.getProperty("Keep-Alive"));
                }
            }
            pout.printf("Content-Type: %s\r\n", param.getProperty("Content-Type"));
            pout.printf("Content-Encoding: %s\r\n", param.getProperty("Content-Encoding"));
            
            if(set_cookies != null)
            pout.printf("Set-Cookie: %s\r\n", set_cookies);
                
        }
        /**
         * Send set_cookie
         */
        // ...
        pout.print("\r\n");

        if (send_data) {

            if (text != null) {
                pout.print(text);
            } else if (file != null) {
                FileInputStream fin = new FileInputStream(file);
                if (fin == null) {
                    Log(true, "Internal error sending answer data\n");
                    return;
                }

                byte[] data = new byte[1024];
                int count = 0;

                while ((count = fin.read(data)) > -1) {
                    pout.write(data, 0, count);
                }

                fin.close();
            } else if (code.get_code() != HTTPReplyCode.NOTMODIFIED) {
                Log(true, "Internal server error sending answer\n");
            }
        }

        pout.flush();
        if (echo) {
            Log(false, "\n");
        }
    }
}
