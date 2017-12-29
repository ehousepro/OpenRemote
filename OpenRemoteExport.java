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
 * Export data to OpenRemote Databases
 * 
 * Ehouse system export for Open Remote Designer (Beehive DB version 3.0Beta1)
 * 
 * Ignore Fields With '@' char
 */
package ehouse4openremote;
//import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import javax.sql.*;
import java.io.FileReader;
import org.apache.commons.codec.binary.Base64;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
//import java.io.StringWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

public class OpenRemoteExport {

    
/**
 * Exporting outputs, programs settings, sensors for open remote designer
 * @param index 
 * 
 */
    

static String URLPage = "http://localhost:8080/beehive/rest/";static String UserName="robert";static String UserPassword="robert"; //for restmode
static String BeehiveUserName=ehousecommunication.BeehiveUserName;           //beehive admin user/pass for accessing and updating designers tables
static String BeehiveUserPassword=ehousecommunication.BeehiveUserPassword;

static String command_ref_item_sql="";
static String device_sql="";
static String device_command_sql="";
static String protocol_sql="";
static String protocol_attr_sql="";
static String range_sensor_sql="";
static String sensor_sql="";
static String sensor_ref_item_sql="";
static String slider_sql="";
static String state_sql="";
static String switch_sql="";

static int account_oid=0;
static int command_ref_item_oid=1;      ///command ref table oid - ext indexed
static int sensor_ref_item_oid=1;       ///
static int on_switch_oid=1;             ///id for on switch - ext indexed  //command_ref_item_
static int off_switch_oid=1;            ///id for off switch - ext indexed //command_ref_item_
static int switch_oid=1;                ///id for switch- ext indexed //command_ref_item_
static int state_oid=1;                 //id of state values/items
//static int target_device_command_oid=1;  ///id for target device - ext indexed  //command_ref_item_
static int slider_oid=1;                ///id for slider - ext indexed //command_ref_item
static int sensor_oid=1;                ///id for sensor - ext indexed //command_ref_item
static int device_oid=1;                ////
static int device_command_oid=1;
static int protocol_oid=1;              //
static int protocol_attr_oid=1;
static String DeviceName="";            //name of device +signal type
static String Model="";                 //model type - controller type
final static int BASIC=0;
final static int BASE64=0;
final static int MD5=1;
final static int MD5_BASE64=2;
final static int NONE=3;
static Connection DBConnection=null;
///////////////////////////////////////////////////////////////////////////////////

public static void l(String s)
        { 
            
            
            ehousecommunication.l(s);
        }
///////////////////////////////////////////////////////////////////////
/**
 *  Sql update command to send to local sql server
 * @param str
 * @return 
 */
static int SQLUpdate(String str)
{
if (DBConnection==null    ) 
        ConnectDB("Beehive");
if (DBConnection==null) return -1;
Statement st = null;
try
{
l("+"+str)    ;
st = DBConnection.createStatement();
int recordsUpdated = 0;

recordsUpdated = st.executeUpdate(str);
if (recordsUpdated==0) {l("0 records");return recordsUpdated;}
if (recordsUpdated>0) {l("+OK");return recordsUpdated;}
else {l("-ER");return -1;}
}
catch (Exception e)
    {
    l("ERROR while executing SQL statement: "+e.getMessage());
    ehousecommunication.ll(e);
    return -1;
    }

}
/***
 * Connect to Beehive database returns connection handle
 * @param DBName
 * @return 
 */
static Connection ConnectDB(String DBName)
{
//String dbUrl = "jdbc:mysql://localhost/"+DBName;        //url for db connection
String dbClass = "com.mysql.jdbc.Driver";               //java driver
  String dbUrl = String.format(
        "jdbc:mysql://%s:%s/%s?user=%s&password=%s&characterEncoding=utf-8&" + 
        "useUnicode=true", ehousecommunication.DBHost, ehousecommunication.MysqlPort,DBName, BeehiveUserName, BeehiveUserPassword);


try 
{

Class.forName("com.mysql.jdbc.Driver");
DBConnection=DriverManager.getConnection (dbUrl,BeehiveUserName,BeehiveUserPassword);
//java.sql.Statement stat = DBConnection.createStatement();
//stat.execute("set names utf8");
//stat.execute("set character set utf8");
    return DBConnection;

} //end try
/////////////////////////////////////////////////////////

catch(Exception e) 
    {
    
    l("Error Connecting to Beehive DB: ");
    ehousecommunication.ll(e);
    //System.exit(0);
    }

return null;


}
////////////////////////////////////////////////////////////////////////////////////
/**
 * Get last oid for each beehive table 
 * @param Table - table name to check oid
 * @return - oid index
 *   -1 on failure
 */
static int GetMaxOID(String Table) 
{
    String dbcolumn="";
    if (DBConnection==null)
        //DBConnection=
                ConnectDB("Beehive");
    if (DBConnection==null) return -1;
 try
    {
    Statement stmt = DBConnection.createStatement();
    String query= "Select max(oid) from "+ Table+";";
    l("Query: "+query);
    ResultSet rs = stmt.executeQuery(query);

    while (rs.next()) 
        {
        dbcolumn = rs.getString(1);
        l(dbcolumn);
        if (dbcolumn==null) return -1;
        return Integer.parseInt(dbcolumn);
        } //end while

    } //end try

catch(Exception e) 
    {
    l("Error get Max OID for: "+Table+" >")   ; 
    ehousecommunication.ll(e);
    }


    
    return -1;
}
/**
 * Get oid TEST
 * @param Table
 * @param ByField
 * @param Value
 * @return 
 */


///////////////////////////////////////////////////////////////////////////////////
/**
 * Get OpenRemote ID for 
 * @param Table - table to get OID
 * @param ByField - find by field
 * @param Value - with value
 * @return OID
 */
static int GetOID(String Table,String ByField,String Value) 
{
    String dbcolumn="";
    if (DBConnection==null)
        //DBConnection=   
                ConnectDB("Beehive");
    if (DBConnection==null) return -1;
    
 try
    {
    Statement stmt = DBConnection.createStatement();
    String query= "Select * from "+ Table + " WHERE "+ByField+"='"+Value+"';";
    l("Query: "+query);
    ResultSet rs = stmt.executeQuery(query);

    while (rs.next()) 
        {
        dbcolumn = rs.getString(1);
        l(dbcolumn);
        } //end while
    DBConnection.close();
    } //end try

catch(Exception e) 
    {
    ehousecommunication.ll(e);
    }


    
    return -1;
}

////////////////////////////////////////////////////////////////////////////////
/**
 * REST authorisation calculation
 * @param AuthorisationType - type of authorisation
 * @return - hashed calculated authorisation string
 */
static String Authorisation(int AuthorisationType)
{  
switch (AuthorisationType)    
    {
    case BASIC:
        {
            return "Basic " + Base64(UserName+":"+UserPassword);
        }
    case MD5:    
        {
           return (Md5(UserName,UserPassword)) ;
        }
    case MD5_BASE64:
        {
        return "Basic " + MD5Base64(UserName,UserPassword);
        }
    case NONE:
        {
            return "";
        }
    }
return "";
        
}
///////////////////////////////////////////////////////////////////////////////////
/***
 * Test stuff - running SQL Script / clearing / testing
 * 
 */
static void runscript(String str)

{

    boolean booleanAutoCommit=false;
    boolean booleanStopOnerror=true;
    try
    {
    ScriptRunner runner = new ScriptRunner(DBConnection, booleanAutoCommit, booleanStopOnerror);
    runner.runScript(new BufferedReader(new FileReader(str)));
    }
    catch (Exception e)
            {l("Error running sql script: ");ehousecommunication.ll(e);}
    

}
static void Test()
        
{
    l("eHouse path:"+ehousecommunication.locpath);
    //System.exit(0);
    
    
    //        System.exit(0);
  //  l(String.valueOf(GetOID("Device","name","Salon") ));
    //l(String.valueOf(GetMaxOID("Device") ));
    //GetRest("user",MD5_BASE64);
    /*
     * GetRest("manageuser/getbyname/robert",NONE);
//    GetRest("sensor/loadall/6",BASIC);//, null,null);
//    GetRest("user/robert/openremote.zip",MD5_BASE64);        //działa
//GetRest("user/ehouse/openremote.zip",MD5_BASE64);        //działa
//GetRest("lirc/3M",NONE);                //działa
//GetRest("device/loadall/1",BASIC);
 //OpenRemoteExport.GetRest("user/robert/openremote.zip");                //działa
 //enRemoteExport.GetRest("manageuser/get/6");                
 //enRemoteExport.GetRest("device/loadall/6");                
//        lirc/3m/MP8640.
System.exit(0);*/
}
//////////////////////////////////////////////////////////////////////////////////

//insert into state (name, sensor_oid, value) values ('off', 2, 'off')

static int  add_state(
        String name,
        String value,
        String sensor_oid) 
        {
        state_sql=
                "INSERT INTO `state` "+
                "(" +// `oid`, 
                "`name`, `value`, `sensor_oid`)"+
                " VALUES ('"+
                name+"','"+
                value+"',"+
                sensor_oid+");\r\n";
        if (SQLUpdate(state_sql)<0) return -1;
        state_oid=GetMaxOID("state");     //set global varible of current oid to be used externally in valid sequence
        return state_oid;
        }




/**
 * Add command reference item for switches,sliders,sensors)
*/
static int  add_command_ref_item(
        String type,

        int target_device_command_oid, 
        String off_switch_oid, 
        String slider_oid, 
        String on_switch_oid, 
        String sensor_oid) 
        {
        command_ref_item_sql=
                "INSERT INTO `command_ref_item` "+
                "(`type`," +// `oid`, 
                "`target_device_command_oid`, `off_switch_oid`, `slider_oid`, `on_switch_oid`, `sensor_oid`)"+
                " VALUES ('"+
                type+"',"+

                String.valueOf(target_device_command_oid)+','+
                off_switch_oid +',' +
                slider_oid +','+ 
                on_switch_oid+','+
                sensor_oid+");\r\n";
        if (SQLUpdate(command_ref_item_sql)<0) return -1;
        command_ref_item_oid=GetMaxOID("command_ref_item");     //set global varible of current oid to be used externally in valid sequence
        return command_ref_item_oid;
        }

/////////////////////////////////////////////////////////////////////////////////
//
// add device table
//
////////////////////////////////////////////////////////////////////////////////
/**
 * 
 * Add device to beehivee database from global variables
 * 
 * 
 */

static int add_device()
{        
account_oid=GetMaxOID("account");
device_sql="INSERT INTO `device` ("
        +"`model`, `name`, `vendor`, `account_oid`) VALUES ('"+
        Model+"','"+DeviceName+"','Ethernet eHouse'"+","+account_oid+");\r\n";
if (SQLUpdate(device_sql)<0) return -1;
device_oid=GetMaxOID("device");        
return device_oid;
}
///////////////////////////////////////////////////////////////////////////////
/**
 * Add protocol for the command
 * @param type - protocol name
 * @return oid of current added protocol
 * also sets global variables for external usage
 */
static int  add_protocol(
        //int oid,
        String type
        )
{

protocol_sql=    
        "INSERT INTO `protocol` ( `type`) VALUES ("+
        "'"+type+"');\r\n";
if (SQLUpdate(protocol_sql)<0) return -1;
protocol_oid=GetMaxOID("protocol");
return protocol_oid;
}
/////////////////////////////////////////////////////////////////////////////////
/**
 * Get blob value and calculate from standard string value
 * @param text - text to decode
 * @return  Blob calculated value
 */
static String getBlob(String text)
    {
    return "0x"+ehousecommunication.hibb(text.getBytes(),text.length());    
    }
/**
 * Add protocol attributes for beehive database - for current command
 * @param Name - Name of field
 * @param value - value of field
 * @param protocol_oid  - assignment to protocol oid - index
 * @return 
 */
////////////////////////////////////////////////////////////////////////////////
static int add_protocol_attr(
 //       int oid,
        String Name,
        String value,
        int protocol_oid
        )
{

    protocol_attr_sql="INSERT INTO `protocol_attr` ("+" `name`, `value`, `protocol_oid`) VALUES ("+
    //String.valueOf(oid)+", '"+
    "'"+Name+"' ,"+
    getBlob(value)+", "+
    String.valueOf(protocol_oid)+");\r\n";
    if (SQLUpdate(protocol_attr_sql)<0) return -1;
protocol_attr_oid=GetMaxOID("protocol_attr");
return protocol_attr_oid;
}
/**
 * Add device command
 * @param name - Command name
 * @param sectionid - null
 * @param protocol_oid - assignment to protocol oid - index
 * @param device_oid - device oid - index
 * @return device command oid - index / -1 if failed
 */
///////////////////////////////////////////////////////////////////////////////////////
static int add_device_command(
        //int oid,
        String name,
        String sectionid,
        int protocol_oid, 
        int device_oid)
{
device_command_sql=
    "INSERT INTO `device_command` ("+" `name`, `sectionId`, `protocol_oid`, `device_oid`) VALUES ('"+
    name+"',"+
    sectionid+", "+
    String.valueOf(protocol_oid)+", "+
    String.valueOf(device_oid)+");\r\n";
if (SQLUpdate(device_command_sql)<0) return -1;
device_command_oid=GetMaxOID("device_command");
return device_command_oid;
}
///////////////////////////////////////////////////////////////////////////////////

/**
 * Add sensor_range to beehive database - sensor
 
 * @param max - value
 * @param min - value
 * @param sensor_oid - sensor oid
 * @return range_sensor_oid
 */
static int add_range_sensor(
        int max,
        int min,
        
        int sensor_oid
        )
{
    //insert into range_sensor (max_value, min_value, oid) values (255, 0, sensor_oid);
    sensor_sql="INSERT INTO `range_sensor` (`max_value`,`min_value`,`oid`) VALUES("+
            String.valueOf(max)+", "+
            String.valueOf(min)+", "+
            String.valueOf(sensor_oid)+");\r\n";
if (SQLUpdate(sensor_sql)<0) return -1;    
return GetMaxOID("range_sensor");
}

/**
 * Add sensor to beehive database - sensor
 * @param dtype - sensor type string value
 * @param name - Sensor name
 * @param type - type (int value)
 * @param account_oid - account oid - index for assignment
 * @param device_oid - device oid - index for assignment
 * @return cz
 */
static int add_sensor(
        String dtype,
        String name,
        int type,
        int account_oid,
        int device_oid
        )
{
    sensor_sql="INSERT INTO `sensor` (`dtype`,"/* `oid`,*/+" `name`, `type`, `account_oid`, `device_oid`) VALUES('"+
            //+ "'SIMPLE_SENSOR', 5, 'Oświetlenie Centralne - Sensor', 0, 6, 2);
            dtype+"', "+
            "'"+name+"', "+
            String.valueOf(type)+", "+
            String.valueOf(account_oid)+", "+
            String.valueOf(device_oid)+");\r\n";
if (SQLUpdate(sensor_sql)<0) return -1;    
sensor_oid=GetMaxOID("sensor");
return sensor_oid;
}
/////////////////////////////////////////////////////////////////////////////////////////
/**
 * Add sensor reference item
 * @param type - sensor type
 * @param slider_oid - slider oid - index of slider / if not slider then NULL
 * @param switch_oid - switch oid - index of switch / if not switch then NULL
 * @param target_sensor_oid - assigned sensor oid - index
 * @return recent sensor reference oid - index after operation / - 1 for error
 * also sets global variable for external usage
 */
//////////////////////////////////////////////////////////////////////////////////////
public static int  add_sensor_ref_item(
        String type,
        String slider_oid,
        String switch_oid,
        String target_sensor_oid
        )
{        
        
        sensor_ref_item_sql="INSERT INTO `sensor_ref_item` (`type`,"+" `slider_oid`, `switch_oid`, `target_sensor_oid`) VALUES ('"+
                //+ "SWITCH_SENSOR_REF', 3, NULL, 2, 5),
                type+"', "+
                slider_oid+", "+
                switch_oid+", "+
                target_sensor_oid+");\r\n";
if (SQLUpdate(sensor_ref_item_sql)<0) return -1        ;
sensor_ref_item_oid=GetMaxOID("sensor_ref_item");
return sensor_ref_item_oid;
}
/**
 * Add switch do database
 * @param name - Name of switch
 * @param account_oid - Account oid - index for assignment
 * @param device_oid  - device oid - index for assigment
 * @return switch oid - index after adding / -1 if error
 * also sets global variable for external usage
 */

////////////////////////////////////////////////////////////////////////////////////
public static int add_switch(
        String name,
        int account_oid,
        int device_oid
        )
{
    
switch_sql="INSERT INTO `switch` ("+" `name`, `account_oid`, `device_oid`) VALUES ("+
    
    "'"+name+"', "+
    String.valueOf(account_oid)+", "+
    String.valueOf(device_oid)+");\r\n";
if (SQLUpdate(switch_sql)<0) return -1;
switch_oid=GetMaxOID("switch");
return switch_oid;
}
/////////////////////////////////////////////////////////////////////////////////////
/**
 * Get base64 string for rest authorisation / Basic
 * @param input string
 * @return base64 calculated string
 */
static String Base64(String input)
{
    return new String(Base64.encodeBase64((input).getBytes()));
}
///////////////////////////////////////////////////////////////////////////////////
/**
 * Md5+base64 beehive/rest basic authorisation calculated from user and password
 * @param username - User Name
 * @param password - Password for user
 * @return calculated string for rest
 */
private static String MD5Base64(String username, String password)
    {
      Md5PasswordEncoder encoder = new Md5PasswordEncoder();

      String encodedPwd = encoder.encodePassword(new String(password), username);

      if (username == null || encodedPwd == null)
      {
        return null;
      }

      return new String(Base64.encodeBase64((username + ":" + encodedPwd).getBytes()));
    }
/////////////////////////////////////////////////////////////////////////////////////
/**
 * Calculate MD5 string from username and password for basic beehive/rest authorisation
 * @param username - Username
 * @param password - Password
 * @return - MD5 calculated string 
 */
private static String Md5(String username, String password)
    {
      Md5PasswordEncoder encoder = new Md5PasswordEncoder();
      String encodedPwd = encoder.encodePassword(password, username);
      if (username == null || encodedPwd == null)
            {
            return null;
            }
      return encodedPwd;
    }
////////////////////////////////////////////////////////////////////////////////////
/**
 * Post rest submittion 
 * @param RestQuery - rest query to submit
 * @param AuthenticationMode - authorisation mode for function
 * @param paramName - additional params name array
 * @param paramVal - additional params values array
 * @return response
 */
public static String PostRest(String RestQuery,int AuthenticationMode,String[] paramName,
String[] paramVal)  {
    
    String authStringEnc;
    try{
  URL url = new URL("http://localhost:8080/beehive/rest/"+RestQuery);
  HttpURLConnection conn =
      (HttpURLConnection) url.openConnection();
  if (AuthenticationMode!=NONE)
    conn.setRequestProperty("Authorization", Authorisation(AuthenticationMode));
  conn.setRequestMethod("POST");
  
  conn.setDoOutput(true);
  conn.setDoInput(true);
  conn.setUseCaches(false);
  conn.setAllowUserInteraction(false);
  conn.setRequestProperty("Content-Type",
      "application/x-www-form-urlencoded");

  // Create the form content
  OutputStream out = conn.getOutputStream();
  Writer writer = new OutputStreamWriter(out, "UTF-8");
  for (int i = 0; i < paramName.length; i++) {
    writer.write(paramName[i]);
    writer.write("=");
    writer.write(URLEncoder.encode(paramVal[i], "UTF-8"));
    writer.write("&");
  }
  writer.close();
  out.close();

  if (conn.getResponseCode() != 200) {
    throw new IOException(conn.getResponseMessage());
  }

  // Buffer the result into a string
  BufferedReader rd = new BufferedReader(
      new InputStreamReader(conn.getInputStream()));
  StringBuilder sb = new StringBuilder();
  String line;
  while ((line = rd.readLine()) != null) {
    sb.append(line);
  }
  rd.close();

  conn.disconnect();
  l(sb.toString());
  return sb.toString();
    }
  catch (Exception e)  
  {
  l("Error posting: ");
  ehousecommunication.ll(e);
  return "Error posting: "+e.getMessage();
  }
//return "end of post";    
  
}

/////////////////////////////////////////////////////////////////////////////////////
/**
 * Get rest - send rest command with GET method
 * @param RESTquery - beehive/rest query
 * @param AuthorisationMode  - authorisation mode for current function
 */
	public static void GetRest(String RESTquery,int AuthorisationMode) {

            	//try 
                    {
                                        String webPage=URLPage+RESTquery;
                    
                        try
                            {                                
                            String authStringEnc = Authorisation(AuthorisationMode);
                            URL url = new URL(webPage);
                              l(">URL");
                            URLConnection urlConnection = url.openConnection();
                            l(">URLConnection");
                            if (AuthorisationMode!=NONE)
                                urlConnection.setRequestProperty("Authorization", authStringEnc);
                            l(">Request property");
                            InputStream is = urlConnection.getInputStream();
                            l(">Input Stream");
                            InputStreamReader isr = new InputStreamReader(is);
                            l(">Input Stream Reader");
                            int numCharsRead;
                            char[] charArray = new char[1024];
                            l(">String Buffer");
                            StringBuffer sb = new StringBuffer();
                            
                            l(">before read");
                            while ((numCharsRead = isr.read(charArray)) > 0) 
                                {
				sb.append(charArray, 0, numCharsRead);
                                }
                            String result = sb.toString();
                            l("*** BEGIN ***");
                            l(result);
                            l("*** END ***");
                            }
                        catch (Exception ie)
                            {
                                
                            l("Error md5: "+ie);    
                            ehousecommunication.ll(ie);
                            }
			
		
	}
        
 //       System.exit(0);
        }

////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Close database connection to beehive dB
 * @param db - database name
 */
public static void CloseDB(Connection db)
        {
         if (DBConnection==null)    return;
         
            try{
                if (DBConnection.isClosed()) 
                    {
                    DBConnection=null;
                    return;
                    }                   
                DBConnection.close();
                }
            catch(Exception e)
                {
                l("Error Closing DB: "+e);
                ehousecommunication.ll(e);
                }
         try{   
            DBConnection=null;
            }
         catch (Exception e)
            {
            l("Error setting db to null: ")    ;
            ehousecommunication.ll(e);
            }
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Export Ethernet devices signals, outputs, items, events to OpenRemote Database for initial settings
 * @param index 
 */
public static void ExportEthernetEhouseDevTCP(int index)
{

if (ehousecommunication.ExportOpenRemoteDesignerObjects==false) return; //blocking flag
if (EhouseTCP.EhDevTCP[index]==null) return;                            //if valid data not null
if (EhouseTCP.EhDevTCP[index].DeviceName==null) return;    
if (EhouseTCP.EhDevTCP[index].DeviceName.length()<1) return;    
try
{

StatusEthernet se=EhouseTCP.EhDevTCP[index];

//model set controller type
if (se.IsCommManager) Model="CommManager";          //commmanager
if (se.IsERM) Model="EthernetRoomManager";          //ethernet Room Manager
if (se.IsEEM) Model="EthernetExtendedManager";      //Ethernet Extended Manager
if (se.IsLevelManager) Model="LevelManager";        //Level Manager
if (se.IsEHM) Model="EthernetHeatManager";          //Ethernet Heat Manager
int upto=se.OUTPUTS_COUNT_RM;
DeviceName=se.DeviceName+" - Outputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

//limit nr of outputs for device types
/**
 * 
 * Perform All Digital OUTPUTS
 * 
 * 
 */

if (se.IsERM) upto=35;
if (se.IsCommManager) upto=80;
if (se.IsLevelManager) upto=80;
for ( int i=0;i<upto;i++)
    {    
    String outtemplate=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "21"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_OUTPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    
//set output event - output ON   
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "01")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.OutputNames[i]+" (ON)", "NULL", protocol_oid, device_oid) ;
    int on_device_command_oid=device_command_oid;
   
//clear output event - output OFF   
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.OutputNames[i]+" (OFF)", "NULL", protocol_oid, device_oid) ;
    int off_device_command_oid=device_command_oid;    
    
//Toggle output event - switch output state
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "02")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.OutputNames[i]+" (Toggle)", "NULL", protocol_oid, device_oid) ;
    int toggle_device_command_oid=device_command_oid;

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.OutputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.OutputNames[i] /*+" (Sensor)"*/,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
//Add switch for changing state use ON/OFF
   add_switch(se.OutputNames[i]+"",account_oid,device_oid);
   add_command_ref_item("SWITCH_CMD_OFF_REF",off_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",on_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));

   
//Add switch for changing state use Toogle event   
   add_switch(se.OutputNames[i]+" Toggle",account_oid,device_oid);   
   add_command_ref_item("SWITCH_CMD_OFF_REF",toggle_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",toggle_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));
   
   

    }
        
/**
 * 
 * Perform All Digital Inputs
 * 
 * 
 */      
DeviceName=se.DeviceName+" - Inputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    

    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_INPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
   

    }
