package Project1;

/**
 * Redes Integradas de Telecomunicações II MIEEC 2013/2014
 *
 * rit2ECGI.java
 *
 * Servlet Example - this class implements the javaEPOST interface. It manages a
 * list of groups, using Properties lists. The user receives the current list,
 * and may add or remove groups. INCOMPLETE VERSION
 */
import HTTPFormat.HTTPAnswer;
import HTTPFormat.HTTPReplyCode;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class rit2API extends JavaRESTAPI {

    private final String bdname = "grupos.txt";

    private groupDB db;
    private JavaRESTAPI javaRAPI;

    /**
     * Creates a new instance of rit2POST
     */
    public rit2API() {
        db = new groupDB(bdname);
    }

    /*
     * Functions that handle cookie related issues
     */
    // To be completed
    /**
     * Select a subset of 'k' number of a set of numbers raging from 1 to 'max'
     */
    private int[] draw_numbers(int max, int k) {
        int[] vec = new int[k];
        int j;

        Random rnd = new Random(System.currentTimeMillis());
        for (int i = 0; i < k; i++) {
            do {
                vec[i] = rnd.nextInt(max) + 1;
                for (j = 0; j < i; j++) {
                    if (vec[j] == vec[i]) {
                        break;
                    }
                }
            } while ((i != 0) && (j < i));
        }
        return vec;
    }

    /**
     * Selects the minimum number in the array
     */
    private int minimum(int[] vec, int max) {
        int min = max + 1, n = -1;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] < min) {
                n = i;
                min = vec[i];
            }
        }
        if (n == -1) {
            System.err.println("Internal error in API.minimum\n");
            return max + 1;
        }
        vec[n] = max + 1;  // Mark position as used
        return min;
    }

    /**
     * Prepares the rit2ECGI demo web page
     */
    private String make_testPage(String ip, int port, String tipo, String grupo, int n, String n1, String na1, String n2, String na2, String n3, String na3, boolean count, String lastUpdate) {
        // Draw "lucky" numbers
        int[] set1 = draw_numbers(50, 5);
        int[] set2 = draw_numbers(9, 2);

        // Prepare string html with web page
        String html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\r\n<html>\r\n<head>\r\n";
        html += "<div  style=\"background-color: rgb(225, 225, 234);\">\n ";
        html += "<meta content=\"text/html; charset=ISO-8859-1\" http-equiv=\"content-type\">\r\n";
        html += "<title>ritIIAPI</title>\r\n</head>\r\n<body style=\"margin:0px\" height:100%>";
        html += "<p align=\"center\"><img src=\"gifs/fctunl_big.gif\" align=\"middle\" height=\"94\" width=\"600\"></p>\r\n ";
        html += "<h1 align=\"center\"><font color=\"#800000\">RIT II</font> <font color=\"#800000\">2015/2016</font></h1>\r\n ";
        html += "<h3 align=\"center\">1&ordm; Trabalho de laborat&oacute;rio</h3>";
        html += "<p align=\"left\">Ligou a partir de <font color=\"#ff0000\">" + ip + "</font>:";
        html += "<font color=\"#ff0000\">" + port + "</font> num browser do tipo <font color=\"#ff0000\">" + tipo + "</font>.</p>\r\n";
        if (n >= 0) {
            html += "<p align=\"left\">Os elementos do grupo <font color=\"#0000ff\">" + (grupo.length() > 0 ? (grupo) : "?") + "</font> j&aacute; actualizaram ";
            html += "<font color=\"#0000ff\">" + n + "</font> vezes o grupo no servidor.</p>\r\n";
            html += "<p align=\"center\"><img src=\"gifs/trump.gif\" border=\"0\" height=\"144\" width=\"285\"></p>\r\n";
        }
        if (n >= 0) {
            html += "<p align=\"left\">O &uacute;ltimo acesso ao servidor por este utilizador foi em: "
                    + " <font color=\"#0000ff\">" + lastUpdate + "</font>.</p>\r\n";
        }

        html += "<form method=\"post\" action=\"rit2API\">\r\n<h3>\r\nDados do grupo</h3>";
        html += "<p>Grupo <input name=\"Grupo\" size=\"2\" type=\"text\""
                + (grupo.length() > 0 ? " value=\"" + grupo + "\"" : "") + "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num1\" size=\"5\" type=\"text\""
                + (n1.length() > 0 ? " value=" + n1 : "")
                + ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome1\" size=\"80\" type=\"text\""
                + (na1.length() > 0 ? " value=" + na1 : "")
                + "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num2\" size=\"5\" type=\"text\""
                + (n2.length() > 0 ? " value=" + n2 : "")
                + ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome2\" size=\"80\" type=\"text\""
                + (na2.length() > 0 ? " value=" + na2 : "")
                + "></p>\r\n";
        html += "<p>N&uacute;mero <input name=\"Num3\" size=\"5\" type=\"text\""
                + (n3.length() > 0 ? " value=" + n3 : "")
                + ">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Nome <input name=\"Nome3\" size=\"80\" type=\"text\""
                + (na3.length() > 0 ? " value=" + na3 : "")
                + "></p>\r\n";

        html += "<p><input name=\"Contador\"" + (count ? " checked=\"checked\"" : "") + " value=\"ON\" type=\"checkbox\">Contador</p>\r\n";
        html += "<p><input value=\"Submeter\" name=\"BotaoSubmeter\" type=\"submit\">";
        html += "<input value=\"Apagar\" name=\"BotaoApagar\" type=\"submit\">";
        html += "<input value=\"Limpar\" type=\"reset\" value=\"Reset\" name=\"BotaoLimpar\">";
        html += "</p>\r\n</form>\r\n";

        if (db.get_biggest_group() * 8.4 < 160) {
            html += "<div id=\"header\" style=\"width:+" + 160 + "px; background-color: rgb(179, 255, 179);\">";
        } else {
            html += "<div id=\"header\" style=\"width:+" + db.get_biggest_group() * 8.4 + "px; background-color: rgb(179, 255, 179);\">";
        }
        html += db.table_group_html();
        html += "</div>";
        html += "<h3>Um exemplo de cont&eacute;udo din&acirc;mico :-)</h3>";
        html += "<p align=\"left\">Se quiser deitar dinheiro fora, aqui v&atilde;o algumas sugest&otilde;es para ";
        html += "o pr&oacute;ximo <a href=\"https://www.jogossantacasa.pt/web/JogarEuromilhoes/?\">Euromilh&otilde;es</a>: ";
        for (int i = 0; i < 5; i++) {
            html += (i == 0 ? "" : " ") + "<font color=\"#00ff00\">" + minimum(set1, 50) + "</font>";
        }
        html += " + <font color=\"#800000\">" + minimum(set2, 9) + "</font> <font color=\"#800000\">" + minimum(set2, 9) + "</font></p>\r\n";
        html += "<p align=\"left\">&nbsp;</p>\r\n";
        html += "<p align=\"left\"><font face=\"Times New Roman\">&copy; 2015/2016</font></p>\r\n</div></body></html>";

        return html; // HTML page code
    }

    /**
     * Runs GET/HEAD method
     */
    @Override
    public boolean doGet(Socket s, Properties param, Properties cookies, HTTPAnswer ans, int KeepAlive) {
        try {
            OutputStream out = s.getOutputStream();
            PrintStream pout = new PrintStream(out, false, "8859_1");

            String group = "", nam1 = "", n1 = "", nam2 = "", n2 = "", nam3 = "", n3 = "", lastUpdate = "";
            int cnt = -1;
            /**
             * This part must check if the browser is sending the rit2Cookie If
             * it is, it must deliver a web page with the last group introduced
             * by the user Otherwise, the fields must be empty
             */

            // Don't forget to convert Names from ECGI format to HTML format before preparing the web page
            String aux = javaRAPI.postString2htmlString(nam1);
            if (aux != null) {
                nam1 = aux;
            }
            aux = javaRAPI.postString2htmlString(nam2);
            if (aux != null) {
                nam2 = aux;
            }
            aux = javaRAPI.postString2htmlString(nam3);
            if (aux != null) {
                nam3 = aux;
            }
            aux = javaRAPI.postString2htmlString(lastUpdate);
            if (aux != null) {
                lastUpdate = aux;
            }

            // Prepare html page
            String html = make_testPage(s.getInetAddress().getHostAddress(), s.getPort(), param.getProperty("User-Agent", "Indefinido"),
                    group, cnt, n1, nam1, n2, nam2, n3, nam3, false, lastUpdate);

            // Prepare answer
            ans.set_code(HTTPReplyCode.OK);
            ans.set_text(html);

            DateFormat httpformat
                    = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
            httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
            ans.param.setProperty("Content-Length", Integer.toString(html.length()));
            ans.param.setProperty("Content-Type", "text/html; charset=ISO-8859-1");
            ans.param.setProperty("Content-Encoding", "ISO-8859-1");
            ans.param.setProperty("Connection", param.getProperty("Connection"));

            if (ans.code.get_version().equals("HTTP/1.1")) {
                if (param.getProperty("Connection").equals("keep-alive")) {
                    ans.param.setProperty("Connection", "keep-alive");
                    ans.param.setProperty("Keep-Alive", Integer.toString(KeepAlive));
                } else {
                    ans.param.setProperty("Connection", "close");
                }
            }
            ans.send_Answer(pout, true, true, false, false);

        } catch (IOException r) {

        }
        return true;
    }

    /**
     * Runs POST method
     */
    @Override
    public boolean doPost(Socket s, Properties param, Properties cookies, Properties fields, HTTPAnswer ans, int KeepAlive, Properties last_update) {
        // Put POST implementation here
        try {
            OutputStream out = s.getOutputStream();
            PrintStream pout = new PrintStream(out, false, "8859_1");
            StringTokenizer st2 = null;
            String group = fields.getProperty("Grupo", "");
            String nam1 = fields.getProperty("Nome1", "");
            String n1 = fields.getProperty("Num1", "");
            String nam2 = fields.getProperty("Nome2", "");
            String n2 = fields.getProperty("Num2", "");
            String nam3 = fields.getProperty("Nome3", "");
            String n3 = fields.getProperty("Num3", "");
            boolean SubmitButton = (fields.getProperty("BotaoSubmeter") != null);
            boolean DeleteButton = (fields.getProperty("BotaoApagar") != null);
            String lastUpdate = "";


            /*System.err.println("Button: " + (SubmitButton ? "Submit" : "")
                    + (DeleteButton ? "Delete" : "") + "\n");*/
            int cnt = -1;
            if (fields.getProperty("Contador") != null) {
                if (cookies.getProperty(group) != null && fields.getProperty("Contador").equals("ON")) {
                    cnt = Integer.parseInt(cookies.getProperty(group));
                    cnt++;
                    Date dt = new Date();
                    long dtn = dt.getTime() + 3600000;
                    DateFormat httpformat
                            = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
                    httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));

                    ans.set_cookie(group + "=" + Integer.toString(cnt) + "; expires=" + httpformat.format(dtn));

                    if (last_update.getProperty(group) == null) {
                        lastUpdate = "";
                    } else {
                        lastUpdate = last_update.getProperty(group);
                    }

                }
                if (cookies.getProperty(group) == null && fields.getProperty("Contador").equals("ON")) {
                    ans.set_cookie(group + "=" + Integer.toString(0));
                    cnt++;

                }
            }

            if (SubmitButton) {
                db.store_group(group, DeleteButton, n1, nam1, n2, nam2, n3, nam3);
            } else if (DeleteButton) {
                db.remove_group(group);
            }

            // Don't forget to convert Names from CGI format to HTML format before preparing the web page
            String aux = javaRAPI.postString2htmlString(nam1);
            if (aux != null) {
                nam1 = aux;
            }
            aux = javaRAPI.postString2htmlString(nam2);
            if (aux != null) {
                nam2 = aux;
            }
            aux = javaRAPI.postString2htmlString(nam3);
            if (aux != null) {
                nam3 = aux;
            }
            aux = javaRAPI.postString2htmlString(lastUpdate);
            if (aux != null) {
                lastUpdate = aux;
            }

            // ...
            // Prepare html page
            String html = make_testPage(s.getInetAddress().getHostAddress(), s.getPort(), param.getProperty("User-Agent", "Indefinido"),
                    group, cnt, n1, nam1, n2, nam2, n3, nam3, (fields.getProperty("Contador") != null), lastUpdate);

            //   System.out.println(html);
            // Prepare answer
            ans.set_code(HTTPReplyCode.OK);
            ans.set_text(html);

            DateFormat httpformat
                    = new SimpleDateFormat("EE, d MMM yyyy HH:mm:ss zz", Locale.UK);
            httpformat.setTimeZone(TimeZone.getTimeZone("GMT"));
            ans.param.setProperty("Content-Length", Integer.toString(html.length()));
            ans.param.setProperty("Content-Type", "text/html; charset=ISO-8859-1");
            ans.param.setProperty("Content-Encoding", "ISO-8859-1");
            ans.param.setProperty("Connection", param.getProperty("Connection"));

            if (ans.code.get_version().equals("HTTP/1.1")) {
                if (param.getProperty("Connection").equals("keep-alive")) {
                    ans.param.setProperty("Connection", "keep-alive");
                    ans.param.setProperty("Keep-Alive", Integer.toString(KeepAlive));
                } else {
                    ans.param.setProperty("Connection", "close");
                }
            }

            //  if(param.getProperty("Cookie")!=null){
            // ans.param.setProperty("Cookie", param.getProperty("Cookie"));
            //}
            ans.send_Answer(pout, true, true, false, false);

        } catch (IOException r) {

        }
        return true;
    }

}
