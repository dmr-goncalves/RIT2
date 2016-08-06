/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Project1;

import static Project1.guiHttpd.server_name;
import java.net.ServerSocket;
/**
 *   /** HTTP server thread 
 * @author pedroamaral */
 

class SHttpd extends Thread {
        guiHttpd root;
        ServerSocket ss;
        volatile boolean active;
        
        SHttpd ( guiHttpd root, ServerSocket ss ) {
            this.root= root;
            this.ss= ss;
        }
        
        public void wake_up () {
            this.interrupt ();
        }
        
        public void stop_thread () {
            active= false;
            this.interrupt ();
        }
        
        @Override
        public void run () {
            System.out.println (
                    "\n******************** "+server_name+" started ********************\n");
            active= true;
            while ( active ) {
                try {
                    httpThread conn = new httpThread ( root, ss, ss.accept () );
                    conn.start ( );
                    root.thread_started ();
                } catch (java.io.IOException e) {
                    root.Log ("IO exception: "+ e + "\n");
                    active= false;
                }
            }
        }
} // end of class SHttpd