//limit nr of outputs for device types
/**
 * 
 * Perform All Output Programs button
 * 
 * 
 */

DeviceName=se.DeviceName+" - Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB


if (se.IsERM) upto=24;
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;

for ( int i=0;i<upto;i++)
    {    
    String outtemplate="";
    if (se.IsCommManager) 
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "62"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    else
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "02"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;
    


   

   

    }
        

DeviceName=se.DeviceName+" - ADC Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB


if (se.IsERM) upto=24;
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_ADC_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {    
    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "61"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ADCProgramNames[i], "NULL", protocol_oid, device_oid) ;
    


   

   

    }
        









if (se.IsCommManager) 
{
    
/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Active";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ACTIVE_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] /*+" (Sensor)"*/,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
   
    }    
    
/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Warning";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_WARNING_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
   
    }    


 /**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Monitoring";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_MONITORING_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] /*+" (Sensor)"*/,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
   
    }    

/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Alarm";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ALARM_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] /*+" (Sensor)"*/,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   
   
    }    
    
DeviceName=se.DeviceName+" - Security Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
    
    
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {    
    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(98)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;
    


   

   

    }
        




DeviceName=se.DeviceName+" - Zone";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
    
    
if (se.IsCommManager) upto=21;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_ZONE)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {    
    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(99)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ZoneNames[i], "NULL", protocol_oid, device_oid) ;
    }
