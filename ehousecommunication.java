package ehouse4openremote;
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
 * 
 * 
 * 
 * Initialization and eHouse communication functions
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;
public class ehousecommunication
{  
    /**
   //export open remote designer object directly to beehive database
                                                                     //afterwards requires:
                                                                            //1) enter designer manually, http://localhost:8080/designer/ and login
                                                                            //2)save remote controller settings
                                                                            //3) setting panel items
                                                                            //4) saving panel items
                                                                            //5) export as zip
                                                                            //6) enter http://localhost:8080/controller/ with the same user name and password
                                                                            //7) Sync With online designer
                                                                            * */
   public static boolean ExportOpenRemoteDesignerObjects=false;     
   /** debug server instances - all server messages */
   public static boolean DebugServers=false;     
   /**
    * Ignore names with @ character in name
    */
   public static boolean IgnoreAtChar=true;
   public static boolean Initialized=false;
   /** Enable Xml file creation*/
   public static boolean EnableXMLStatus=true;  
   /** disable open remote export for ethernet controllers*/
   public static boolean DisabledOpenRemoteExportTCP=false; 
   /** disable open remote export for ehouse1 controllers*/
   public static boolean DisabledOpenRemoteExportEhouse1=false; 
    /** Enable Autodisconnection of open remote server instances after data submition   */
   public static boolean AutoDisconnectOpenRemoteServerInstances=true;  
   /** Open remote tcp servers for sensor status input */
   public static boolean OpenRemoteTCPStatusEnabled=true;   
   /** Enable rest server - future */
   public static boolean RestEnable=true;
   public static long PrevTime=0;
   public static boolean internet=false;
   public static String sep=System.getProperty("file.separator");
   /**
    * Path
    */
   public static String locpath=System.getProperty("user.dir")+sep;
   /** eHouse PC directory for config files
    * 
    */
   //public static String locpath="d:/e-comm/e-house/";//System.getProperty("user.dir")+sep+"ehouse"+sep;
   public static String eHousePath="";
   public static String path=locpath;
   public static boolean disablestatperforme=true;
   //final static int MaxTCPDevs=255;
   //final static int MaxEhouse1Devs=255;
   static String VendorCodes;
   static String PassCodes;
   /**
    * OpenRemote tcp port
    */
   static String OpenRemotePort="4321";
   /**
    * Open remote server address - use localhost ip adres for the same machine
    */
   static String orServerHost="127.0.0.1";    //address for javaserver
   /**
    * Mysql database host - localhost recommended
    * for url - jdbc
    */
   static String DBHost="localhost";    //url for jdbc
   /**
    * Port of Mysql Server
    */
   static String MysqlPort="3306";
    public static final int OPTIONS=3;
    /**
     * TCP authorisation method to ehouse ethernet controllers
     */
    final static int CHALLANGE_RESPONSE =0;
    final static int XOR_PASS           =1;
    final static int PLAIN_PASS         =2;
    final static int NOTHING            =3;   
    final static int FURTKA             =5;   
    /**
     * Password code for decoding static/dynamic authorisation
     */
    protected static int[] PassWord    = new int[255];
    /**
     * Vendor code for additional hashing of authorisation
     */
    protected static int[] Vendor      = new int[255];
    /**
     * Windows file system code page
     */
    static String enc="Cp1250"; 
    /**
     * End of line string
     */
    static String EOLN="\r\n";
    /**
     * local CommManager IP
     */
    static String TCPLocalServer="192.168.0.254";
    /**
     * TCP local port of ehouse controllers
     */
    static String TCPLocalPort="9876";
    /**
     * UDP Port reception of Ethernet eHouse status from controllers
     */
    static String UDPStatusReceptionPort="6789";
    protected static int[] RESP = new int[20];
    protected static int[] response = new int[255];
    protected static int[] Hashingin=new int[250];
    /**
     * Terminate flags for finalize continuous threads
     */
    static boolean Terminate=false;
    /**
     * Size of event in queue
     */
    protected static int EventSize=0;
    /**
     * Wifi Send OK flag
     */
    public static boolean WiFiSendOk=false;
    /**
     * Event queue to send - binary
     */
    protected static byte[] EventToRunByte= new byte[256];
    /**
     * Event To Send Queue in string
     */
    protected static String EventToRun="";
    boolean eHouse1Support=false;
    boolean statusapp=false;
    private static boolean ValidDate =false;
    protected static String pass="";
    /**
     * Socket and stream for connection
     */
    public static Socket ehouseconnection=null;            //Client Connection
    public static OutputStream ehouseout=null;            //output stream connection
    public static InputStream ehousein=null;              //input stream connection
    public static BufferedReader ehouseinput=null;
    /**
     * Server for OpenRemote queries port number (TELNET or TCP)
     */
    public static int OpenRemotePortNr=4321;
    /**
     * beehive admin user/pass for accessing and updating designers tables
     */
    static String BeehiveUserName="beehive";           
static String BeehiveUserPassword="beehivepass";

