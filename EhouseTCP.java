package ehouse4openremote;
//ONLY ONE COMMMANAGER CAN BE CONFIGURED
/**
 * @author Robert Jarzabek 
 * http://www.isys.pl/
 * http://inteligentny-dom.ehouse.pro/
 * http://www.ehouse.pro/
 * http://sterowanie.biz/
 * 
 * TCP Initialization
 * Query  perform
*/

import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.*;
import java.io.File;
import java.util.Date;
public class EhouseTCP
    {
//WifiManager wifi;
/**
 * initial priority of thread for TCP client connection
 */
final static int EventLengtMax = 250;
static int TCPQueryPriority=Thread.MIN_PRIORITY;
/**
 * initial priority of thread for udp client connection
 */
static int UDPQueryPriority=Thread.MIN_PRIORITY;
/**
 * speech synthesizer thread priority
 */

static int StatusServerPriority=Thread.//NORM_PRIORITY;
                                MIN_PRIORITY;
static byte[] message=new byte[1500];
static int CurrentORSensorConnection=0;
//blocking unblocking some options
/**
 * udp flag
 */
static boolean UDPInit=true;
/**
 * tcp flag
 */
static boolean StatusServerEnable=true;
ServerSocket providerSocket;
static Date lastupdate = new Date();
static final int MAX_OR_SERVER_SOCKETS=1000;
//static Socket[] CliSocket=new Socket[MAX_OR_SERVER_SOCKETS];
/**
 * udp socket
 */
static DatagramSocket ds=null;
/**
 * UDP reception initiated
 */
public static boolean UDPStarted=false; 
/**
 * Enable UDP Reception
 */
public static boolean UDPEnabled=false;
/**
 * Udp port nr for reception
 */
public static int UDPPort=6789;          
public static boolean Dynamic = false;
/**
 * loads settings of devices from files (statusehouse, statuscommmanager)
 */
public static boolean EhouseTCPDevicesInitiated=false; 
public static boolean EhouseProDevicesInitiated=false; 
public static boolean ViaCommManager=true;
//static public boolean Offline=true;
public static String sep=System.getProperty("file.separator");
static public InetAddress broadcastinet=null;
/**
 * tcp connection for query controller status is active
 */
    static boolean QueryConnected=false;
    static boolean chchch=false;
    /**
     * wifi submit success flag
     */
    static boolean WiFiSendOk=true;      
    /**
     * Client Connection
     */
    public static Socket ehouseconnection=null;  
    /**
     * output stream connection
     */
    public static OutputStream ehouseout=null;            
    /**
     * input stream connection
     */
    public static InputStream ehousein=null;              
    public static BufferedReader ehouseinput=null;
//    static Thread RunQuery=null;
    /**
     * additional size of query for ehouse over communication module
     * 0 for direct ehouse connection
     */
    public static int QueryOffset = 2;
    /**
     * changed ADC measurement flag
     */                                 
    public static boolean AdcChanged=false;
    /**
     * checking for init all graphic matrix temp, temps, other
     */
    static private boolean initiatedgraphicobjects=false;    
    static public int iiter=0;
    /**
     * enabled TCP query status
     */
    public static boolean TCPPanelEnabled = false;
    /**
     * Wifi / internet  configuration - internet uses as well ddns service support
     */
    //public static Boolean EnabledWan = false;
    public static String statustemp = "";
    /**
     * first flag for application init
     */
    static public boolean OnlyTheFirst = false;
        static Byte[] EventList = new Byte[256];
        static int EventSize = 0;
        static Byte ClientOpts = 0;
        static int WiFiSendTimeOut = 0;        
        final static int WiFiSENDTIMEOUT=3;
        static byte[] QueryBuff = new byte[10000];
        final static int MAX_TOTDEVS_COUNT=30;
        static int DevNameIndex=0;
        static int Status = 0;
        public static int QueryStatus = 0;
        static boolean Changed = false;
        public static int ProgressIndicator = 0;
        public static int QueryProgressIndicator = 0;
        /**
         * Devices auto views
         */
        public static String[] DevViewNames=new String[MAX_TOTDEVS_COUNT];
//        private static int[] HashTable = new int[15];		//finally converted
        /**
         * terminate send for kill application, threads or cancel
         */
        public static boolean TerminateSend = false;
        /**
         * terminate infinite query reception over tcp ip
         */
        public static boolean QueryTerminateSend = false; 
        /**
         * tcp ip client current status
         */
        public final static int CLIENT_BSD_CONNECT = 3;
        public final static int CLIENT_BSD_RECEIVE_CHALANGE = 4;
        public final static int CLIENT_BSD_RECEIVE_PROMPT = 5;
        public final static int CLIENT_BSD_RECEIVE_PROMPT_AFTER_SIZE = 6;
        public final static int CLIENT_BSD_RECEIVE_COMPLETE_AFTER_DATA = 7;
        public static boolean EhouseDevicesInitiated = false; //
        //#define	    CLIENT_BSD_SEND 
        public final static int CLIENT_BSD_OPERATION = 8;
        public final static int CLIENT_BSD_CLOSE = 9;
        public final static int CLIENT_BSD_DONE = 10;
        public final static int CLIENT_BSD_REPORT = 11;
        public final static int CLIENT_BSD_REPORT_END = 12;
        public static int BSDClientState = 0;
        //static Socket EhTCP=null;
        //////////////////////////////////////////////////////////////////////////////////////////////////
        static int[] response = new int[100];
        final static int EVENT_SIZE = 10;
        /**
         * max nr of ehouse 1 devs (RMs, EM, HM)
         */
        final static int MAX_SIZE_OF_EHOUSE_DEVS=16;
        /**
         * max size of ehouse ethernet devs (commmanager, levelmanager, ethernetroommanager)
         */
		final static int MAX_SIZE_OF_EHOUSE_DEVS_WIFI = 30;
        final static int MAX_SIZE_OF_EHOUSE_DEVS_TCP = 30+MAX_SIZE_OF_EHOUSE_DEVS_WIFI;
        final static int MAX_SIZE_OF_EHOUSE_PRO=2;
//        final 
        /**
         * caching arrays for store ehouse 1 devs status, names, calculated results, visualization objects, etc
         */
        static StatusEhouse[] EhDev=new StatusEhouse[MAX_SIZE_OF_EHOUSE_DEVS];
        /**
         * caching arrays for store ethernet ehouse  devs status, names, calculated results, visualization objects
         */
        static StatusEthernet[] EhDevTCP = new StatusEthernet[MAX_SIZE_OF_EHOUSE_DEVS_TCP]; 
        static StatusPro[] EhPro=new StatusPro[MAX_SIZE_OF_EHOUSE_PRO];
        /**
         * heat manager status strings
         */ 
        static String HEATER_KOMSTAT = "";  
        static String HEATER_RECUPERATOR_MODE = "";
        static String HEATER_RECUPERATOR_SPEED = "";
        static String HEATER_RECUPERATOR_TEMP = "";
        static String HEATER_PROGRAM = "";
        /**
         * external manager or commmanager security zone nr and name
         */
        static String EXTERNAL_CURRENTZONE = "";
        static String EXTERNAL_CURRENTZONENAME = "";
        /**
         * external manager or commmanager security programm nr and name
         */
        static String External_currentprogram = "";
        static String External_currentprogramname = "";
        /**
         * external manger device name
         */ 
        public static String ExternalName = "";
        /**
         * heatmanager device name
         */ 
        public static String HeatName = "";
        /**
         * count of RM,EM,HM
         */
        static int RM_COUNT=0;
        /**
         * count of CM,ERM,LM
         */
        static int TCP_COUNT=0;
//////////////////////////////////////////////////////////////////////////////////////////////////
/** Add to local log
    Dodaje string do logu     */        
        public static void l(String s)
            { 
            ehousecommunication.l(s);
            }
////////////////////////////////////////////////////////////////////////////////////////////////        
/////////////////////////////////////////////////////////////////////////////////////
/**
 * first time setup flag
 * 
 * @return 
 */       
        public boolean OnlyFirst()
        {
            if (OnlyTheFirst)
            {
                OnlyTheFirst = false;
                return true;
            }
            else return false;
        }
//////////////////////////////////////////////////////////////////////////////////////////////////////        
/**
 * Check if Adc measurement was changed from previous query
 * 
 * @return 
 */        
        
   public boolean IsAdcChanged()
    {
        if ((AdcChanged) && (OnlyTheFirst))
        {
            OnlyTheFirst = false;
            AdcChanged = false;
            return true;
        }
        return false;
    }
/////////////////////////////////////////////////////////////////////////////////////////////        
/**
 * Get Size of Eventque keep it less then 70
 * 
 * @return 
 */
        static public int GetEventsCount()
        {
            
            return EventSize;
        }
////////////////////////////////////////////////////////////////////////
/**
 * return string representation of adc value  
 * devindex - index of rm table
 * opts - type of measurement (percent, absolute, light level, temp, invert percent, and other for future use)        
 * nr - index of sensor for device
 * 
 * @param devindex
 * @param opts
 * @param nr
 * @return 
 */
        static public String Replacement(int devindex, int opts, int nr)
        {
            if (devindex >= MAX_SIZE_OF_EHOUSE_DEVS) return "";
            if (opts > 3) return "";
            if (nr >= 16) return "";
            switch (opts)
                {
                case 0:
                    return String.valueOf(EhDev[devindex].SensorABSValues[nr]); //absolute values 0..1023
                case 1:
                    return String.valueOf(EhDev[devindex].SensorTemps[nr]);     //calculate as temperature in degrees
                case 2:
                    return String.valueOf(EhDev[devindex].SensorLights[nr]);    //calculate as light level (inverted scale linear)
                case 3:
                    return String.valueOf(EhDev[devindex].SensorPercents[nr]);  //calculate as percent value comparing to power device supplly
                case 4:
                    //return String.valueOf(EhDev[devindex].SensorVolt[nr]);
                //case 5:
                    return String.valueOf(EhDev[devindex].SensorPercents[nr]);   //volty zmienic w razie potrzeby
                }
            return "";
        }
//////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * wifi send timeout decrement of sending event via WiFi or internet
 * 
 * @return 
 */       
static public int DecWiFiTimeOut()
{
    if (WiFiSendTimeOut>0) WiFiSendTimeOut--;
    return WiFiSendTimeOut;
}
//////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Update Status Class for each hardware devices EhDev[i] 
 * 
 * @return 
 */
static public int QueryReceived(long checksum)   //query status received event of ehouse 1 devs
        {
        int i;
        boolean qchanged=false;
        for (i = 0; i < RM_COUNT; i++)  //ehouse 1 devices
            {
               if (EhDev[i]==null) break;
               else
                if (EhDev[i].isAddress(QueryBuff[1]&0xff, QueryBuff[2]&0xff))// compare by addr h and addr l
                {
                    if (i == 0) OnlyTheFirst = true;                //first device in EhDev matrix
                    EhDev[i].CheckSum=checksum;
                    if (EhDev[i].WriteCheckIfChangedAndCpy(QueryBuff,false) ) //update data to EhDev[i] matrix
                        {   //data was changed from previous reception
                            qchanged=true;
                            Date lastupdate = new Date();
                            Changed = true;

                        }
                        break;    
                }
            }
        boolean cmmana=false;
        int ADDRH=QueryBuff[1]&0xff;
        int ADDRL=QueryBuff[2]&0xff;
        if ((ADDRH>0)&& (ADDRH<=85)) cmmana=true;  //roommanagers via commmanager
        //if ((ADDRH>85)) cmmana=true;  //roommanagers via commmanager
        //if ((ADDRH==0) ) cmmana=true;
        //if ((ADDRL>254) ) cmmana=true;
        //if ((QueryBuff[2]==-2)) cmmana=true;
        //if ((QueryBuff[2]==-1)) cmmana=true;
        //if ((ADDRH==254) && (ADDRL==254))            cmmana=true;
        if (ViaCommManager)   //Connection via commanager
        for (i = 0; i < TCP_COUNT; i++)    //ethernet devices
            {
            if (EhDevTCP[i]==null) break;
            else
            {
            if ((cmmana))    
                {
              if (EhDevTCP[i].IsCommManager)        //if commmanager
                    {
                    EhDevTCP[i].CheckSum=checksum;
                    if (EhDevTCP[i].WriteCheckIfChangedAndCpy(QueryBuff,false,ViaCommManager) ) //decode commanager status
                        {   //data was changed from previous reception
                            qchanged=true;
                            Changed = true;
                            //EhDevTCP[i].SignalUpdate();
                        }
                break;
                }
            }
            else
              {
                if (EhDevTCP[i].isAddress(QueryBuff[1],QueryBuff[2]))  
                {
                    EhDevTCP[i].CheckSum=checksum;
                    if (EhDevTCP[i].WriteCheckIfChangedAndCpy(QueryBuff,false,true) ) //decode commanager status
                        {   //data was changed from previous reception
                            qchanged=true;
                            Changed = true;
                            //EhDevTCP[i].SignalUpdate();
                            break;
                        }    
                }
              }        
            }
         }
        
        return -1;
   }
//////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Update Status Class for each hardware devices EhDev[i] for each device
 * 
 * @return 
 */
static public int QueryWiFiReceived(long checksum)   //query status received event of ehouse 1 devs
        {
        int i;
        boolean qchanged=false;

        boolean cmmana=false;
        int ADDRH=QueryBuff[1]&0xff;
        int ADDRL=QueryBuff[2]&0xff;
//        if (!ehousecommunication.DISABLED_EHOUSE1)
//            if (((ADDRH==1) || (ADDRH==2) || (ADDRH==55) || (ADDRH==85)) && (ADDRL<100)) cmmana=true;  //roommanagers via commmanager
        //if ((ADDRH>85)) cmmana=true;  //roommanagers via commmanager
        //if ((ADDRH==0) ) cmmana=true;
        //if ((ADDRL>254) ) cmmana=true;
        //if ((QueryBuff[2]==-2)) cmmana=true;
        //if ((QueryBuff[2]==-1)) cmmana=true;
        //if ((ADDRH==254) && (ADDRL==254))            cmmana=true;
        for (i = 0; i < TCP_COUNT; i++)    //ethernet devices
            {
            if (EhDevTCP[i]==null) break;
            else
            {
            
              {
                if (EhDevTCP[i].isAddress(QueryBuff[1]&0xff,QueryBuff[2]&0xff))  
                {
                    EhDevTCP[i].CheckSum=checksum;
                    if (EhDevTCP[i].WriteCheckIfChangedAndCpyWiFi(QueryBuff,//false));//,//EhDevTCP[i].IsCommManager))
                            false) ) //decode commanager status
                        {   //data was changed from previous reception
                            qchanged=true;
                            Changed = true;
                            break;
                        }
                }
              }
            }
         }
//visualization added        
        if (qchanged) ///something was changed from previous state
            { //update cache and visualization
            UpdateVisualization();
            UpdateViews();      
            }
        return -1;
   }
//////////////////////////////////////////////////////////////////////////////////////////////////

static public int QueryProReceived(long checksum,int size)   //query status received event of ehouse 1 devs
        {
        int i;
        boolean qchanged=false;
        for (i = 0; i < MAX_SIZE_OF_EHOUSE_PRO; i++)  //ehouse 1 devices
            {
                if ((QueryBuff[0]&0xff)!=0xff) return -1;
                if ((QueryBuff[1]&0xff)!=0x55) return -1;
                if ((QueryBuff[2]&0xff)!=0xff) return -1;
                if ((QueryBuff[3]&0xff)!=0x55) return -1 ;
                
               if (EhPro[i]==null) break;
               else
                if (EhPro[i].isAddress(QueryBuff[4]&0xff, QueryBuff[5]&0xff))// compare by addr h and addr l
                {
                    
                    
//                    if (i == 0) OnlyTheFirst = true;                //first device in EhDev matrix
                    EhPro[i].CheckSum=checksum;
                    if (EhPro[i].WriteCheckIfChangedAndCpy(QueryBuff,false,size) ) //update data to EhDev[i] matrix
                        {   //data was changed from previous reception
                            qchanged=true;
                            Date lastupdate = new Date();
                            Changed = true;

                        }
                        break;    
                }
            }
        
//visualization added        
        if (qchanged) ///something was changed from previous state
            { //update cache and visualization
            UpdateVisualization();
            UpdateViews();      
            }
        return -1;
   }
//////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * check if something was changed flag
 * 
 * @return 
 */
public boolean IsChanged()
    {
        return Changed;
    }
/**
 * Get last update status
 * @return 
 */
public Date LastUpdate()
    {
        return lastupdate;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Clear Changed Flag
 */
public void ClrChanged()
        {
            Changed = false;
        }
///////////////////////////////////////////////////////////////////////////////
/**
 * get index of ehouse 1 device searched by name
 * 
 * @param str
 * @return 
 */
public static int GetEhouseIndex(String str)
{
if (str==null) return -1;    
for (int i=0;i<RM_COUNT;i++)
    {
    if (EhDev[i].DeviceName.compareTo(str)==0) return i;
    }
return -1;
}
///////////////////////////////////////////////////////////////////////////////
/**
 * get index of ethernet ehouse device name 
 * 
 * @param str
 * @return 
 */
public static int GetEhouseTCPIndex(String str)
{
if (str==null) return -1;    
for (int i=0;i<TCP_COUNT;i++)
    {
    if (EhDevTCP[i].DeviceName.compareTo(str)==0) return i;
    }
return -1;
}
////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
/**
 * get names, signals, eventnames for inicialization of appliaction
 */
public static void AllDevsStatus()
         {
if (EhouseDevicesInitiated) return; //if already initiated at the start of aplication then ignore
    String path=ehousecommunication.path;
             int i=0;
             String temp;
        if (ehousecommunication.FileExists(path+"mobile/devices.txt")) //device names
             //try
             {
            
             String devs[]=ehousecommunication.getfile(path+"mobile/devices.txt"); //load
             l("[startup] Reading configuration for eHouse 1 devices from "+path+"mobile/devices.txt");


int m=0;
             for (i = 0; i < MAX_SIZE_OF_EHOUSE_DEVS; i++)
             {
                 if (devs[i]!= null)
                 {
                     temp = devs[i];
                     boolean ignore=false;
                     //dummy events for programs, events, security zones, security programs, analog programs, user events, output events, rollers events
                     if  
                     ((temp.indexOf(" - P") > 0) ||
                             (temp.indexOf(" - E") > 0) ||
                             (temp.indexOf(" - U") > 0) ||
                             (temp.indexOf(" - A") > 0) ||
                             (temp.indexOf(" - O") > 0) ||
                             (temp.indexOf(" - R") > 0) ||
                            (temp.indexOf(" - S") > 0)  ||
                            (temp.indexOf(" - Z") > 0)) {ignore=true;continue;}
                         
                      if (temp.equalsIgnoreCase("Batch")) {ignore=true;continue;}
                        if (temp.equalsIgnoreCase("Secu")) {ignore=true;continue;}                                    
                         
                     
                      if (temp!=null)
                        if ((temp.length() > 0) && (!ignore)) //if not empty and not to ignore
                     {
                         temp = path + "eMobile"+sep+"devs"+sep + temp + ".dev"; //devs config file
                         EhDev[m]=new StatusEhouse(); // initialize status of ehouse 1 dev matrix
                         EhDev[m].LoadDta(temp);//, enc); //load configuration data
                         if (DevNameIndex<MAX_TOTDEVS_COUNT)
                            DevViewNames[DevNameIndex]=EhDev[m].DeviceName; //load device name for automatic visualization names
                        DevNameIndex++;                       
                         if (DevNameIndex<MAX_TOTDEVS_COUNT) //set next device name to null for any case
                            DevViewNames[DevNameIndex]=null;
                        
//                         EhDev[m].clearvisualisationitems();
                         EhDev[m].SetStatusIndex(i); //
                         if (EhDev[m].IsEM==true)
                            {
                                EhDev[m].ReadAlarmSensorsNames(path+"AlarmSensors.txt"); //load alarm sensor names for external manager
                            }
                         if (EhDev[m].IsHM) 
                             EhDev[m].ReadHMSensorsNames(); //load temperature sensor names for HM
                         RM_COUNT++;
                         //OpenRemoteExport.ExportEhouseDev(m);
                         try{
                        if (!ehousecommunication.DisabledOpenRemoteExportEhouse1) OpenRemoteExport.ExportEhouse1DevTelnet(m);
                        }
                        catch (Exception aa)
                        {l("[eHouse 1] Exception exp: "+aa.getMessage());}
                        
                         m++;
                         if (m==MAX_SIZE_OF_EHOUSE_DEVS) break; //if too much ehouse 1 devs then drop the rest
                     }
                     //else break;
                 }
                 else
                     break;
                 //else break;
             }
             EhouseDevicesInitiated = true; //set configuration initialized flag
             }
             

        }

/// /////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * read and initialize array ethernet device names, sensors, configurations
 */
public static void AllDevsTCPStatus()//String path, String TCPLocalServer, String TCPLocalPort,String TCPRemoteServer,String TCPRemotePort)
{   int i=0;
    String temp;
    String path=ehousecommunication.path;
    if (EhouseTCPDevicesInitiated) return; //if already initiated at aplication start then ignore
    File dir = new File(ehousecommunication.locpath+"devs");

String[] TCPConfigDev = dir.list();

//String[] tcpdevs= new String[ehousecommunication.MaxTCPDevs];
if (TCPConfigDev == null) 
    {
    // Either dir does not exist or is not a directory
    l("[startup] Configuration Directory does not exists: %ehouse%/devs");
    return;
    } 


if (TCPConfigDev.length>0) 
        {
            l("[startup] Reading configuration for Ethernet devices");
            //String tcpdevs[]=ehousecommunication.getfile(path + "mobile"+sep+"tcpdevices.cfg");//, enc);
            int m=0;
            for (i = 0; i < TCPConfigDev.length; i++)
            {
                //if (TCPConfigDev[i] != null)
                 if ((TCPConfigDev[i].length()>2) &&(TCPConfigDev[i].indexOf(".cfg")>0))
                {
                    temp = ehousecommunication.locpath+"devs"+sep+TCPConfigDev[i];
                    l("[startup] Reading configuration for "+temp+" - started.");
                    if (temp.length() > 0) //if ethernet dev name is not null and not empty
                    {
                        //temp = path + "mobile"+sep+"tcpdevs"+sep + temp + ".dev"; //configuration path
                        //ehousecommunication.locpath+"devs"+sep+filename
                        EhDevTCP[m] = new StatusEthernet();                      //initialize ethernet device  matrix
                        try
                            {
                            EhDevTCP[m].LoadDta(temp);       //load configuration data for current ethernet device
                            l("[startup] Reading configuration for "+temp+" - finished.");
                            }
                        catch (Exception aa)
                            {
                            ehousecommunication.l("[startup] Exception: "+aa.getMessage());
                            //System.exit(0);
                            }
                        
                                
                         if (DevNameIndex<MAX_TOTDEVS_COUNT)
                            DevViewNames[DevNameIndex]=EhDevTCP[m].DeviceName; //set devname for automatic visualization
                        DevNameIndex++;
                        if (DevNameIndex<MAX_TOTDEVS_COUNT) //set next to null for any case
                            DevViewNames[DevNameIndex]=null;
                        EhDevTCP[m].SetStatusIndex(i);
                        if (EhDevTCP[m].IsCommManager) //load alarm sensor names for commmanager
                        {
                            EhDevTCP[m].ReadAlarmSensorsNames(path + EhDevTCP[i].DevAdr +sep+ "SensorNames.txt");
                        }
                        TCP_COUNT++;
                        // --- zapis statusu do pliku XML
                        //EhDevTCP[m].MakeXml();
                        try{
                            if (!ehousecommunication.DisabledOpenRemoteExportTCP) {
                                OpenRemoteExport.ExportEthernetEhouseDevTelnet(m) ;
                            }
                                else 
                            {
                                l("[startup] TCP devices export deactivated.");
                            };
                        }
                        catch (Exception aa)
                        {l("[startup] ERROR: "+aa.getMessage());}
                                
                        m++;
                        if (m==MAX_SIZE_OF_EHOUSE_DEVS_TCP) break;
                    }
                    else break;
                }
                else
                    break;
                
            }
            
            EhouseTCPDevicesInitiated = true; //set flag that ethernet devices were initialized

        }
  
}

//////////////////////////////////////////////////////////////////////////////////////////////////
public static int GetProgress()
        {
            return ProgressIndicator;
        }
//////////////////////////////////////////////////////////////////////////////////////////////////
        public static void ClrEvents()
        {
            EventSize = 0;
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////
        public static void SetMode(Byte mode)
        {
            ClientOpts = mode;
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * append binary event to send queue
 * 
 * @param events
 * @return 
 */
        public static int AppendEvent(Byte[] events)
        {
            int i;
            if (EventSize + EVENT_SIZE < EventLengtMax)
            {
                for (i = 0; i < EVENT_SIZE; i++)
                {

                    EventList[EventSize] = events[i];
                    EventSize++;
                }
                return EventSize;
            }
            else
            {
                return 0;

            }
        }
        //////////////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
        static void PerformQuery()
        {

            //  (QueryBuff);
        }
        /// ////////////////////////////////////////////////////////////////
        //check if terminate tcp communication was initiated
        public static boolean isTerminated()
        {
            if (TerminateSend)
                return true;
            else
                return false;
        }
///////////////////////////////////////////////////////////////////
/**
 * initiate termination of tcp communication
 */
        public static void Terminate()
        {
            TerminateSend = true;

        
       }
//////////////////////////////////////////////////////////////////////////////////        
/**
 * terminate query reception from controllers over tcpip   
 */     
        public static void TerminateQuery()
        {
            TCPPanelEnabled = false;
            QueryTerminateSend = true;
            
        }
    static int QueryRunnerStatus=0;
/**
 * check integer value
 * 
 * @param stt
 * @param defaut
 * @return 
 */
    static private int check_in(String stt, int defaut)
    {
        double dd = Double.valueOf(stt);
        int ino = (int)dd;
        //ino=st.IConvertible.ToInt32();
        return (ino);
    }

////////////////////////////////////////////////////////////////////////////////        
        
     
    }
//}