//System.exit(0);
}
//l("Before db close");
CloseDB(DBConnection);
//l("after db close");
//System.exit(0);
//device_oid++;      //in add device  
//}
}
catch (Exception e)        
    {
    l("Exc ddfa: "+e);
    ehousecommunication.ll(e);
//    System.exit(0);
    }


}




/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Export Ethernet devices signals, outputs, items, events to OpenRemote Database for initial setup
 * @param index 
 */
public static void ExportEthernetEhouseDevTelnet(int index)
{
//System.exit(0);
if (ehousecommunication.ExportOpenRemoteDesignerObjects==false) return;
if (EhouseTCP.EhDevTCP[index]==null) return;
if (EhouseTCP.EhDevTCP[index].DeviceName==null) return;    
if (EhouseTCP.EhDevTCP[index].DeviceName.length()<1) return;    
try
{

StatusEthernet se=EhouseTCP.EhDevTCP[index];

//model set controller type
if (se.IsCommManager) Model="CommManager";          //commmanager
if (se.IsERM) Model="EthernetRoomManager";          //ethernet Room Manager
if (se.IsEEM) Model="EthernetExtendedManager";      //Ethernet Extended Manager
if (se.IsLevelManager) Model="LevelManager";        //Level Manager
if (se.IsEHM) Model="EthernetHeatManager";          //Ethernet Heat Manager
int upto=se.OUTPUTS_COUNT_RM;
DeviceName=se.DeviceName+" - Outputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

//limit nr of outputs for device types
/**
 * 
 * Perform All Digital OUTPUTS
 * 
 * 
 */

if (se.IsERM) upto=35;
if (se.IsCommManager) upto=80;
if (se.IsLevelManager) upto=80;
for ( int i=0;i<upto;i++)
    {    
    String param=se.OutputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
    String outtemplate=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "21"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_OUTPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    
//set output event - output ON   
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "01")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (ON)", "NULL", protocol_oid, device_oid) ;
    int on_device_command_oid=device_command_oid;
   
//clear output event - output OFF   
        add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (OFF)", "NULL", protocol_oid, device_oid) ;

    int off_device_command_oid=device_command_oid;    
    
//Toggle output event - switch output state
        add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "02")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (Toggle)", "NULL", protocol_oid, device_oid) ;

    int toggle_device_command_oid=device_command_oid;
    
////Get status command from java server
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.OutputNames[i] ,4,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    //
    
//Add switch for changing state use ON/OFF
   add_switch(se.OutputNames[i]+"",account_oid,device_oid);
   add_command_ref_item("SWITCH_CMD_OFF_REF",off_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",on_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));

   
//Add switch for changing state use Toogle event   
   add_switch(se.OutputNames[i]+" (Toggle)",account_oid,device_oid);   
   add_command_ref_item("SWITCH_CMD_OFF_REF",toggle_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",toggle_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));
   
   

    }
        
/**
 * 
 * Perform All Digital Inputs
 * 
 * 
 */      
DeviceName=se.DeviceName+" - Inputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_INPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    
    
    

  /*  
////Get status command from java server
    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
*/
   
    }


DeviceName=se.DeviceName+" - ADC Sensors";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=16;
if (se.IsCommManager) upto=16;
if (se.IsLevelManager) upto=16;
for ( int i=0;i<upto;i++)
    {    
String param=se.SensorNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    int sensortype=0;
    
    if (se.SensorTypeName[i].equalsIgnoreCase("LM335")) sensortype=TCPStatusServerHandler.SIGNAL_LM335_VALUE;
    if (se.SensorTypeName[i].equalsIgnoreCase("LM35")) sensortype=TCPStatusServerHandler.SIGNAL_LM35_VALUE;
    if (se.SensorTypeName[i].equalsIgnoreCase("VOLTAGE")) sensortype=TCPStatusServerHandler.SIGNAL_VOLTAGE_VALUE;        
    if (se.SensorTypeName[i].equalsIgnoreCase("% INV")) sensortype=TCPStatusServerHandler.SIGNAL_INVPERCENT_VALUE;        
    else 
        if (se.SensorTypeName[i].equalsIgnoreCase("%")) sensortype=TCPStatusServerHandler.SIGNAL_PERCENT_VALUE;        
    if (se.SensorTypeName[i].equalsIgnoreCase("MCP9700")) sensortype=TCPStatusServerHandler.SIGNAL_MCP9700_VALUE;        
    if (se.SensorTypeName[i].equalsIgnoreCase("MCP9701")) sensortype=TCPStatusServerHandler.SIGNAL_MCP9701_VALUE;        
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(sensortype)+" "+
            ehousecommunication.ConvertAsciHex(i+1);

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","60000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.SensorNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status For custom sensor
    add_sensor("CUSTOM_SENSOR",se.SensorNames[i] +" (Value)",4,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    
//for simple sensor    
//    add_sensor("SIMPLE_SENSOR",se.SensorNames[i]+" (Value)" ,0,account_oid,device_oid);
//    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    
/* range sensor
 *       1 Query insert into sensor (account_oid, device_oid, name, type, dtype) values (1, 3, 'sensor_range_1..123', 2, 'RANGE_SENSOR')
      1 Query insert into range_sensor (max_value, min_value, oid) values (123, 1, 7)
      1 Query insert into command_ref_item (target_device_command_oid, sensor_oid, type) values (5, 7, 'SENSOR_CMD_REF')

 * 
 * 
 */
//add_sensor("RANGE_SENSOR",se.SensorNames[i]+" (Value)" ,2,account_oid,device_oid);    
//    range_sensor (max_value, min_value, oid) values (123, 1, sensor_oid);
//add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
   

    }



DeviceName=se.DeviceName+" - Dimmers";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=3;
if (se.IsCommManager) upto=3;
if (se.IsLevelManager) upto=3;
for ( int i=0;i<upto;i++)
    {    
String param=se.DimmerNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_DIMMER_LEVEL)+" "+
            ehousecommunication.ConvertAsciHex(i+1);

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","60000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.DimmerNames[i]+" (Level)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status For custom sensor
//    add_sensor("CUSTOM_SENSOR",se.SensorNames[i] +" (Value)",4,account_oid,device_oid);
//    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
//    add_state("value","0",String.valueOf(sensor_oid));
    //add_state("off","off",String.valueOf(sensor_oid));
//for simple sensor    
//    add_sensor("SIMPLE_SENSOR",se.SensorNames[i]+" (Value)" ,0,account_oid,device_oid);
//    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    
/* range sensor
 *       1 Query insert into sensor (account_oid, device_oid, name, type, dtype) values (1, 3, 'sensor_range_1..123', 2, 'RANGE_SENSOR')
      1 Query insert into range_sensor (max_value, min_value, oid) values (123, 1, 7)
      1 Query insert into command_ref_item (target_device_command_oid, sensor_oid, type) values (5, 7, 'SENSOR_CMD_REF')

 * 
 * 
 */
add_sensor("RANGE_SENSOR",se.DimmerNames[i]+" (Value)" ,2,account_oid,device_oid);    
add_range_sensor (255, 0, sensor_oid);
add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
   

    }



//limit nr of outputs for device types
/**
 * 
 * Perform All Output Programs button
 * 
 * 
 */

DeviceName=se.DeviceName+" - Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB


if (se.IsERM) upto=24;
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    if (se.IsCommManager) 
        {
        add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
        add_sensor("CUSTOM_SENSOR","Current Security Program" ,0,account_oid,device_oid);
        }
    else 
        {
        add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
        add_sensor("CUSTOM_SENSOR","Current Program" ,0,account_oid,device_oid);
        }
    
    
//Add Sensor for querying device status
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
      

for ( int i=0;i<upto;i++)
    {    
    String param=se.ProgramNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
        
    String outtemplate="";
    if (se.IsCommManager) 
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "62"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    else
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "02"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;
  */  
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;
    
   

   

   

    }
        

DeviceName=se.DeviceName+" - ADC Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB


if (se.IsERM) upto=24;
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_ADC_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {    
    String param=se.ADCProgramNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
            
    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "61"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ADCProgramNames[i], "NULL", protocol_oid, device_oid) ;

   

   

    }
        