        //public BufferedWrite ehouseoutput=null;
        int error=0;    
        String str="";
        static String log="";
        public static String[] Win = { "\u00b9", "\u00a5", "\u00e6", "\u00c6", "\u00ea", "\u00ca", "\u00b3", "\u00a3", "\u00f1", "\u00d1", "\u00f3", "\u00d3", "\u009c", "\u008c", "\u00bf", "\u00af", "\u009f", "\u008f" };        //windows code page
        public static String[] SmsFormat = { "\u0002", "\u0003", "\u0004", "\u0005", "\u0006", "\u0007", "\u0008", "\u000B", "\u000C", "\u000E", "\u000F", "\u0010", "\u0011", "\u0012", "\u0013", "\u0014", "\u0015", "\u0016" };  //sms coding polish chars to unused chars in order to not use 16b unicode 
        public static String[] UnicodeFormat = { "\u0105", "\u0104", "\u0107", "\u0106", "\u0119", "\u0118", "\u0142", "\u0141", "\u0144", "\u0143", "\u00F3", "\u00D3", "\u015B", "\u015A", "\u017C", "\u017B", "\u017A", "\u0179" };//unicode 16b
        
////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Loging data to %ehouse%\logs\transactionlog.txt
 * @param str 
 */        
        static void log(String str)
{
    boolean append=false;
  try
      {
        // Create file 
        if (ehousecommunication.FileExists(ehousecommunication.path+"logs/TransactionLog.txt")) append=true;
        FileWriter fstream = new FileWriter(ehousecommunication.path+"logs/TransactionLog.txt",append);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(str+"\r\n");
        out.close();
        }
  catch (Exception e)
        { //Catch exception if any
        l("Error: "+e.getMessage());
        }    
}
/**
 * 
 * Logging exception trace to log
 * @param e - Exception
 * 
 */        
        static void ll(Exception e)
{
    boolean append=false;
    String stt="";
  try
    {
    stt=   e.getMessage()+"\r\n";
    for (int i=0;i<e.getStackTrace().length;i++)
        { 
        stt+=(e.getStackTrace()[i].toString())+"\r\n";
        }
    l(stt);
    }
  catch (Exception ee)
        {//Catch exception if any
        l("Error: stacking exceptions" );
        
        }        
    
    
    
    
}

/**
 * Convert int array to string value in hex format
 * @param dta   - input array
 * @param size  - size to perform
 * @return string representation
 */        
public static String hix(int[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]);
        }
    return res;
}
////////////////////////////////////////////////////////////////////////////////////////
/**
 * Write text to file
 * @param Fname - file name
 * @param text - text to write to file
 */
public static void WriteFile(String Fname, String text)
{
try {
    BufferedWriter out = new BufferedWriter(new FileWriter(//ehousecommunication.path+
            Fname));
    out.write(text);
    out.flush();
    out.close();
} catch (Exception e) {
    ehousecommunication.ll(e);
    
}    
    
    
}
//////////////////////////////////////////////////////////////////////////////////////////////        
/**
 * convert hex array data to string representation in hexadecimal format
 * @param dta  - Array of Byte to convert
 * @param size - size of array to convert
 * @return string value in hexadecimal format
 */
