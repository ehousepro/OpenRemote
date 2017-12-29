

/**
 * eHouse for OpenRemote.Org (OR) Server Integration
 * Data Export for OpenRemote.Org
 * Direct import to beehive database of OR
 * author: Robert Jarzabek / iSys.Pl
 * 
 * http://www.isys.pl/      -   strona domowa producenta systemu eHouse
 * http://home-automation.isys.pl/  - eHouse producer & manufacturer home page 
 * http://www.ehouse.pro/ - 
 * http://inteligentny-dom.ehouse.pro/  - inteligentny dom eHouse przyklady, projekty, zrob to sam, programowanie, projektowanie, przyklady
 * http://home-automation.ehouse.pro/   - eHouse home automation DIY, programming, designing, tips&trips, examples of usage
 * http://forum.eHouse.pro/
 * MAIN
 */
package ehouse4openremote;
import java.io.BufferedWriter;
import java.net.DatagramSocket;
import java.net.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
/*import org.openremote.controller.OpenRemoteRuntime;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.bootstrap.Startup;
*/
public class EHouse4OpenRemote {
static boolean firsttime=true;                      //first time run for initialization flag
static ServerSocket OpenRemoteServerSocket=null;    //Main Mutli-thread Server socket connection for OR sensors (status queries,commands)
/**
 * logging strings - shortcut to ehousecommunication.l()
 * @param line - string to append to log
 */
static void l(String line)
    {
    ehousecommunication.l(line);
    }

/**
 * UDP Listener for capturing status from eHouse Controllers
 * @throws IOException 
 */
static void eUDPmon() throws IOException {
        new UDPMonitor();
    }
/**
 * TCP server for OpenRemote requests
 * @throws IOException 
 */
static void eTCPSrv() throws IOException {
        new TCPStatusServer();
    }
/**
 * Main function 
 * @param args - 
 *              export - for beehive database initialization and update status of controllers to the java server
 * @throws IOException 
 */
public static void main(String[] args) throws IOException 
{
    try
        {
            new File(ehousecommunication.path+"logs/").mkdirs();
        }     
    catch (Exception e)    
        {
            
        }
 l("");
 l("*> Starting eHouse4openremote <*");
 l("");

if (args.length>0)        
    for (int i=0;i<args.length;i++)
            {
            if (args[i].toLowerCase().equals("export"))                        
                ehousecommunication.ExportOpenRemoteDesignerObjects=true; //initialize Beehive Database restore / export
            }
        EhouseTCP.StatusServerEnable=true;              //Enable OpenRemote Server
        EhouseTCP.UDPInit=true;                         //UDP LISTENER of ehouse system init
        EhouseTCP.UDPQueryPriority=Thread.MIN_PRIORITY; //udp listener thread set priority
        EhouseTCP.UDPEnabled=true;                      //Enable udp listener
        ehousecommunication.init();                     //initialize program / set main configuration
        
      try  //clearing Transaction log on application start
        {
        new File(ehousecommunication.path+"logs/").mkdirs();
        FileWriter fstream = new FileWriter(ehousecommunication.path+"logs/"+"TransactionLog.txt");   
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("Starting eHouse4OpenRemote Sever\r\n");
        out.close();
        
        }
      catch (Exception e)
        {   //Catch exception if any
        ehousecommunication.ll(e);
        //System.err.println("Error: " + e.getMessage());
        }    
    OpenRemoteExport.Test();            //perform test functions for developping
    if (ehousecommunication.ExportOpenRemoteDesignerObjects) // perform ehouse configuration, object, sensors, signals - export to OpenRemote.Org beehive database
            {
            l("[SQL] Export start");
            l("[SQL] Beehive DB Connect");
            OpenRemoteExport.ConnectDB("Beehive");
            l("[SQL] Cleaning devices database (script restoredb.sql)");
            OpenRemoteExport.runscript(ehousecommunication.locpath+"restoredb.sql");    //initial script for recreation and clearing database
            l("[SQL] Export completed");
            try     //removing all sql transaction log file
                {
                File del= new File(ehousecommunication.path+"logs/"+"TransactionLog-sql-export.txt");          //clear sql log
                del.delete();
                File f = new File(ehousecommunication.path+"logs/"+"TransactionLog.txt"); // backup of this source file.
                f.renameTo(new File(ehousecommunication.path+"logs/"+"TransactionLog-sql-export.txt"));
                }
            finally
                {
         
                }
    }

    try
        {
        EhouseTCP.AllDevsTCPStatus();               //load all Ethernet Ehouse Devs cache config
        }
    catch (Exception e)
        {
        l("[startup] Error while reading Ethernet eHouse device list:"+e.getMessage()+" "+"\r\n");
        ehousecommunication.ll(e);
        }
    try{
        EhouseTCP.AllDevsStatus();      //load all Ehouse1 Devs cache Config
        }
    catch (Exception e)
        {
        l("[startup] Error while reading eHouse 1 device list:"+e.getMessage());
        ehousecommunication.ll(e);
        }
      

     try
        {
        eUDPmon();                  //start udp listener
        }
     catch (Exception e)
        {
        ehousecommunication.ll(e);
        }
    
        firsttime=false;            //aplication was successfully initialized
        
     try
        {
            eTCPSrv();              //start OpenRemote requests server
        }
     catch (Exception e)   
        {
        ehousecommunication.ll(e);
        }
     
}    
}