if (se.IsCommManager) 
{
    
/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Active";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ACTIVE_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;
*/
//Add Sensor for querying device status
/*   
 * add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
*/

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Active - Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    

    
    
   
    }    
    
/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Warning";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_WARNING_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
*/
        
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Active - Warning)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    
   
   
    }    


    /**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Monitoring";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_MONITORING_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    /*add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
*/
     
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Monitoring - Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    
   
   
    }    

/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Alarm";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsERM) upto=12;
if (se.IsCommManager) upto=48;
if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ALARM_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
////Get status command from java server
    /*add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;

//Add Sensor for querying device status
    add_sensor("SIMPLE_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

   */
   
   
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Alarm - Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
      
     
     
    }    
    
DeviceName=se.DeviceName+" - Security Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
    
    
if (se.IsCommManager) upto=24;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(11);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {
    String param=se.ProgramNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(98)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    /*add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;*/
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;



   

   

    }
        




DeviceName=se.DeviceName+" - Zone";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
    
    
if (se.IsCommManager) upto=21;
if (se.IsLevelManager) upto=24;
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_ZONE)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {    
    String param=se.ZoneNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(99)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Zone
    /*add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ZoneNames[i], "NULL", protocol_oid, device_oid) ;
    */
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ZoneNames[i], "NULL", protocol_oid, device_oid) ;


   

   

    }
        