public static String hib(Byte[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]);
        }
    return res;
}
public static String hibb(byte[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]);
        }
    return res;
}
/////////////////////////////////////////////////////////////////////////////////////        
/**
 * Calculate challange response for eHouse controller authorization 
 * @param mode - mode of authorization :
 *      CHALLANGE_RESPONSE =0
 *      XOR_PASS           =1
 *      PLAIN_PASS         =2
 *      NOTHING            =3
 *      FURTKA             =5
 *      Write directly to response buffer
 */
protected static void MakeChalangeResponse(int mode)
{ 
if (mode<XOR_PASS) mode=XOR_PASS;
if (mode<=XOR_PASS) //xored password
    {
    response[6+0]=(response[0+0]^PassWord[0]^Vendor[0]) & 0xff;
    response[6+1]=(response[0+1]^PassWord[1]^Vendor[1]) & 0xff;
    response[6+2]=(response[0+2]^PassWord[2]^Vendor[2]) & 0xff;
    response[6+3]=(response[0+3]^PassWord[3]^Vendor[3]) & 0xff;
    response[6+4]=(response[0+4]^PassWord[4]^Vendor[4]) & 0xff;
    response[6+5]=(response[0+5]^PassWord[5]^Vendor[5]) & 0xff;
    response[12]=13;
    return;
    }

if (mode==PLAIN_PASS) //plain pass
    {
    response[6+0]=(PassWord[0]^Vendor[0])&0xff;
    response[6+1]=(PassWord[1]^Vendor[1])&0xff;
    response[6+2]=(PassWord[2]^Vendor[2])&0xff;
    response[6+3]=(PassWord[3]^Vendor[3])&0xff;
    response[6+4]=(PassWord[4]^Vendor[4])&0xff;
    response[6+5]=(PassWord[5]^Vendor[5])&0xff;
    response[12]=13;
    return;
    }
if (mode>PLAIN_PASS) //nothing
    {
    response[6+0]=0xff;
    response[6+1]=0xff;
    response[6+2]=0xff;
    response[6+3]=0xff;
    response[6+4]=0xff;
    response[6+5]=0xff;
    response[12]=13;
    return;
    }
}
/////////////////////////////////////////////////////////////////////////////////        
/**
 * Rounding double value to nearest integer value
 * @param dd - double param
 * @return integer value 
 */        
////////////////////////////////////////////////////////////////////////////////////////////////    
static public int round(double dd)
        {
            return (int)(dd + 0.5);
        }
//////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Check if file exists
 * @param str - file name + path
 * @return true / false
 */
///////////////////////////////////////////////////////////////////////////////        
static public boolean FileExists(String str)        
{
    
String  stra=str;
File f = new File(stra); 

if (f.exists())    return true;
else return false;
}
//////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/**
 * Get hex value one nibble 
 * @param dta - int value convert
 * @return 0..9 i A..F
 */
///////////////////////////////////////////////////////////////////////////////
static public String Hex(int dta)        
{
    
dta=dta&0x0f;
if (dta<10) return String.valueOf(dta);
if (dta==10) return "A";
if (dta==11) return "B";
if (dta==12) return "C";
if (dta==13) return "D";
if (dta==14) return "E";
if (dta==15) return "F";
return "0";
}
/**
 * Convert integer to hex data in string 00-FF
 * @param dta - int value to convert (0-15)
 * @return 
 */
///////////////////////////////////////////////////////////////////////////////
static public String ConvertAsciHex(int dta)
{
String res="";    
int tempdta=dta;

tempdta&=0x0f;
res=Hex(tempdta);
tempdta=dta>>4;
tempdta&=0x0f;
res=Hex(tempdta)+res;
return res;
}
////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////////////
/**
 * Add string to log
 * @param str - string for logging
 */
// Add to log st        
//////////////////////////////////////////////////////////////////////////////////        
    public static void l(String str)    
    {
        
        Calendar cal = Calendar.getInstance();
    long CurrentTime=cal.getTimeInMillis();
        System.out.println(str);    
        log(String.valueOf(CurrentTime-PrevTime) +"\t\t"+str);
        PrevTime=CurrentTime;

    }
