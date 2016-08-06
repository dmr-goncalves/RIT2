package Project1;

/**
 * Redes Integradas de Telecomunicações II
 * MIEEC 2011/2012
 *
 * javaECGI.java
 *
 * Abstract class that defines the interface that must be provided by a servlet.
 * It also provides some auxiliary functions that handle char convertion
 * INCOMPLETE VERSION
 *
 * Created on 13 de Fevereiro de 2011, 12:00
 * @author  Luis Bernardo
 */

import HTTPFormat.HTTPAnswer;
import HTTPFormat.HTTPReplyCode;
import java.net.Socket;
import java.util.Properties;

public abstract class JavaRESTAPI {

    
    /** Converts CGI string into Java string (ISO-8859-1) (removes formating codes) */
    public static String postString2string(String in_s) {
        if (in_s == null)
            return null;
        StringBuilder out_s= new StringBuilder();
        int i= 0;
        while (i<in_s.length()) {
            switch (in_s.charAt (i)) {
                case '%':   try {   // "%dd" - character code in hexadecimal
                                i++;
                                byte[] n= new byte[1];
                                n[0]= (byte)Integer.parseInt(in_s.substring(i, i+2), 16);
                                if (n[0] == -96)    // Patch for MSIE
                                    out_s.append (' ');
                                else
                                    out_s.append (new String(n, "ISO-8859-1"));
                                i++;    // Jumps first char
                            }
                            catch (Exception e) {
                                System.err.println("Error parging CGI string: "+e);
                                return null;
                            }
                            break;
                case '+':   out_s.append(' ');
                            break;
                default:    out_s.append(in_s.charAt (i));
            }
            i++;
        }
        //System.out.println("CGI2STR: '"+in_s+"' > '"+out_s+"'");
        return out_s.toString ();
    }
    
    /** Converts java string (ISO-8859-1) to HTML format */
    public static String String2htmlString(String in_s) {
        if (in_s == null)
            return null;
        StringBuilder out_s= new StringBuilder();
        for (int i= 0; i<in_s.length(); i++) {
            switch (in_s.charAt (i)) {
                case ' ': out_s.append("&nbsp;"); break;
/* For Linux
                case 'á': out_s.append("&aacute;"); break;
                case 'é': out_s.append("&eacute;"); break;
                case 'í': out_s.append("&iacute;"); break;
                case 'ó': out_s.append("&oacute;"); break;
                case 'ú': out_s.append("&uacute;"); break;
                //case '�?': out_s.append("&Aacute;"); break;
                case 'É': out_s.append("&Eacute;"); break;
                //case '�?': out_s.append("&Iacute;"); break;
                case 'Ó': out_s.append("&Oacute;"); break;
                case 'Ú': out_s.append("&Uacute;"); break;
                case 'à': out_s.append("&agrave;"); break;
                case 'À': out_s.append("&Agrave;"); break;
                case 'ã': out_s.append("&atilde;"); break;
                case 'õ': out_s.append("&otilde;"); break;
                case 'Ã': out_s.append("&Atilde;"); break;
                case 'Õ': out_s.append("&Otilde;"); break;
                case 'â': out_s.append("&acirc;"); break;
                case 'ê': out_s.append("&ecirc;"); break;
                case 'ô': out_s.append("&ocirc;"); break;
                case 'Â': out_s.append("&Acirc;"); break;
                case 'Ê': out_s.append("&Ecirc;"); break;
                case 'Ô': out_s.append("&Ocirc;"); break;
                case 'ç': out_s.append("&ccedil;"); break;
                case 'Ç': out_s.append("&Ccedil;"); break;
  */
/* For MacOS 
                case '�': out_s.append("&aacute;"); break;
                case '�': out_s.append("&eacute;"); break;
                case '�': out_s.append("&iacute;"); break;
                case '�': out_s.append("&oacute;"); break;
                case '�': out_s.append("&uacute;"); break;
                case '�': out_s.append("&Aacute;"); break;
                case '�': out_s.append("&Eacute;"); break;
                case '�': out_s.append("&Iacute;"); break;
                case '�': out_s.append("&Oacute;"); break;
                case '�': out_s.append("&Uacute;"); break;
                case '�': out_s.append("&agrave;"); break;
                case '�': out_s.append("&Agrave;"); break;
                case '�': out_s.append("&atilde;"); break;
                case '�': out_s.append("&otilde;"); break;
                case '�': out_s.append("&Atilde;"); break;
                case '�': out_s.append("&Otilde;"); break;
                case '�': out_s.append("&acirc;"); break;
                case '�': out_s.append("&ecirc;"); break;
                case '�': out_s.append("&ocirc;"); break;
                case '�': out_s.append("&Acirc;"); break;
                case '�': out_s.append("&Ecirc;"); break;
                case '�': out_s.append("&Ocirc;"); break;
                case 'c': out_s.append("&ccedil;"); break;
                case '�': out_s.append("&Ccedil;"); break;
 /* */
                default: out_s.append(in_s.charAt (i));
            }
        }
        //System.out.println("STR2HTML: '"+in_s+"' > '"+out_s+"'");
        return out_s.toString ();        
    }

    /** Convert CGI string (ISO-8859-1) to HTML format */
    public  static String postString2htmlString(String in_s) {
        return String2htmlString(postString2string(in_s));
    }
    
    /** Private method returns page with "Not Implemented" */
    private void not_implemented(HTTPAnswer reply, String method) {
        // Define html error page
        String txt= "<HTML>\n";
        txt=txt+"<HEAD><TITLE>Error - " + method + " not implemented\n</TITLE></HEAD>\n";
        txt= txt+ "<H1> Error - " + method + " not implemented </H1>\n";
        txt= txt+ "  by JavaECGI\n";
        txt= txt + "</HTML>\n";
        // Prepare reply code and header fields
        reply.set_text(txt);
        reply.set_code(HTTPReplyCode.NOTIMPLEMENTED);
        reply.set_version("HTTP/1.1");
    }
            
    /** Runs GET method */
    public boolean doGet(Socket s, Properties param, Properties cookies, HTTPAnswer reply, int KeepAlive) { 
        // By default returns - "not supported"
        System.out.println("default GET");
        not_implemented(reply, "GET");
        return true;
    }
            
    /** Runs POST method */
    public boolean doPost(Socket s, Properties in_param, Properties cookies, Properties fields, HTTPAnswer reply , int KeepAlive, Properties last_update) { 
        // By default returns - "not supported"
        System.out.println("default POST");
        not_implemented(reply, "POST");
        return true;
    }
        
}