//System.exit(0);


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   


}






//l("Before db close");
CloseDB(DBConnection);
//l("after db close");
//System.exit(0);
      
        
        
        
        

//device_oid++;      //in add device  
//}
}
catch (Exception e)        
    {
    l("Exc ddfa: ");
    ehousecommunication.ll(e);
//    System.exit(0);
    }

}




/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Export Ethernet devices signals, outputs, items, events to OpenRemote Database for initial settings
 * @param index 
 */






































/**
 * Export Ethernet devices signals, outputs, items, events to OpenRemote Database for initial settings
 * @param index 
 */
public static void ExportEhouse1DevTelnet(int index)
{
//System.exit(0);
if (ehousecommunication.ExportOpenRemoteDesignerObjects==false) return;
if (EhouseTCP.EhDev[index]==null) return;
if (EhouseTCP.EhDev[index].DeviceName==null) return;    
if (EhouseTCP.EhDev[index].DeviceName.length()<1) return;    
try
{

StatusEhouse se=EhouseTCP.EhDev[index];

//model set controller type
if (se.IsEM) Model="ExternalManager";          //commmanager
if (se.IsRM) Model="RoomManager";          //Room Manager
if (se.IsHM) Model="HeatManager";          //Heat Manager
int upto=se.OUTPUTS_COUNT_RM;
DeviceName=se.DeviceName+" - Outputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

//limit nr of outputs for device types
/**
 * 
 * Perform All Digital OUTPUTS
 * 
 * 
 */

if (se.IsRM) upto=35;
if (se.IsEM) upto=35;
if (se.IsHM) upto=24;
for ( int i=0;i<upto;i++)
    {    
    String param=se.OutputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
    String outtemplate=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "01"+" "+
            ehousecommunication.ConvertAsciHex(i+1)+" "+
            "ss"+" ";
    
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_OUTPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    
//set output event - output ON   
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "01")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (ON)", "NULL", protocol_oid, device_oid) ;
    int on_device_command_oid=device_command_oid;
   
//clear output event - output OFF   
        add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (OFF)", "NULL", protocol_oid, device_oid) ;

    int off_device_command_oid=device_command_oid;    
    
//Toggle output event - switch output state
        add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "02")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (Toggle)", "NULL", protocol_oid, device_oid) ;

    int toggle_device_command_oid=device_command_oid;
    
////Get status command from java server
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.OutputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.OutputNames[i] ,4,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    //
    
//Add switch for changing state use ON/OFF
   add_switch(se.OutputNames[i]+"",account_oid,device_oid);
   add_command_ref_item("SWITCH_CMD_OFF_REF",off_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",on_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));

   
//Add switch for changing state use Toogle event   
   add_switch(se.OutputNames[i]+" (Toggle)",account_oid,device_oid);   
   add_command_ref_item("SWITCH_CMD_OFF_REF",toggle_device_command_oid, String.valueOf(switch_oid), "NULL", "NULL","NULL") ;
   add_command_ref_item("SWITCH_CMD_ON_REF",toggle_device_command_oid,"NULL","NULL", String.valueOf(switch_oid), "NULL");
   add_sensor_ref_item("SWITCH_SENSOR_REF","NULL",String.valueOf(switch_oid),String.valueOf(sensor_oid));
   
   

    }
        
/**
 * 
 * Perform All Digital Inputs
 * 
 * 
 */      
DeviceName=se.DeviceName+" - Inputs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsRM) upto=12;
if (se.IsEM) upto=12;
if (se.IsHM) upto=0;
for ( int i=0;i<upto;i++)
    {    
String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_INPUT_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    
    
    

    }





DeviceName=se.DeviceName+" - ADC Sensors";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsRM) upto=8;
if (se.IsEM) upto=8;
if (se.IsHM) upto=16;
for ( int i=0;i<upto;i++)
    {    
String param=se.SensorNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    int sensortype=TCPStatusServerHandler.SIGNAL_LM335_VALUE;
    if (i==0) sensortype=TCPStatusServerHandler.SIGNAL_INVPERCENT_VALUE;
    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(sensortype)+" "+
            ehousecommunication.ConvertAsciHex(i+1);

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","60000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.SensorNames[i]+" (Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status For custom sensor
    add_sensor("CUSTOM_SENSOR",se.SensorNames[i] +" (Value)",4,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 

    }



DeviceName=se.DeviceName+" - Dimmers";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

if (se.IsRM) upto=3;
if (se.IsHM) upto=3;
if (se.IsEM) upto=3;
for ( int i=0;i<upto;i++)
    {    
String param=se.DimmerNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_DIMMER_LEVEL)+" "+
            ehousecommunication.ConvertAsciHex(i+1);

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","60000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.DimmerNames[i]+" (Level)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status For custom sensor
//    add_sensor("CUSTOM_SENSOR",se.SensorNames[i] +" (Value)",4,account_oid,device_oid);
//    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
//    add_state("value","0",String.valueOf(sensor_oid));
    //add_state("off","off",String.valueOf(sensor_oid));
//for simple sensor    
//    add_sensor("SIMPLE_SENSOR",se.SensorNames[i]+" (Value)" ,0,account_oid,device_oid);
//    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    
/* range sensor
 *       1 Query insert into sensor (account_oid, device_oid, name, type, dtype) values (1, 3, 'sensor_range_1..123', 2, 'RANGE_SENSOR')
      1 Query insert into range_sensor (max_value, min_value, oid) values (123, 1, 7)
      1 Query insert into command_ref_item (target_device_command_oid, sensor_oid, type) values (5, 7, 'SENSOR_CMD_REF')

 * 
 * 
 */
add_sensor("RANGE_SENSOR",se.DimmerNames[i]+" (Value)" ,2,account_oid,device_oid);    
add_range_sensor (255, 0, sensor_oid);
add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
   

    }