////////////////////////////////////////////////////////////////////////////////////
//
/** Create eHouse Connection, setting all streams
   send DirectEvents via Ethernet / TCP IP directly to eHouse controllers
 * 
 * @param host - if empty use CommManager or eHouse PC Server Address, otherwise use this parameters
 * @param port - if not 0 then use port 
 * @return 
 */
//
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
public static boolean SendWiFi(String host,int port)    
{

if (EventSize==0) return true;
if (Terminate)   return false;
String IPadr=TCPLocalServer;
int PORT=Integer.parseInt(TCPLocalPort);
  
    if (host.length()>0) IPadr=host;
    if (port>0) PORT=port;

WiFiSendOk=false;    
if (CreateSocket(IPadr,PORT))
    {   //Send OK and close socket
    
    }

 
else CloseSockets();
return false;
}
/**
 * Close all sockets, free buffers, handler setting to null
 * 
 */
///////////////////////////////////////////////////////////////////////////////////////////////////////

static void CloseSockets()
{
    

    try
        {
            
       if (ehouseinput!=null) 
            ehouseinput.close();
       ehouseinput=null;
        }
    catch (java.io.IOException iof)   
            {   ehouseinput=null;
                l("[eHouse TCP] ERROR closing buffered input stream: ");
                ehousecommunication.ll(iof);
            }
    //ehouseoutput.close();
    try
        {
        if (ehouseout!=null)
            ehouseout.close();
        ehouseout=null;
        }
    catch (java.io.IOException iof)   
        {
        ehouseout=null;
        l("[eHouse TCP]  ERROR closing output stream: ");
        ehousecommunication.ll(iof);
        }
    try{
        if (ehousein!=null)
        ehousein.close();
        ehousein=null;
        }
    catch (java.io.IOException iof)   
            {
            ehousein=null;
            l("[eHouse TCP]  ERROR closing input stream: ");
            ehousecommunication.ll(iof);
            }
    try
        {
        if (ehouseconnection!=null)
        ehouseconnection.close();
        ehouseconnection=null;
        }
    catch (java.io.IOException iof)   
            {ehouseconnection=null;
                l("[eHouse TCP]  ERROR closing connection socket: ");
                ehousecommunication.ll(iof);
            }
    l("[eHouse TCP]  Closing Socket");
    ehouseinput=null;
    ehousein=null;
    ehouseout=null;
    ehouseconnection=null;

}
////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////

