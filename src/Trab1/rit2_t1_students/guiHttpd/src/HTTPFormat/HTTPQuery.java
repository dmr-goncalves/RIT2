package HTTPFormat;

/**
 * Redes Integradas de Telecomunicações II MIEEC 2015/2016
 *
 * HTTPQuery.java
 *
 * Class that stores all information about a HTTP request Incomplete Version
 *
 */
import Project1.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

public class HTTPQuery {

    public String operation;
    public String url_txt;
    public String version;
    public String nome;
    public String valor;
    public char[] campoMensagem;
    public String campoMensagemS;
    public Properties param;
    public String text;
    private Log log;
    private String id_str;
    public int local_port;

    /**
     * Creates a new instance of HTTPQuery
     *
     * @param log
     * @param id
     * @param local_port
     */
    public HTTPQuery(Log log, String id, int local_port) {
        // initializes everything to null
        param = new Properties();
        this.log = log;
        this.id_str = id;
        this.local_port = local_port;

    }

    private void Log(boolean in_window, String s) {
        if (in_window) {
            log.Log(id_str + "  " + s);
        } else {
            System.out.print(id_str + "  " + s);
        }
    }

    /**
     * Parses a new HTTP query
     *
     * @param in
     * @param echo
     * @return
     * @throws java.io.IOException
     */
    public int parse_Query(BufferedReader bin, boolean echo) throws IOException {
        // Get first line
        String request = bin.readLine();  	// Reads the first line
        if (request == null) {
            if (echo) {
                Log(true, "Invalid request Connection closed\n");
            }
            return -1;
        }
        Log(true, "Request: " + request + "\n");
        StringTokenizer st = new StringTokenizer(request);
        if (st.countTokens() != 3) {
            return -1;  // Invalid request
        }
        operation = st.nextToken();    // USES HTTP syntax
        url_txt = st.nextToken();    // for requesting files
        version = st.nextToken();
        // It does not read the other header fields! 
        if (!operation.equals("HEAD")) {
            String cabecalho = bin.readLine();

            while (!cabecalho.equals("/r/n") && !cabecalho.equals("")) {
                nome = cabecalho.substring(0, cabecalho.indexOf(":")); //O que vem antes dos ":" ? nome do cabecalho
                valor = cabecalho.substring(cabecalho.indexOf(":") + 2);    //O que vem depois dos dois pontos e do espa?depois dos dois pontos ? valor do cabecalho       
                param.setProperty(nome, valor);
                Log(true, nome + ": " + valor + "\n");
                cabecalho = bin.readLine();
            }

            if (param.getProperty("Content-Length") != null && Integer.parseInt(param.getProperty("Content-Length")) > 0) {

                campoMensagem = new char[Integer.parseInt(param.getProperty("Content-Length"))];
                bin.read(campoMensagem);
                campoMensagemS = new String(campoMensagem);
                param.setProperty("CampoMensagem", campoMensagemS);

                if (campoMensagemS != null) {
                    //Log(true, campoMensagemS);
                }

            }
        }
        Log(true, "\r\n");
        return HTTPReplyCode.OK;
    }

}