//limit nr of outputs for device types
/**
 * 
 * Perform All Output Programs button
 * 
 * 
 */

DeviceName=se.DeviceName+" - Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB


if (se.IsRM) upto=24;
if (se.IsEM) upto=24;
if (se.IsHM) upto=24;
////Get status command from java server
String querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    if (se.IsEM )
        {
        add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
        add_sensor("CUSTOM_SENSOR","Current Security Program" ,0,account_oid,device_oid);
        }
    else 
        {
        add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
        add_sensor("CUSTOM_SENSOR","Current Program" ,0,account_oid,device_oid);
        }
    
    
//Add Sensor for querying device status
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
      

for ( int i=0;i<upto;i++)
    {    
    String param=se.ProgramNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
        
    String outtemplate="";
    if (se.IsHM) 
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "aa"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    else
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            "02"+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;
    
   

   

   

    }
        


/**
 * 
 * Perform All Active Alarm sensors for EM
 * 
 
 * 
 */      

if (se.IsEM) 
{
upto=84;    

DeviceName=se.DeviceName+" - Active";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

//if (se.IsERM) upto=12;
//if (se.IsCommManager) upto=48;
//if (se.IsLevelManager) upto=48;
for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;
    
     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ACTIVE_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    

    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Active - Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    

    
    
   
    }    
    