//
// List Log Data
//
//
/////////////////////////////////////////////////////////////////////////
static public String GetLog()
{
    return log;
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////
//
//
// Clear Log Data
//
//
////////////////////////////////////////////////////////////////////////////////////
public static void ClearLog()
{
    log="";
}

////////////////////////////////////////////////////////////////////////////////////
/**
 * initialize program
 * 
 */
public static void init()
{
    if (Initialized) return;
   WiFiReadCfg();
    
    
    
Initialized=true;    
boolean success=new   File(path+"TransactionLog.txt").delete();
}
//
/** Create Socket, Connect, Authorize - Challenge / Response SHORT EVENT SEND PROTOCOL EHouse */
//
//////////////////////////////////////////////////////////////////////////////////////////
    private static boolean CreateSocket(String addr,int port)    
    {
        int i=0;
        char[] TempBuff=new char[255];
        byte[] TempBuff2=new byte[255];
 
        l("[eHouse TCP] Connecting device");              
    try
        {
        InetAddress serverAddr = InetAddress.getByName(addr);
        ehouseconnection = new Socket(serverAddr,port);
        ehouseconnection.setTcpNoDelay(true);
        ehouseconnection.setSoLinger(false, 0);
        ehouseconnection.setSoTimeout(3000);
        ehouseconnection.setKeepAlive(true);
        }
 catch (java.net.UnknownHostException ife)
        {
        l("[eHouse TCP]  ERROR while creating connection (Unknown Host): ");
        ehousecommunication.ll(ife);
        return false;
        }
catch (java.io.IOException ioe)    
        {
        l("[eHouse TCP]  ERROR while creating input socket (I/O Exception): "+ioe.getMessage());
        ehousecommunication.ll(ioe);
        return false;
        }
    try    
        {
        ehousein = ehouseconnection.getInputStream();
        }
 catch (java.io.IOException iof)
        {
        l("[eHouse TCP]  ERROR while creating input stream (I/O Exception): "+iof.getMessage());
        ehousecommunication.ll(iof);
        return false;
        }
 try
       {
        ehouseout = ehouseconnection.getOutputStream ();
        }
 catch (java.io.IOException iof)   
 {
     l("[eHouse TCP]  ERROR while sreating output steream (I/O Exception): "+iof.getMessage());     
     ehousecommunication.ll(iof);
     return false;
 }
 if (ehouseconnection.isConnected())
 {
 l("[eHouse TCP] Device connected.");
 }
 ehouseinput = new BufferedReader (new InputStreamReader(ehousein));
 
// str="";
 try
    {
 
    if ((ehouseinput.read(TempBuff, 0, 6))==6)
        {   /// readed chalange code from controller
        
        pass=PassCodes;
        i=0;
        //l("Chalange Received from Server");
        response[i] = TempBuff[i]; i++;             //Calculate Response
        response[i] = TempBuff[i]; i++;
        response[i] = TempBuff[i]; i++;
        response[i] = TempBuff[i]; i++;
        response[i] = TempBuff[i]; i++;
        response[i] = TempBuff[i]; i++;
        l("[eHouse TCP]  Receive challange: "+hix(response,6));

        MakeChalangeResponse(XOR_PASS);   ///Calculate XORed password for authorisation
        for (int m=0;m<13;m++) TempBuff2[m]=(byte)response[m];
        i=13;TempBuff2[i]=(byte)EventSize;response[i]=EventSize;i++;        //attach Event to submit
        for (int m=0;m<EventSize;m++){TempBuff2[m+14]=(byte)EventToRunByte[m];response[m+14]=EventToRunByte[m];}
        l("[eHouse TCP]  Sending Response: "+hix(response,EventSize+14));    
        try
            {
            ehouseout.write(TempBuff2, 0, EventSize+14);   //send size
            ehouseout.flush();
            }
        catch (Exception e)
            {   
             l("[eHouse TCP]  Error Sending data: "+e.getMessage());       
             ehousecommunication.ll(e);
             return false;
            }
        TempBuff[0]='\0';
        int Res=0;
        l("[eHouse TCP]  Receive response ");
        try
            {
            Res=ehouseinput.read();
            }
        catch (Exception e)
                {
                l("[eHouse TCP]  ERROR Receiving Confirmation: "+e.getMessage());    
                ehousecommunication.ll(e);
                return false;
                }
        l("[eHouse TCP]  Received Confirmation:"+ (char)(Res));    
            {
          
            if (Res==(int)'+')    
                {
                l("[eHouse TCP]  Events Send successfully");
                
                ClearEventQueue();              //Clear event queue
                WiFiSendOk=true;                //set wifi/internet transmition completion flag
                TempBuff[0]=0;        
                
                CloseSockets();                 //close socket
        
                return true;                    //true for success
                }
            else            //event was send but not confirmed - uncertain execution
                {
                l("[eHouse TCP]  Event Not Confirmed");     
                try
                    {
                    ehouseout.write(0);   ///force closing socket on server side               
                    }
                catch (Exception e)
                	{
                    l("[eHouse TCP]  error sending closing command: "+e.getMessage());
                    ehousecommunication.ll(e);
                	}
                CloseSockets();        
                return false;
                }
        
            }

        }
    else
        {
        l("[eHouse TCP]  Incorrect challange data length");
        return false;
        }
    }
 catch (java.io.IOException iof)
    { 
        l("[eHouse TCP]  Error read the Socket (I/O Exception ): "+iof.getMessage());
        ehousecommunication.ll(iof);
        return false;
    }
 }
////////////////////////////////////////////////////////////////////////////////////
    
    
    
/////////////////////////////////////////////////////////////////////////////////////    
/**
 * Delete file
 * @param str  - path+filename
 */    
////////////////////////////////////////////////////////////////////////////////////
public static void delete(String str)
{
    File fl=new File(str);
    fl.delete();
}
////////////////////////////////////////////////////////////////////////////////

/**
 * Get whole text file content and return as simple string
 * @param str - path+file name
 * @return contents of text file as string
 */

    public static String GetFileContent(String str)
    {
    //String[] stttt=new String[260];
    String stra=str;
    String result="";
    int index=0;
    File f = new File(stra);
    
    
    try
    {
   FileInputStream fileIS = new FileInputStream(f);
   
   BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS,enc));
   
   String readString = null; 
      while((readString = buf.readLine())!= null)
        {
        result+=readString+"\r\n";
        
        }
      buf.close();
      fileIS.close();
    } 
    catch (FileNotFoundException e) 
        {
        l("[eHouse]  File: " + str+
                             " could not be found on filesystem : "+e.getMessage());    
        ehousecommunication.ll(e);
        }
    catch (UnsupportedEncodingException e)
        {
        l("[eHouse]  Exception while reading the file" + e.getMessage());    
        ehousecommunication.ll(e);
        
        }
    catch (IOException e)
        {
        l("[eHouse]  Exception while reading the file" + e.getMessage());    
        ehousecommunication.ll(e);
        }
    

return result;        
    }
////////////////////////////////////////////////////////////////////////////////    

/**
 * Read whole file max 260 lines
 * @param str - path+filename
 * @return  array of string
 */





///////////////////////////////////////////////////////////////////////////////////
    public static String[] getfile(String str)
    {
    String[] stttt=new String[260];
    String stra=str;
    int index=0;
    File f = new File(stra);
    
    try
    {
   FileInputStream fileIS = new FileInputStream(f);
   BufferedReader buf = new BufferedReader(new InputStreamReader(fileIS,enc));
   String readString = null; 
      while((readString = buf.readLine())!= null)
        {
        stttt[index]=readString;
        index++;
        stttt[index]=null;
        if (index>259) break;
        }
      buf.close();
      fileIS.close();
    } 
    catch (FileNotFoundException e) 
        {
        l("[eHouse]  File: " + str+" could not be found on filesystem : "+e.getMessage());    
        ehousecommunication.ll(e);
        }
    catch (UnsupportedEncodingException e)
        {
        l("[eHouse]  Exception while reading the file" + e.getMessage());    
        ehousecommunication.ll(e);
        }
    catch (IOException e)
        {
        l("[eHouse]  Exception while reading the file" + e.getMessage());    
        ehousecommunication.ll(e);
        }
    

return stttt;        
    }
////////////////////////////////////////////////////////////////////////////////    
/**    
 * 
 * Read initial configuration for application.
 * 
 * 
 */ 
    
static private boolean WiFiReadCfg()
            {
        

        String[] wificfg=new String[20];

        wificfg=getfile(locpath+"JavaORServer.cfg");
        TCPLocalServer=wificfg[0];
        TCPLocalPort=wificfg[1];
        UDPStatusReceptionPort=wificfg[2];
        PassCodes=wificfg[3];
        PassWord[0]=PassCodes.charAt(0);
        PassWord[1]=PassCodes.charAt(1);
        PassWord[2]=PassCodes.charAt(2);
        PassWord[3]=PassCodes.charAt(3);
        PassWord[4]=PassCodes.charAt(4);
        PassWord[5]=PassCodes.charAt(5);
                      
        VendorCodes=wificfg[4];
        eHousePath=wificfg[5];
        locpath=eHousePath;
        path=locpath;

        OpenRemotePort=wificfg[6];
        orServerHost=wificfg[7];    // 		or Server Host="127.0.0.1";    //address for javaserver
        DBHost=wificfg[8];
        MysqlPort=wificfg[9];
        enc=wificfg[10];        
	BeehiveUserName=wificfg[11];		 //beehive admin user/pass for accessing and updating designers tables
        BeehiveUserPassword=wificfg[12];	//beehive pass	

        try
            {
            OpenRemotePortNr=Integer.parseInt(OpenRemotePort);
            }
        catch (Exception e)
                {
                    l("Wrong OpenRemote port - default port 4321 is  used.");
                    ehousecommunication.ll(e);
                    OpenRemotePortNr=4321;
                }
        int h=0;
        try
            {
            Vendor[0]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            Vendor[1]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            Vendor[2]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            Vendor[3]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            Vendor[4]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            Vendor[5]=Integer.parseInt(VendorCodes.substring(h,h+1),16)*16+Integer.parseInt(VendorCodes.substring(h+1,h+2),16);h+=2;
            }
       catch(Exception e)
                {
                l("[eHouse] Exception in vendor code: " + e.getMessage());
                ehousecommunication.ll(e);
                }

return true;
    }
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Clear Event Queue to send events
 */