/**
 * 
 * Perform All Active Inputs for CM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Warning";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_WARNING_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
        
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Active - Warning)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
    
   
   
    }    


    
    
/**
 * 
 * Perform All Active Inputs for EM
 * 
 
 * 
 */      
DeviceName=se.DeviceName+" - Alarm";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB

for ( int i=0;i<upto;i++)
    {    
    String param=se.InputNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

     querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_ALARM_STATE)+" "+
            ehousecommunication.ConvertAsciHex(i+1);
    

    
   
   
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+querytemp,protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.InputNames[i]+" (Alarm - Status)", "NULL", protocol_oid, device_oid) ;


    
    
//Add Sensor for querying device status
    add_sensor("CUSTOM_SENSOR",se.InputNames[i] ,0,account_oid,device_oid);
    add_command_ref_item("SENSOR_CMD_REF", device_command_oid, "NULL", "NULL", "NULL",String.valueOf(sensor_oid)); 
    add_state("on","on",String.valueOf(sensor_oid));
    add_state("off","off",String.valueOf(sensor_oid));
      
     
     
    }    
    
DeviceName=se.DeviceName+" - Security Programs";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
upto=24;    
    
////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_PROGRAM)+" "+
            ehousecommunication.ConvertAsciHex(11);
    
/*    add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);
    add_protocol_attr("ipAddress",ehousecommunication.localhost,protocol_oid);
    add_protocol_attr("command",querytemp,protocol_oid);
    if (se.IsCommManager) add_device_command("Current Security Program (Status)", "NULL", protocol_oid, device_oid) ;
    else add_device_command("Current Program (Status)", "NULL", protocol_oid, device_oid) ;
*/
for ( int i=0;i<upto;i++)
    {
    String param=se.ProgramNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(98)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Program
    /*add_protocol( "TCP/IP");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command",outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;*/
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ProgramNames[i], "NULL", protocol_oid, device_oid) ;



   

   

    }
        



/*
DeviceName=se.DeviceName+" - Zone";              //Device name set controller name + signal type for catalogs
add_device();                                       //add device to beehive DB
    
    
upto=0;    

////Get status command from java server
 querytemp=
            ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(TCPStatusServerHandler.SIGNAL_CURRENT_ZONE)+" "+
            ehousecommunication.ConvertAsciHex(1);
    
for ( int i=0;i<upto;i++)
    {    
    String param=se.
            ZoneNames[i];
    if (param==null)    continue;
    if (param.length()==0)    continue;
    if (param.indexOf("@")>=0)    continue;

    String outtemplate="";
    
            outtemplate=ehousecommunication.ConvertAsciHex(se.DevAdrH)+" "+
            ehousecommunication.ConvertAsciHex(se.DevAdrL)+" "+
            ehousecommunication.ConvertAsciHex(99)+" "+
            ehousecommunication.ConvertAsciHex(i)+" "+
            "ss"+" ";
    
//set Zone
    add_protocol( "Telnet");            
    add_protocol_attr("port",ehousecommunication.OpenRemotePort,protocol_oid);        
    add_protocol_attr("ipAddress",ehousecommunication.orServerHost,protocol_oid);
    add_protocol_attr("command","null|"+outtemplate.replaceAll("ss", "00")+" 00 00 00 00 00",protocol_oid);
    add_protocol_attr("statusDefault","None",protocol_oid);
    add_protocol_attr("pollingInterval","10000",protocol_oid);
    add_protocol_attr("statusFilter","^(?i)(?s)(?m)(.*)$",protocol_oid);
    add_protocol_attr("statusFilterGroup","1",protocol_oid);
    add_protocol_attr("timeout","10",protocol_oid);
    add_device_command(se.ZoneNames[i], "NULL", protocol_oid, device_oid) ;


   

   

    }
  */      
//System.exit(0);


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
   


}






//l("Before db close");
CloseDB(DBConnection);
//l("after db close");
//System.exit(0);
      
        
        
        
        

//device_oid++;      //in add device  
//}
}
catch (Exception e)        
    {
    l("Exc ddfa: ");
    ehousecommunication.ll(e);
//    System.exit(0);
    }

}



}