public static void ClearEventQueue()
{
while (EventSize>0)
                  {
                  EventToRunByte[EventSize]=0;
                  EventSize--;
                  }
EventToRun="";
EventSize=0;

}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
/** 
 * Convert Event and add to queue from asci hexed value
 *
 * 
 * @param dta - String event in 20bytes of hex value spaces are removed eg. "00 FE 21 00 02 00 00 00 00 00"
 * @return size of queue of events store in cache 
 */
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
public static int AddAsciiEvent(String dta)
        {
            dta=dta.toUpperCase();
            dta=dta.replaceAll(" ", "");
            dta=dta.replaceAll("\t", "");
            dta+="0000000000000000000000000000";     //just in case if no event become empty event
            int offset=EventSize;
            EventToRunByte[offset+0] = GetHex(dta, 0);
            EventToRunByte[offset+1] = GetHex(dta, 2);
            EventToRunByte[offset+2] = GetHex(dta, 4);
            EventToRunByte[offset+3] = GetHex(dta, 6);
            EventToRunByte[offset+4] = GetHex(dta, 8);
            EventToRunByte[offset+5] = GetHex(dta, 10);
            EventToRunByte[offset+6] = GetHex(dta, 12);
            EventToRunByte[offset+7] = GetHex(dta, 14);
            EventToRunByte[offset+8] = GetHex(dta, 16);
            EventToRunByte[offset+9] = GetHex(dta, 18);
            EventSize=offset+10;
            EventToRun+=dta;
            return EventSize;
            
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// Convert Event and add to queue from asci hexed value
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//
/** get binary data from hex 2 byte - 00-FF */
//        
//
public static int GetHexU(String st,int offset)
        {
        int temp;
        int temp2;
        char[] cst=st.toUpperCase().toCharArray();
                
        if (cst[offset+0] <= '9')
            temp = cst[offset+0] - '0';
        else
            temp = 10+cst[offset+0] - 'A';

        if (cst[offset+1] <= '9')
            temp2 = cst[offset+1] - '0';
        else
            temp2 = 10 + cst[offset + 1] - 'A';
        temp=(temp<<4)+temp2;
        return temp;
        }
/////////////////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////
//
/** get binary data from hex 2 byte 

 * 
 * @param st - string coded in hexadecimal format 
 * @param offset - offset from begin of string
 * @return  binary data (byte)
 */
//        
//
public static byte GetHex(String st,int offset)
        {
        int temp;
        int temp2;
        char[] cst=st.toUpperCase().toCharArray();
                
        if (cst[offset+0] <= '9')
            temp = cst[offset+0] - '0';
        else
            temp = 10+cst[offset+0] - 'A';

        if (cst[offset+1] <= '9')
            temp2 = cst[offset+1] - '0';
        else
            temp2 = 10 + cst[offset + 1] - 'A';
        temp=(temp<<4)+temp2;
        return (byte) temp;
        }
  //////////////////////////////////////////////////////////////////////////////
  
////////////////////////////////////////////////////////////////////////////////
/**
 * Convert binary data to string representation in hex format
 * @param dta - byte buffer 0-255 values
 * @param size - size of buffer
 * @return string of hex conversion
 */
public static String hx(Byte[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]&0xff);
        }
    return res;
}
////////////////////////////////////////////////////////////////////////////////
/**
 * string representation for decimal value 
 * @param data 0
 * @return "1" or "0"
 */
public static String i(int data)
{
    return String.valueOf(data);
}

/**
 * string value of Boolean 
 * @param data - boolean argument
 * @return string "1" or "0"
 */
public static String bo(boolean data)
{
    if (data) return "1";
    else return "0";
    
}


}
