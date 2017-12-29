/**
 * ethernet controllers - commmanager ethernet roommanager, levelmanager status cache instance for single Ethernet controller gathering all names, states, values, calculation 
 * @author Robert Jarzabek 
 * http://www.isys.pl/
 * http://inteligentny-dom.ehouse.pro/
 * http://home-automation.ehouse.pro/
 * http://www.ehouse.pro/
 * http://sterowanie.biz/
 * 
*/
package ehouse4openremote;
import java.util.Calendar;
import java.io.BufferedWriter;
import java.io.FileWriter;
public class StatusEthernet
  { 
public String recent="";                                        //recent status date/time
public String charset="";
public static String sep=System.getProperty("file.separator");  //file system separator slash or backslash
public boolean InitializedMatrix=false; 
public long CheckSum=0;
public long PrevCheckSum=0;
public boolean XmlChanged=false;
public double MCP9700x=10000;   // uV/C
public double MCP9701x=19500;   // uV/C
public double MCP9700_Offset=-500; //Offset voltage at 0C [mV]
public double MCP9701_Offset=-400; //Offset voltage at 0C [mV]
int STATUS_INDEX = -1;
static int OUTPUTS_COUNT_RM = 256;          //maximal count of outputs in current controller
static int ADC_PROGRAMS_COUNT_RM=12;        //adc programs count in current controller
static int INPUTS_COUNT_RM = 128;           //input count in current controller
static int SENSORS_COUNT_RM = 16;           //adc sensors / inputs count in current controller
static int ZONES_MAX=21;                    //max zones count in current controller (for CM)
static int DIMMERS_COUNT_RM=3;        //maximal single dimmers count in current controller
static int DMX_DIMMERS_COUNT_RM=32;
static int DIMMERS_RGB_COUNT_RM=1;    //maximal rgb (triple) dimmers count in current controller
static int PROGRAMS_COUNT_RM      = 24;//maximal programs count in current controller
public String[] ProgramNames        = new String[PROGRAMS_COUNT_RM];    //program names in cache
public String[] ZoneNames        = new String[ZONES_MAX];               //zone names for CM
public String[] ADCProgramNames        = new String[ADC_PROGRAMS_COUNT_RM]; //adc program names
public String[] DimmerNames         = new String[DIMMERS_COUNT_RM];    //dimmers names
public String[] DimmerRGBNames      = new String[DIMMERS_RGB_COUNT_RM];    //RGB dimmers names
public String[] DMXDimmerNames         = new String[DMX_DIMMERS_COUNT_RM];    //dimmers names
public String[] DMXDimmerRGBNames      = new String[DMX_DIMMERS_COUNT_RM/3];    //RGB dimmers names
            //static int ADC_SENSORS_COUNT = 16;
static int SENSORS_COUNT_ALARM = 128;   //security sensors count
static int STATUS_SIZE = 256;           //size of status buffer
public int[] Dimmer                 = new int[DIMMERS_COUNT_RM];        //dimmer state
public int[] DimmerRGB              = new int[DIMMERS_RGB_COUNT_RM];    //rgb dimmer state
public int[] DMXDimmer              = new int[DMX_DIMMERS_COUNT_RM];        //dimmer state
public int[] DaliDimmer                 = new int[DALI_CHANNELS];        //dimmer state
public int[] DMXDimmerRGB              = new int[DMX_DIMMERS_COUNT_RM/3];    //rgb dimmer state

public String XML="";
        /// All necessary data for visualisation (names and status)
public String DeviceName;                                   //current device name
public int StatusCurrentSize=0;             //status size (prefix to current status buffer)
public String[] OutputNames     = new String[OUTPUTS_COUNT_RM];    //name of outputs
public String[] DriveNames     = new String[OUTPUTS_COUNT_RM/2];    //name of outputs
//public int    DeviceNameORID=0;                 //open remote Device ID
//public String ModelNameOR="";
//public int[]    OutputORID=new int[OUTPUTS_COUNT_RM];   //open remote output id
public String[] InputNames      = new String[INPUTS_COUNT_RM];      //list of input names
public String[] SensorNames     = new String[SENSORS_COUNT_RM];                     //list of adc input names
public int[] SensorType= new int[SENSORS_COUNT_RM];
//            public Double[] SensorLM335Temps     = new Double[SENSORS_COUNT_RM];
public Double[] SensorTempsLM35     = new Double[SENSORS_COUNT_RM];     //adc calculated value for LM35 temperature sensor connected to the input
public Double[] SensorTempsMCP9700     = new Double[SENSORS_COUNT_RM];  //adc calculated value for mcp9700 temperature sensor connected to the input
public Double[] SensorTempsMCP9701     = new Double[SENSORS_COUNT_RM];  //adc calculated value for mcp9701 temperature sensor connected to the input
public String[] SensorTypeName  = new String[SENSORS_COUNT_RM];         //Name of sensor type
public int[] SensorABSValues    = new int[SENSORS_COUNT_RM];            //Sensor absolute value 0..1023 (for 10bit ADC)
public int[] ADCHLevel= new int[SENSORS_COUNT_RM];
public int[] ADCLLevel= new int[SENSORS_COUNT_RM];
public int[] Calibration        = new int[SENSORS_COUNT_RM];            //calibrations value * 100 for each ADC input
public Double[] SensorTemps     = new Double[SENSORS_COUNT_RM];         //adc calculated value for LM335 temperature sensor connected to the input
public Double[] SensorPercents  = new Double[SENSORS_COUNT_RM];         //adc calculated percent value
public Double[] SensorLights    = new Double[SENSORS_COUNT_RM];         //adc calculated inverted percent (or light) value for fototransistor sensor
public Double[] SensorVolts     = new Double[SENSORS_COUNT_RM];         //adc calculated value for voltage in reference to Power supply value or Vref
public Boolean[] OutputStates   = new Boolean[OUTPUTS_COUNT_RM];        //output states
public Boolean[] InputStates    = new Boolean[INPUTS_COUNT_RM];         //input states
public Boolean[] InputInverts    = new Boolean[SENSORS_COUNT_ALARM];//INPUTS_COUNT_RM];        //input inversion flags
public Boolean[] AlarmSensorsActive     = new Boolean[SENSORS_COUNT_ALARM];     //alarm sensors activity state
public Boolean[] AlarmSensorsWarning = new Boolean[SENSORS_COUNT_ALARM];        //alarm sensors warning state
public Boolean[] AlarmSensorsMonitoring= new Boolean[SENSORS_COUNT_ALARM];      //alarm sensors monitoring state
            //public Boolean[] MonitoringSensorsWarning = new Boolean[SENSORS_COUNT_ALARM];   //
            public Boolean[] AlarmSensorsAlarm = new Boolean[SENSORS_COUNT_ALARM];          // alarm sensors alarm states
            public String[] AlarmSensorsNames = new String[SENSORS_COUNT_ALARM];            //Alarm sensors names

            public int DevAdrH;                                                 //device address h
            public int DevAdrL;                                                 //device address l
            public String DevAdr;                                               //combined device address
            public boolean changed;                                             //flag to set if changed value from previous query status
            public boolean AdcChanged;                                          //adc changed from previous query status
            //public static DateTime LastRead;
            public int CurrentProgram=0;                                          //Current program no.
            public String CurrentProgramName="";                                //current program name
            public int ADCCurrentProgram=0;                                       //ADC current program
            public String ADCCurrentProgramName="";                             //ADC current program name
            public int CurrentZone=0;                                             //current security zone no.
            public String CurrentZoneName="";                                   //current security zone name
            public Byte[] CurrentStatus = new Byte[STATUS_SIZE];                //current status received from the controller (binary)
            //public Byte[] PreviousStatus = new Byte[STATUS_SIZE];
//devices types            
            public boolean IsBatch; //batch not common device
            /*public boolean IsRM;                                              //stored in StatusEhouse for Ehouse 1 devs cache
            public boolean IsHM;
            public boolean IsEM;
            public boolean IsAlarm;
            public boolean IsEthernet;*/
        public boolean IsCommManager;      	//communicationManager
        public boolean IsLevelManager;                                          //LevelManager
        public boolean IsERM;            	//TCP roommanager
        public boolean IsEEM;            	//TCP em
        public boolean IsEPool;            	//TCP em
        public boolean IsEHM;              	//indywidual heat manager                    
        public boolean IsWiFi;                                                   //Ethernet Heat Manager                    
        public boolean IsAlarm;             //is security system
        public boolean IsEthernet;
        public int VCC; //power suply for calibration * 100
        //final static int STATUS_EHOUSE1_DEVS		=0+StatusEhouse.STATUS_OFFSET;			//miejsce na status urządzeń podłączonych do RS485
//INDEXES in current status buffer (binary status information) / location information
        final static int STATUS_TCP_OFFSET=2;                   //offset in current status for data
        final static int STATUS_COMMMANAGER_OFFSET      =70;    //offset for storing CommManager status attached to eHouse 1 devices status in hybrid mode
                                                                //Ehouse1 under CommManager supervisor
//in reference to STATUS_COMMMANAGER_OFFSET each ethernet controllers
        final static int STATUS_ADC_ETH			=0;    			//adc values * 2B
        final static int STATUS_ADC_ETH_END		=32;                    //end of adc values
//STATUS_OUT_ETH			=STATUS_ADC_ETH_END;	//4*8 32		
//STATUS_INPUTS			=STATUS_OUT_ETH+4;		//4*8 32		
        final static int STATUS_OUT_I2C			=STATUS_ADC_ETH_END;        //i2c output buffers states  //max=160 outputs
        final static int STATUS_INPUTS_I2C		=STATUS_OUT_I2C+20;         //i2c input buffers states //max 96 inputs
        final static int STATUS_ALARM_I2C		=STATUS_INPUTS_I2C+12;      //--|--- for alarm states in case of CM
        final static int STATUS_WARNING_I2C		=STATUS_ALARM_I2C+12;       //--|--- for warning states in case of CM
        final static int STATUS_MONITORING_I2C	        =STATUS_WARNING_I2C+12;     //--|--- for monitoring states in case of CM
        final static int STATUS_PROGRAM_NR		=STATUS_MONITORING_I2C+12;  //--|--- for alarm states in case of CM
        final static int STATUS_ZONE_NR			=STATUS_PROGRAM_NR+1;       //current program no.
        final static int STATUS_ADC_PROGRAM		=STATUS_ZONE_NR+1;          //current adc program no.
        final static int STATUS_LIGHT_LEVEL		=STATUS_ADC_PROGRAM+2;		//dimmer levels
        final static int STATUS_PROFILE_RM              =180-1;
        final static int  STATUS_LIGHT                  =(180-5);
        final static int  STATUS_DIMMERS                =4;
        final static int STATUS_DMX_DIMMERS2            =STATUS_OUT_I2C+5;
        final static int STATUS_ADC_LEVELS		=24;
        final static int STATUS_MORE            =64;    //overlaps status adc levels last 8bytes
        final static int STATUS_DALI                    =STATUS_INPUTS_I2C+2;        
        
        final static int  STATUS_ZONE_NO		=(180-2);			//NUMER STREFY ZABEZPIECZEn
////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final static int ADC_N_OFFSET = 4;              //adc current levels index in status (except CM)
        final static int O_N_OFFSET=ADC_N_OFFSET+16;    //direct output states index in status (except CM)
        final static int I_N_OFFSET=O_N_OFFSET+35;      //direct input states index in status (except CM)
//////////////////////////////////////////////////////////////////////////////////////////////////////////        
        final static int DALI_CHANNELS =46;
//////////////////////////////////////////////////////////////////////////////
public void SetStatusIndex(int nr)
          {
          STATUS_INDEX=nr;
          }
//////////////////////////////////////////////////////////////////////////////////////////////////////////      
public double calculate_voltage(int dta)
            {
    
        
             double tmp = Math.round((double)((dta *  VCC ) / 1023)*100);///10000000;  //do 2 miejsc po przecinku
             tmp=tmp/100000;
                //tmp = (tmp) / 100;
            return tmp;
            }
////////////////////////////////////////////////////////////////////////////
public double calculate_apds9600(int dta,double k)
            {
    
        double tmp = ((double)((dta) / 1023)*1650);///10000000;  //do 2 miejsc po przecinku
        //     double tmp = ((double)((dta/*VCC  ) / 1023)*100);///10000000;  //do 2 miejsc po przecinku
             //tmp=((double)Math.round((tmp/6)*10*1000))/10;
                //tmp = (tmp) / 100;
            return tmp;
            }

////////////////////////////////////////////////////////////////////////////////////////

public double calculate_MCP9700(int dta)
            {
    
        double tmp = ((double)((dta *  VCC ) / 1023))+MCP9700_Offset;
             //get voltage in [mv]
             tmp=((double)Math.round((tmp/MCP9700x)*10*1000))/10;
                
            return tmp;
            }

/////////////////////////////////////////////////////////////////////////
public double calculate_MCP9701(int dta)
            {
    
        double tmp = ((double)((dta *  VCC ) / 1023))+MCP9701_Offset ;
             //get voltage in [mv]
             tmp=((double)Math.round((tmp/MCP9701x)*10*1000))/10;
                
            return tmp;
            }
            

///////////////////////////////////////////////////////////////////////////////
/***
 * Create XML file with combined status and data of current controller
 * for information or logging
 * @return 
 */
 public Thread  MakeXml=new Thread()
 {
        @Override
    public void run()
        {    
            //this.
            setPriority(Thread.MIN_PRIORITY);
while (!ehousecommunication.Terminate)            
{
while (!XmlChanged)     
    {
    try
        {    
        sleep(200);
        }
    catch (Exception e) //exception
        {
        
        }
    if (ehousecommunication.Terminate) return;
    }
String eol,Event;
XmlChanged=false;
eol="\r\n";
XML="";
String header,footer,body,bodyheader,bodyfooter;    
header="<?xml version=\"1.0\" encoding=\"UTF-8\" ?> \r\n <eHouse xmlns=\"http://www.isys.pl\" \r\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n            xsi:schemaLocation=\"http://www.openremote.org protocol.xsd\">";
header+="<Device>"+eol;
header+="<Name>"+DeviceName+"</Name>"+eol;
header+="<Address>"+DevAdrH+","+DevAdrL+"</Address>"+eol;
header+="<IPAddress>192.168."+DevAdrH+"."+DevAdrL+"</IPAddress>"+eol;            
header+="<AddressCombined>"+DevAdr+"</AddressCombined>"+eol;
header+="<StateModified>"+ehousecommunication.bo(changed)+"</StateModified>"+eol;
header+="<ADCModified>"+ehousecommunication.bo(AdcChanged)+"</ADCModified>"+eol;
header+="<CurrentProgram>"+ehousecommunication.i(CurrentProgram)+"</CurrentProgram>"+eol;
header+="<CurrentProgramName> "+ CurrentProgramName+" </CurrentProgramName>"+eol;
header+="<ADCCurrentProgram>"+ehousecommunication.i(ADCCurrentProgram)+"</ADCCurrentProgram>"+eol;
header+="<ADCCurrentProgramName> "+ ADCCurrentProgramName+" </ADCCurrentProgramName>"+eol;
header+="<CurrentStatus>"+ehousecommunication.hx(CurrentStatus,STATUS_SIZE)+"</CurrentStatus>"+eol;
header+="<RecentStatus>"+recent+"</RecentStatus>"+eol;
if (IsERM) header+="<Devicetype>"+"EthernetRoomManager"+"</Devicetype>"+eol;
if (IsEPool) header+="<Devicetype>"+"EthernetPoolManager"+"</Devicetype>"+eol;
if (IsWiFi) header+="<Devicetype>"+"WiFi"+"</Devicetype>"+eol;
if (IsCommManager) header+="<Devicetype>"+"CommManager"+"</Devicetype>"+eol;
if (IsEHM ) header+="<Devicetype>"+"EthernetRoomManager"+"</Devicetype>"+eol;
if (IsLevelManager) header+="<Devicetype>"+"LevelManager"+"</Devicetype>"+eol;
if (IsEthernet)  header+="<InterfaceType>"+"Ethernet"+"</InterfaceType>"+eol;;
        if (IsCommManager)       //communicationManager
            {
            header+="<CurrentZone>"+ehousecommunication.i(CurrentZone)+"</CurrentZone>"+eol;
            header+="<CurrentZoneName>"+ CurrentZoneName+"</CurrentZoneName>"+eol;

            }
bodyheader=eol+"<Outputs>"+eol;
body="";
int maxio=OUTPUTS_COUNT_RM;
if ((IsERM) || (IsEHM) || (IsEEM) || (IsEPool)) maxio=35;
if ((IsWiFi) ) maxio=4;
for (int i=0;i<maxio;i++)
{    
if (((!ehousecommunication.IgnoreAtChar) || (OutputNames[i].indexOf("@")<0)))    
    {
    int tmp[]={DevAdrH,DevAdrL,0x21,0,1,0,0,0,0,0};
    tmp[3]=i;
    String state="0"; String value="Off";   if (OutputStates[i]) {state="1";value="On";}
    body+="    <Item>"+eol+
    "        <Name>"+OutputNames[i]+"</Name>"+eol+
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <State>"+state+"</State>"+eol+
    "        <Value>"+value+"</Value>"+eol;
    Event=ehousecommunication.hix(tmp,10);
    body+="        <EventOn>"+Event+"</EventOn>"+eol;
    tmp[4]=0;Event=ehousecommunication.hix(tmp,10);
    body+="        <EventOff>"+Event+"</EventOff>"+eol;
    tmp[4]=2;Event=ehousecommunication.hix(tmp,10);
    body+="        <EventToggle>"+Event+"</EventToggle>"+eol            
    +"    </Item>"+eol+eol;
    }
}
bodyfooter=eol+"</Outputs>"+eol;

header+=bodyheader+body+bodyfooter+eol;
bodyheader=eol+"<ADCInputs>"+eol;
body=eol;
bodyfooter=eol+"</ADCInputs>"+eol;
maxio=SENSORS_COUNT_RM;
for (int i=0;i<maxio;i++)
{
 if (((!ehousecommunication.IgnoreAtChar) || (SensorNames[i].indexOf("@")<0)))    
    {
        body+="    <Item>"+eol+
    "        <Name>"+SensorNames[i]+"</Name>"+eol+
    "        <ADCValue>"+SensorABSValues[i]+"</ADCValue>"+eol+
    "        <ADCTempValue>"+SensorTemps[i]+"</ADCTempValue>"+eol+
    "        <ADCLM335TempValue>"+SensorTemps[i]+"</ADCLM335TempValue>"+eol+
    "        <ADCLM35TempValue>"+SensorTempsLM35[i]+"</ADCLM35TempValue>"+eol+            
    "        <ADCMCP9700TempValue>"+SensorTempsMCP9700[i]+"</ADCMCP9700TempValue>"+eol+
    "        <ADCMCP9701TempValue>"+SensorTempsMCP9701[i]+"</ADCMCP9701TempValue>"+eol+
    "        <ADCPercentValue>"+SensorPercents[i]+"</ADCPercentValue>"+eol+
    "        <ADCInvertedPercentValue>"+SensorLights[i]+"</ADCInvertedPercentValue>"+eol+
    "        <ADCVoltageValue>"+SensorVolts[i]+"</ADCVoltageValue>"+eol+
    "        <ADCCalibrationValue>"+Calibration[i]+"</ADCCalibrationValue>"+eol+
    
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <SensorType>"+SensorType[i]+"</SensorType>"+eol+
    "        <SensorTypeName>"+SensorTypeName[i]+"</SensorTypeName>"+eol;
    //Event=hix(tmp,10);
    //body+="        <EventOn>"+Event+"</EventOn>"+eol;
    //tmp[4]=0;Event=hix(tmp,10);
    //body+="        <EventOff>"+Event+"</EventOff>"+eol;
    //tmp[4]=2;Event=hix(tmp,10);
    //body+="        <EventToggle>"+Event+"</EventToggle>"+eol            
    /*String inversion="Normal";
    if (InputInverts[i]) inversion="Inverted";
     body+="        <Type>"+inversion+"</Type>"+eol;
     */
     body+="    </Item>"+eol+eol;
    }
}

header+=bodyheader+body+bodyfooter+eol;



bodyheader=eol+"<Inputs>"+eol;
body=eol;
bodyfooter=eol;
maxio=INPUTS_COUNT_RM;
if ((IsERM) || (IsEHM) || (IsEEM) || (IsEPool)) maxio=12;
if (IsWiFi) maxio=4;
for (int i=0;i<maxio;i++)
{
    
if (((!ehousecommunication.IgnoreAtChar) || (InputNames[i].indexOf("@")<0)))    
{
//    int tmp[]={DevAdrH,DevAdrL,0x21,0,1,0,0,0,0,0};
  //  tmp[3]=i;
    String state="0"; String value="Off";   
    /*if ((IsCommManager) || (IsLevelManager)) 
        {
        if (!((InputStates[i])^(InputInverts[i])))
            {state="1";value="Off";} 
        else
            {state="0";value="On";}
    
        }
    else
        {
        if ((InputStates[i])^(InputInverts[i]))
            {state="0";value="On";} 
        else
            {state="1";value="Off";}
        }*/
    if (((InputStates[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}
    
    body+="    <Item>"+eol+
    "        <Name>"+InputNames[i]+"</Name>"+eol+
            
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <State>"+state+"</State>"+eol+
    "        <Value>"+value+"</Value>"+eol;
    if ((IsCommManager) || (IsLevelManager)) 
        {

            
if (((AlarmSensorsActive[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}            
        /*if (!((AlarmSensorsActive[i])^(InputInverts[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}*/
        body+="        <ActiveState>"+state+"</ActiveState>"+eol+
        "        <ActiveValue>"+value+"</ActiveValue>"+eol;
          
if (((AlarmSensorsWarning[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}            
/*            
        if (!((AlarmSensorsWarning[i])^(InputInverts[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}*/
        body+="        <WarningState>"+state+"</WarningState>"+eol+
        "        <WarningValue>"+value+"</WarningValue>"+eol;
        
        //MonitoringSensorsWarning
        if (((AlarmSensorsMonitoring[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}
        /*if (!((AlarmSensorsMonitoring[i])^(InputInverts[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}*/
        body+="        <MonitoringState>"+state+"</MonitoringState>"+eol+
        "        <MonitoringValue>"+value+"</MonitoringValue>"+eol;
        
        if (((AlarmSensorsAlarm[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}
       /*if (!((AlarmSensorsAlarm[i])^(InputInverts[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}*/
        body+="        <AlarmState>"+state+"</AlarmState>"+eol+
        "        <AlarmValue>"+value+"</AlarmValue>"+eol;
        
        
        
        
        }
    //Event=hix(tmp,10);
    //body+="        <EventOn>"+Event+"</EventOn>"+eol;
    //tmp[4]=0;Event=hix(tmp,10);
    //body+="        <EventOff>"+Event+"</EventOff>"+eol;
    //tmp[4]=2;Event=hix(tmp,10);
    //body+="        <EventToggle>"+Event+"</EventToggle>"+eol            
    String inversion="Normal";
    if (InputInverts[i]) inversion="Inverted";
     body+="        <Type>"+inversion+"</Type>"+eol;
     
     body+="    </Item>"+eol+eol;
}
}
bodyfooter=eol+"</Inputs>"+eol;

header+=bodyheader+body+bodyfooter+eol;
footer="</Device>\r\n";
footer+="\r\n</eHouse> \r\n";

try {
    BufferedWriter out = new BufferedWriter(new FileWriter(ehousecommunication.path+"logs/"+DeviceName+".xml"));
    out.write(header+footer);
    out.flush();
    out.close();
} catch (Exception e) {
    //log()
}
XML= header+footer;
}
	}

    
 
};    

////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 * Check ADC status changed flag
 * @return 
 */
///////////////////////////////////////////////////////////////////////////////////////////////////////
public boolean ChkIsAdcChanged()
      {
          if (AdcChanged)
          {
              AdcChanged = false;
              return true;
          }
          return false;
      }
///////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 * Check device name of current device
 * @param dname - Device name for checking index
 * @return true if device name = dname
 */
public boolean IsDeviceName(String dname)
      {
          if (DeviceName == null) return false;
          if (DeviceName.length() == 0) return false;
      if (dname.length() > 0)
          {
          if (dname.compareTo(DeviceName) == 0) return true;
          }
      return false;
      }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 * 
 *  Calculate temperature of LM335 sensor value from ADC input
 * 
 * @param dta   --ADC value 0-1024 10bit
 * @param calibration   - offset for calibration
 * @param k
 * @param VCC  - supply voltage for reference
 * @return   double value
 */
private double gettemplm(int dta, int calibration, double k, int VCC)
    {
    double temp;
    temp=(dta*VCC/(1023*k))+calibration/100;
//t=Math.Round(temp*10);
//temp=(t/10);
    temp = Math.round(temp*10);
    temp/=10;
    return temp;
    }
/////////////////////////////////////////////////////////////////////////////
private double gethih(int dta, double calibration, double k)
    {
    double temp; //k=6.36 //calibration 15.15
    double data=dta;
    data/=1023;
    temp=1000*(data-(calibration/100))/(k);
//t=Math.Round(temp*10);
//temp=(t/10);
    temp = Math.round(temp*10);
    temp/=10;
    return temp;
    }
        



private double gettemplm35(int dta)
    {
    double temp;
    temp=(dta*VCC/(1023*10));
//t=Math.Round(temp*10);
//temp=(t/10);
    temp = Math.round(temp*10);
    temp/=10;
    return temp;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 *  Check integer value and calculate from string
 * @param stt
 * @param defaut
 * @return 
 */
private int check_in(String stt, int defaut)
    {        
    double dd = Double.valueOf(stt);
    int ino = (int)dd;
    //ino=st.IConvertible.ToInt32();
    return (ino);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 * Read alarm sernsor names from file and init data
 * @param pat 
 */
public void ReadAlarmSensorsNames(String pat)
{
    int i=0;
    int z=0;
    if (ehousecommunication.FileExists(pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(pat,charset);
                        while (AlarmS[i]!=null)
                        {
                            
                            {
                                AlarmSensorsActive[i] = false;
                                AlarmSensorsAlarm[i] = false;
                                InputInverts[i]=false;
//20121003                                MonitoringSensorsWarning[i]=false;
                                AlarmSensorsWarning[i] = false;
                                AlarmSensorsMonitoring[i]=false;
                            }
                            AlarmSensorsNames[i] = AlarmS[i];
                            
                            i++;
                            if (i == SENSORS_COUNT_ALARM) break;
                            //z++;
                            
                        }
                            while (i<SENSORS_COUNT_ALARM)
                            {
                            AlarmSensorsActive[i] = false;
                            AlarmSensorsAlarm[i] = false;
                            AlarmSensorsWarning[i] = false;
                            InputInverts[i]=false;
//20121003                            MonitoringSensorsWarning[i]=false;
                            AlarmSensorsMonitoring[i]=false;
                            AlarmSensorsNames[i] = "";
                            i++;
                            }
                            
                        }
}                  
      /******************************************************************************************************/
/**
 * Load Complete Configuration data for controller from config files
 * @param name 
 */
        public void LoadDta(String name)
        {
            String temp;
            int i;
//            try
            {
                if (ehousecommunication.FileExists(name))
                
                {
                
                    String st[]=ehousecommunication.getfile(name,charset);
                    DeviceName = st[1];                            //device name
                    
                    String adrh=st[0].substring(0,3);
                    String adrl=st[0].substring(3) ;                           
                    DevAdrH = check_in(adrh, 0);                                             //device addr h

                    DevAdrL = check_in(adrl, 0);                                             //device addr l
                    DevAdr = String.valueOf(DevAdrL); ;
                    DevAdr=st[0];
                    /*while (DevAdr.length()<3) DevAdr='0'+DevAdr;
                    DevAdr = String.valueOf(DevAdrH) + DevAdr;
                    while (DevAdr.length() < 6) DevAdr = '0' + DevAdr;                          //combined address
                    */
                    temp = st[2];
                    if (temp!=null)
                        {
                        if (temp.compareTo("CM") == 0) 
                                    {
                                    IsCommManager = true;
                                    ehousecommunication.CommManagerName=DeviceName;
                                    }     //communicationManager
                        if (temp.compareTo("LM") == 0)  IsLevelManager = true;     //LevelManager
                        if (temp.compareTo("ERM") == 0) IsERM = true;               //EthernetRoommanager
                        if (temp.compareTo("EW") == 0)  IsWiFi = true;               //WiFi
                        if (temp.compareTo("EHM") == 0) IsEHM = true;           //EthernetHeatManager
                        if (temp.compareTo("EEM") == 0) IsEEM = true;           //indywidual heat manager
                        if (temp.compareTo("POL") == 0) IsEPool = true;               //EthernetRoommanager
/*                        if (ehousecommunication.FileExists(ehousecommunication.path+DevAdr+sep+"pool.cfg"))
                            {
                             IsEPool=true;
                            }*/
                        }
                    String[] senstypes=new String[50];
                    String[] senstype=new String[SENSORS_COUNT_RM];
                    if (!IsWiFi)
                        {
                        VCC = 3300;
                        st=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"AdcNames.txt",charset);//adc input names 
                        if (ehousecommunication.FileExists(ehousecommunication.path+DevAdr+sep+"ADCSensorTypes.txt")) //adc sensor types list
                            senstypes=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"ADCSensorTypes.txt",charset); //adc sensor types list
                        else senstypes=ehousecommunication.getfile(ehousecommunication.path+"ADCSensorTypes.txt",charset); //adc sensor types list
                        senstype=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"AdcConfig.cfg","");        //adc sensors type
                        }
                    else
                        {
                        VCC = 1000;
/*                        SENSORS_COUNT_RM=4;
                        OUTPUTS_COUNT_RM=4;
                        INPUTS_COUNT_RM=4;
                        ADC_PROGRAMS_COUNT_RM=0;        //adc programs count in current controller
                        ZONES_MAX=0;                    //max zones count in current controller (for CM)
                        DIMMERS_COUNT_RM=3;        //maximal single dimmers count in current controller
                        DIMMERS_RGB_COUNT_RM=1;    //maximal rgb (triple) dimmers count in current controller
                        PROGRAMS_COUNT_RM = 0;//maximal programs count in current controller
  */          
                        DriveNames=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"RollerNamesWiFi.txt",charset);//adc input names 
                        DimmerNames=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"DimmerNamesWiFi.txt",charset);//adc input names 
                        DimmerRGBNames=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"RGBDimmerNamesWiFi.txt",charset);//adc input names 
                        st=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"AdcNamesWiFi.txt",charset);//adc input names 
                        if (ehousecommunication.FileExists(ehousecommunication.path+DevAdr+sep+"ADCSensorTypes.txt"))
                                {
                                    senstypes=ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"ADCSensorTypes.txt",charset); //adc sensor types list                        
                                }
                        else 
                                {
                                senstypes=ehousecommunication.getfile(ehousecommunication.path+"ADCSensorTypes.txt",charset); //adc sensor types list                        
                                }
                        //senstype=new String[SENSORS_COUNT_RM];
                        senstype[0]="2";//voltage =ehousecommunication.getfile(ehousecommunication.path+DevAdr+sep+"AdcConfig.cfg");        //adc sensors type
                        senstype[1]="3";//percent
                        senstype[2]="5";//temp mcp9700
                        senstype[3]="7";//absolute
                        
                        }
                    for (i = 0; i < SENSORS_COUNT_RM; i++)
                         {
                        if (st[i]==null) st[i]="@ADC"+String.valueOf(i+1);
                        if (st[i].length()==0) st[i]="@ADC"+String.valueOf(i+1);
                        SensorNames[i] = st[i];
                        int senstyp=0;
                        try
                        	{
                            senstyp=Integer.parseInt(senstype[i]);
                            }
                        catch (Exception e)
                            {
                            senstyp=0;
                            }
                        SensorType[i]=senstyp;
                        SensorTypeName[i]=senstypes[senstyp];
                        if (SensorNames[i] == null) SensorNames[i] = "@"+String.valueOf(i+1);
                        SensorABSValues[i] = 0;
                        SensorTemps[i] = 0.0;      //sensor Temp value calibrated
                        SensorPercents[i] = 0.0;   //sensor percent value
                        SensorLights[i] = 0.0;
                        SensorVolts[i]=0.0;
                        Calibration[i] = -27315;
                        SensorTemps[i] = 0.0;
                        SensorTempsLM35[i] = 0.0;
                        SensorTempsMCP9700[i] = 0.0;
                        SensorTempsMCP9701[i] = 0.0;
                        }
                    // Set output names and initialize
                    if ((IsCommManager) || (IsLevelManager))
                         st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "AdditionalOutputNames.txt",charset);    //aditional inputs for large controllers LM,CM
                    else
                        if (IsWiFi)
                            st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+"OutputNamesWiFi.txt",charset);      //standard output names for current controler
                        else
                            st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+"OutputNames.txt",charset);      //standard output names for current controler
                    for (i = 0; i < OUTPUTS_COUNT_RM; i++)
                        {
                        if (st[i]==null) st[i]="@Out"+String.valueOf(i+1);
                        if (st[i].length()==0) st[i]="@Out"+String.valueOf(i+1);
                        OutputNames[i] = st[i].trim();
                        if (OutputNames[i] == null) OutputNames[i] = "@Out"+String.valueOf(i+1);
                        OutputStates[i] = false;
                        }
                    ///Get input names 
                    if (IsWiFi) st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "SensorNamesWiFi.txt",charset);
                        else
                            {
                            if (ehousecommunication.FileExists(ehousecommunication.path + DevAdr +sep+ "InputNames.txt"))          
                                st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "InputNames.txt",charset);
                            else
                                st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "SensorNames.txt",charset);                    
                            }
                    String inv[]=ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "SensorsInverts.cfg","");
                    for (i =0; i < INPUTS_COUNT_RM; i++)
                    
                            {
                            if (st[i]==null) st[i]="@In"+String.valueOf(i+1);
							if (st[i].length()==0) st[i]="@In"+String.valueOf(i+1);
                            AlarmSensorsNames[i] = st[i].trim();
                            InputNames[i] = st[i];
                            InputStates[i]=false;
                            InputInverts[i]=false;
                            if (inv[i]!=null)
                                if (inv[i].trim().equalsIgnoreCase("1")) 
                                    InputInverts[i]=true;
                            //AlarmSensorsStates[i]=false;
                            AlarmSensorsActive[i] = false;
                            AlarmSensorsWarning[i] = false;
                            AlarmSensorsMonitoring[i] = false;
                            AlarmSensorsAlarm[i] = false;
                            }
                    
                    
                    ///Read program names
                    if (IsEPool)    //Pool
                        {
                        if  (ehousecommunication.FileExists(ehousecommunication.path + DevAdr+sep+ "PoolPrograms.txt"))          //(!IsCommManager)
                            {
                            st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "PoolPrograms.txt",charset);
                            for (i =0; i < PROGRAMS_COUNT_RM; i++)
                                {
                                if (st[i]==null) st[i]="@Program "+String.valueOf(i+1);
                                if (st[i].length()==0) st[i]="@Program"+String.valueOf(i+1);
                                ProgramNames[i] = st[i].trim();
                                }
                            }
                        }
                    else
                    {
                    if  (ehousecommunication.FileExists(ehousecommunication.path + DevAdr+sep+ "Programs.txt"))          //(!IsCommManager)
                    {
                    st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "Programs.txt",charset);
                    for (i =0; i < PROGRAMS_COUNT_RM; i++)
                            {
                            if (st[i]==null) st[i]="@Program "+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@Program"+String.valueOf(i+1);
                            ProgramNames[i] = st[i].trim();
                            }
                    }
                    else
                        if  (ehousecommunication.FileExists(ehousecommunication.path + DevAdr +sep+  "SecuPrograms.txt"))          //(!IsCommManager)
                            {
                            st = ehousecommunication.getfile(ehousecommunication.path +DevAdr +sep+ "SecuPrograms.txt",charset);
                            for (i =0; i < PROGRAMS_COUNT_RM; i++)
                                {
                                if (st[i]==null) st[i]="@Program "+String.valueOf(i+1);
                                  if (st[i].length()==0) st[i]="@Program"+String.valueOf(i+1);
                                ProgramNames[i] = st[i].trim();
                                }
                            }
                    
                }
//                    if (!IsEPool)
                    {
                    //read adc program names
                    if  (ehousecommunication.FileExists(ehousecommunication.path + DevAdr +sep + "AdcPrograms.txt"))          //(!IsCommManager)
                    	{
                    	st = ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+  "AdcPrograms.txt",charset);
                    	for (i =0; i < ADC_PROGRAMS_COUNT_RM; i++)
                            {
                            if (st[i]==null) st[i]="@ADC Program "+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@ADC Program "+String.valueOf(i+1);
                            ADCProgramNames[i] = st[i].trim();
                            }
						}
                    }
                    //read security zones names
                    if (IsCommManager)
                        if  (ehousecommunication.FileExists(ehousecommunication.path + DevAdr +sep+ "ZonesNames.txt"))          //(!IsCommManager)
                            {
                            st = ehousecommunication.getfile(ehousecommunication.path +DevAdr +sep+ "ZonesNames.txt",charset);
                            for (i =0; i < ZONES_MAX; i++)
                                {
                                if (st[i]==null) st[i]="@Zone "+String.valueOf(i+1);
                                ZoneNames[i] = st[i].trim();
                                }
                            }
                    
                    changed = false;                                            //flag to set if changed value
                    //                    AdcChanged = false;
                    //LastRead = 0;
                    CurrentProgram = 0;
                    CurrentProgramName = "";
                    for (i = 0; i < STATUS_SIZE; i++)
                        {
                        CurrentStatus[i] = 0;
                        //                        PreviousStatus[i] = 0;
                        }
                    if (ehousecommunication.EnableXMLStatus) MakeXml.start();
                    IsBatch = false;
                    IsAlarm = false;
                    IsEthernet = true;
                }
            }
            /*catch (Exception ie)
            {
                sMessageBox.Show("Error Reading File: " + name + " " + ie.getMessage());
            }*/
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 * 
 * 
 * Calculate percent value of adc inputs
 * 
 * @param dta
 * @return 
 */        
public double calculate_percent(int dta)
            {
    
long ltmp = Math.round(((double)dta * 10000 )/1023);
                  double    tmp=ltmp/ 100;  //rounding to  2 digits of fractional part
                             //tmp = (tmp) / 100;
            return tmp;
            }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Get light value of sensor
 * @param dta - adc absolute value 0..1023
 * @return 
 */
public double getlight(int dta)
  {
 
  //double dtta=(1023-dta)*100;
      double dtta = (dta) * 100;
  dtta = ((((double)dtta) / 1023));
    return ((double)Math.round(dtta*10))/10;
  }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/***
 *  Get value of input (bit) from bytes in status
 * 
 * @param dta - data byte which consist 8 inputs/outputs states
 * @param offset - offset in byte to get single byte for output/input
 * @return state of output/input
 */
public boolean GetInOutValue(int dta, int offset)
    {
    int temp=dta>>offset;
    if ((temp & 0x01)>0) 
            return true;
    else return false;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
public boolean ginv(boolean val, boolean inv)
{
if (IsCommManager)  return val^inv;
else return (!val)^inv;
}
/////////////////////////////////////////////////////////////////////
public int[] GetOptsIndex(String EventName,int AlarmState)
    {
        int i;
    int[] m=new int[2];
    for (i=0;i<OUTPUTS_COUNT_RM;i++)
    {
        if (OutputNames[i].length()>0)
            if (EventName.compareTo(OutputNames[i])==0)
                {
                m[1]=i;
                m[0]=1;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
                }
    }
    for (i=0;i<INPUTS_COUNT_RM;i++)
    {
        if (InputNames[i].length()>0)
            if (EventName.compareTo(InputNames[i])==0)
                {
                m[1]=i;
                m[0]=2;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
                }
    }
    
    for (i = 0; i < SENSORS_COUNT_RM; i++)
    {
        if (SensorNames[i].length() > 0)
            if (EventName.compareTo(SensorNames[i]) == 0)
            {
                m[1] = i;
                m[0] = 3;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
            }
    }
    if (IsEEM)
    for (i = 0; i < SENSORS_COUNT_ALARM;i++ )
        if (AlarmSensorsNames[i].length() > 0)
        {
            if (EventName.compareTo(AlarmSensorsNames[i]) == 0)
            {
                m[1] = i;
                m[0] = 4 + AlarmState;
                return m;
            }
        }
        
    m[1] = 255;
    m[0] = 255;
    return m;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Decode binary status from UDP broadcast or TCP status and update internal cache
 * @param dta - status data from eHouse devices
 * @param InitFromPrevious - unconditional update status
 * @param CommManager - Offset for CommManager in case of Ehouse 1 under CommManager supervision (CommManager status attached  after eHouse1 controller status)
 * @return 
 */
public boolean WriteCheckIfChangedAndCpy(byte[] dta, boolean InitFromPrevious,boolean CommManager)

        { int i,k;
        int sss=0;
        EhouseTCP.FinishedEthernet=false;
        boolean tempboolean = false;
        boolean unconditional = InitFromPrevious;
        int TotalOffset=STATUS_TCP_OFFSET;
        //if (ehousecommunication.disablestatperforme) return true;
        if (CommManager) TotalOffset+=STATUS_COMMMANAGER_OFFSET;    //for commmanager offset for status ehouse1 devies
        int CurrentSize=dta[0]&0xff;

        int Max_ADC = 16;
        Calendar c = Calendar.getInstance(); 
        recent=c.getTime().toLocaleString();    //set recent query value for info
//            boolean changedd=false;
            if (InitFromPrevious) changed = true;       //unconditional
            else changed=false;
            if (!InitFromPrevious)
              //if (dta != null)
                if (PrevCheckSum!=CheckSum)
                    {
                    changed=true; 
                    //CurrentStatus=(dta);
                   for (i = 0; i < CurrentSize; i++)
                         {
                    
                    // if (CurrentStatus[i] != dta[i])
                            {
                            CurrentStatus[i] = dta[i];
                      //      changed = true;
                            }
                        }
                    }
                else 
                    if ((CheckSum==0))
                        {
                        for (i = 0; i < CurrentSize; i++)
                            {
                            if (CurrentStatus[i] != dta[i])
                                {
                                CurrentStatus[i] = dta[i];
                                changed = true;
                                }
                            }
                        }
            PrevCheckSum=CheckSum;
        //if (ehousecommunication.disablestatperforme) return true;
        if (changed) //data changed
        {   //update data
        
        //if ((IsERM) || (IsEEM)) Max_ADC=8;
        
        {
            
//            if ((IsERM) && (!InitFromPrevious))                CurrentStatus[STATUS_SIZE-1] = 0;
            
            //Update All Outputs
            for (k = 0; k < 8; k++)
                {   ///output states
                    int off=0;
                    for (sss=0;sss<160;sss+=8)
                        {
                        OutputStates[k+sss] = GetInOutValue(CurrentStatus[TotalOffset+STATUS_OUT_I2C+off]&0xff, k);
                        //UpdateIOStatus(OutputStates[k+sss],OutputViewH[k+sss],OutputViewL[k+sss],GRAPHICS_OBJECTS_MAX);
                        off++;
                        }
                }
                    
                    for (k = 0; k < 8; k++)
                    {   ///output states
                    int off=0;
                    for (sss=0;sss<96/*INPUTS_COUNT_RM*/;sss+=8)
                        {
                        InputStates[k+sss]= ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_INPUTS_I2C+off]&0xff, k),InputInverts[k+sss]);
                        //UpdateIOStatus(InputStates[k+sss],InputViewH[k+sss],InputViewL[k+sss],GRAPHICS_OBJECTS_MAX);
                        off++;
                        }
                    }
                    
                    //Security sensors status Alarm, Warning, Monitoring Mask violations
                if (IsCommManager)
                    {
                    for (k = 0; k < 8; k++)
                {   ///output states
                    int off=0;
                    for (sss=0;sss<96 /*INPUTS_COUNT_RM*/;sss+=8)
                        {
                        AlarmSensorsActive[k+sss]= ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_INPUTS_I2C+off]&0xff, k),InputInverts[k+sss]);
                        //UpdateIOStatus(AlarmSensorsActive[k+sss],ActiveViewSensorsH[k+sss],ActiveViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);
                        
                        AlarmSensorsAlarm[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_ALARM_I2C+off]&0xff, k),InputInverts[k+sss]);
                        //UpdateIOStatus(AlarmSensorsAlarm[k+sss],AlarmViewSensorsH[k+sss],AlarmViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        
                        AlarmSensorsWarning[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_WARNING_I2C+off]&0xff, k),InputInverts[k+sss]);
                        //UpdateIOStatus(AlarmSensorsWarning[k+sss],WarningViewSensorsH[k+sss],WarningViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        
                        AlarmSensorsMonitoring[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_MONITORING_I2C+off]&0xff, k),InputInverts[k+sss]);
                        //UpdateIOStatus(AlarmSensorsMonitoring[k+sss],MonitoringViewSensorsH[k+sss],MonitoringViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                                                
                        off++;
                        }
                }


                    }
                }
            if (IsCommManager)
                {
                CurrentProgram=CurrentStatus[TotalOffset+STATUS_PROGRAM_NR]&0xff;
                CurrentZone = CurrentStatus[TotalOffset+STATUS_ZONE_NR]&0xff;            
                }
            else
                {
                CurrentProgram=CurrentStatus[TotalOffset+STATUS_PROGRAM_NR]&0xff;
                CurrentZone = CurrentStatus[TotalOffset+STATUS_ZONE_NR]&0xff;            
                CurrentProgram=CurrentStatus[STATUS_PROFILE_RM]+1;
                DimmerRGB[0]=0;
                for (k=0;k<3;k++)
                    {
                    DimmerRGB[0]=DimmerRGB[0]<<8;
                    DimmerRGB[0]|=CurrentStatus[STATUS_DIMMERS+k]&0xff;
                    Dimmer[k]=((CurrentStatus[STATUS_DIMMERS+k]&0xff)*100)/255;
                    }
                //DimmerRGB[0]=Dimmer[0];
                
                for (k=3;k<20;k++)
                    {
                    DMXDimmer[k]=((CurrentStatus[STATUS_DIMMERS+k]&0xff)*100)/255;
                    }
                for (k=0;k<15;k++)
                    {
                    DMXDimmer[k+17]=((CurrentStatus[STATUS_DMX_DIMMERS2+k]&0xff)*100)/255;
                    }
                for (k=0;k<DALI_CHANNELS;k++) 
                    {
                    DaliDimmer[k]=((CurrentStatus[STATUS_DALI+k]&0xff)*100)/255;
                    }
                CurrentZone=CurrentStatus[STATUS_ZONE_NO];			//NUMER STREFY ZABEZPIECZEŃ
                ADCCurrentProgram=CurrentStatus[TotalOffset+STATUS_ADC_PROGRAM]+1;
                }
            CurrentProgramName="";
            if (CurrentZone<ZONES_MAX)
                CurrentZoneName=ZoneNames[CurrentZone];
            else CurrentZoneName="-";
			if (CurrentZoneName==null) CurrentZoneName="";
            ADCCurrentProgramName="";
            
            }
/*        if (IsTCPEM)
        {
            CurrentProgramName = "";
        }*/
/*        if (IsTCPHM) 
            {
            Max_ADC=16;
            CurrentProgram = CurrentStatus[HM_STATUS_PROGRAM];
            CurrentProgramName = "";//na razie
            for (k = 0; k < 8; k++)
                {
                int mm = 0;
                sss=k;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k] = GetInOutValue(CurrentStatus[HM_STATUS_OUT], k);
                sss=k+8;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k + 8] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 1], k); //bitoffset++; k++;
                sss=k+16;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k + 16] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 2], k); //bitoffset++; k++;
//                InputStates[k] = GetInOutValue(CurrentStatus[RM_STATUS_IN ], k);
  //              InputStates[k + 8] = GetInOutValue(CurrentStatus[RM_STATUS_INT ], k);
                }
            }*/
        i=0;
        while (i < Max_ADC)
                {   
                    //offset od 2                       //offset od 2
                //int temppp;
                //if (temppp<0) temppp+=256;
                 int temppp=((CurrentStatus[TotalOffset+(i<<1) ]&0x03) );
                 temppp=temppp<<8;
                 temppp+= CurrentStatus[TotalOffset+(i<<1) + 1 ]&0xff;
                 
                 if ((IsERM) /*&& (StatusMore==0)*/)  // && (DevAdrH==0) && (DevAdrL==201))
                    {
                    int adch=((CurrentStatus[TotalOffset+(i<<1) ]&0xff) >> 6);
                    int adcl=((CurrentStatus[TotalOffset+(i<<1) ]&0xff) >> 4)&0x3;
                    adch=adch<<8;
                    adcl=adcl<<8;
                    //int indexxx=i;
//                    if (i>3) indexxx--;
                    //if (i<13)
                    
                        {
                        adcl|=CurrentStatus[STATUS_ADC_LEVELS+/*STATUS_TCP_OFFSET*/+(i<<1)]&0xff;                        
                        adch|=CurrentStatus[STATUS_ADC_LEVELS+/*STATUS_TCP_OFFSET*/+(i<<1) + 1]&0xff;                            
                        ADCHLevel[i]=adch;
                        ADCLLevel[i]=adcl;
                        //ehousecommunication.l(String.valueOf(i)+  ") "+String.valueOf(ADCHLevel[i]) +" "+String.valueOf(ADCLLevel[i]));
                        }
                    }
                if (SensorABSValues[i] != temppp)
                    {
//                    Form1.ModifyDrawList(STATUS_INDEX, 3, i, tempbool);   //wyjścia 1-8
                    changed = true;
                    AdcChanged = true;
                    }
                    SensorABSValues[i] = temppp;
                    SensorTemps[i] = gettemplm(temppp, Calibration[i], 10, VCC);
                    SensorPercents[i] = calculate_percent(temppp);
                    SensorLights[i] = 100-SensorPercents[i]; //inverted percent
                    SensorTempsLM35[i]=gettemplm35(temppp);
                    SensorTempsMCP9700[i]=calculate_MCP9700(temppp);
//                  SensorTemps[i] =calculate_MCP9700(temppp);
                    SensorTempsMCP9701[i]=calculate_MCP9701(temppp);
                    SensorVolts[i]=calculate_voltage(temppp);
//                    UpdateADC(i, 5, 0, 10, SensorViewL[i],SensorViewH[i],SensorViewOK[i],GRAPHICS_OBJECTS_MAX);
                    i++;
                }
/*        int m=0;
        while (OtherViews[m].event_name!=null)
            {
            CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(OtherViews[m]);
            CurrentViewStat[CurrentViewStatIndex].Label_text=ChangeDynamic(CurrentViewStat[CurrentViewStatIndex].Label_text,CurrentViewStat[CurrentViewStatIndex]);
            CurrentViewStatIndex++;
            if(CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) 
            {
				EhouseTCP.FinishedEthernet=true;
                return false;
            //break;
            }
            m++;
            }
*/
if (changed)  
            {
                 XmlChanged=true;
            
            }
        EhouseTCP.FinishedEthernet=true;
        return changed;
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
public boolean WriteCheckIfChangedAndCpyWiFi(byte[] dta, boolean InitFromPrevious)

        { 
int STATUS_ADC_WIFI=4;            
int STATUS_DIMMERS_WIFI=STATUS_ADC_WIFI+8;
int STATUS_OUT_WIFI=STATUS_DIMMERS_WIFI+4;
int STATUS_INPUT_WIFI=STATUS_OUT_WIFI+1;            
int STATUS_PGM_WIFI=STATUS_INPUT_WIFI+1;
int STATUS_ADCPGM_WIFI=STATUS_PGM_WIFI+1;
         int i,k;
        int sss=0;
        EhouseTCP.FinishedEthernet=false;
        boolean tempboolean = false;
        boolean unconditional = InitFromPrevious;
        //int TotalOffset=STATUS_TCP_OFFSET;
        //if (ehousecommunication.disablestatperforme) return true;
        int CurrentSize=dta[0]&0xff;

        int Max_ADC = 4;
        Calendar c = Calendar.getInstance(); 
        recent=c.getTime().toLocaleString();    //set recent query value for info
//            boolean changedd=false;
            if (InitFromPrevious) changed = true;       //unconditional
            else changed=false;
            if (!InitFromPrevious)
                if (PrevCheckSum!=CheckSum)
                    {
                        
                    changed=true; 
                   for (i = 0; i < CurrentSize; i++)
                         {
                            CurrentStatus[i] = dta[i];
                         }
                    }
                else 
                    if ((CheckSum==0))
                        {
                        for (i = 0; i < CurrentSize; i++)
                            {
                            if (CurrentStatus[i] != dta[i])
                                {
                                CurrentStatus[i] = dta[i];
                                changed = true;
                                }
                            }
                        }
            PrevCheckSum=CheckSum;
        //if (ehousecommunication.disablestatperforme) return true;
        if (changed) //data changed
        {   //update data
//        ClearActiveVisualization();            //clear main visualization cache
        
        //if ((IsERM) || (IsEEM)) Max_ADC=8;
        
        {
            
            //if ((IsERM) && (!InitFromPrevious))                CurrentStatus[STATUS_SIZE-1] = 0;
            int off=0;
            //Update All Outputs
            //SENSORS_COUNT_RM=4;
            //            OUTPUTS_COUNT_RM=4;
            //            INPUTS_COUNT_RM=4;
            for (k = 0; k < OUTPUTS_COUNT_RM; k++)
                {   ///output states
                    
                    //for (sss=0;sss<160;sss+=8)
                        {
                        OutputStates[k] = GetInOutValue(CurrentStatus[STATUS_OUT_WIFI]&0xff, k);
  //                      UpdateIOStatus(OutputStates[k],OutputViewH[k],OutputViewL[k],GRAPHICS_OBJECTS_MAX);
                        //off++;
                        }
                }    
                    off=0;
                    for (k = 0; k < INPUTS_COUNT_RM; k++)
                    {   ///output states
                    //int off=0;
                    sss=0;
                    //for (sss=0;sss<96/*INPUTS_COUNT_RM*/;sss+=8)
                        {
                        InputStates[k]= ginv( GetInOutValue(CurrentStatus[STATUS_INPUT_WIFI]&0xff, k),InputInverts[k]);
//                        UpdateIOStatus(InputStates[k],InputViewH[k],InputViewL[k],GRAPHICS_OBJECTS_MAX);
                        //off++;
                        }
                    }
                    
                    //Security sensors status Alarm, Warning, Monitoring Mask violations

//                    }
                }
                {
                CurrentProgram=(CurrentStatus[STATUS_PGM_WIFI]>>3)&0xff;
                CurrentZone = 0;//CurrentStatus[TotalOffset+STATUS_ZONE_NR]&0xff;            
                CurrentProgram=CurrentStatus[STATUS_PROFILE_RM]+1;
                Dimmer[0]=CurrentStatus[STATUS_DIMMERS_WIFI];
                Dimmer[1]=CurrentStatus[STATUS_DIMMERS_WIFI+1];
                Dimmer[2]=CurrentStatus[STATUS_DIMMERS_WIFI+2];
                CurrentZone=0;//CurrentStatus[STATUS_ZONE_NO];			//NUMER STREFY zab
                ADCCurrentProgram=((CurrentStatus[STATUS_ADCPGM_WIFI]>>3)&0xff)+1;
                }
            CurrentProgramName="";
//            if (CurrentZone<ZONES_MAX)
                CurrentZoneName="";//ZoneNames[CurrentZone];
            //else CurrentZoneName="-";
//			if (CurrentZoneName==null) CurrentZoneName="";
            ADCCurrentProgramName="";
            
            }
/*        if (IsTCPEM)
        {
            CurrentProgramName = "";
        }*/
/*        if (IsTCPHM) 
            {
            Max_ADC=16;
            CurrentProgram = CurrentStatus[HM_STATUS_PROGRAM];
            CurrentProgramName = "";//na razie
            for (k = 0; k < 8; k++)
                {
                int mm = 0;
                sss=k;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k] = GetInOutValue(CurrentStatus[HM_STATUS_OUT], k);
                sss=k+8;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k + 8] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 1], k); //bitoffset++; k++;
                sss=k+16;UpdateIOStatus(OutputStates[sss],OutputViewH[sss],OutputViewL[sss],GRAPHICS_OBJECTS_MAX);
                OutputStates[k + 16] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 2], k); //bitoffset++; k++;
//                InputStates[k] = GetInOutValue(CurrentStatus[RM_STATUS_IN ], k);
  //              InputStates[k + 8] = GetInOutValue(CurrentStatus[RM_STATUS_INT ], k);
                }
            }*/
        i=0;
        while (i < SENSORS_COUNT_RM)
                {                                               //offset od 2                       //offset od 2
                int temppp= CurrentStatus[STATUS_ADC_WIFI+i * 2 +1]&0xff;
                //if (temppp<0) temppp+=256;
                 temppp+=((CurrentStatus[STATUS_ADC_WIFI+i * 2 ]&0xff) << 8);
                if (SensorABSValues[i] != temppp)
                    {
//                    Form1.ModifyDrawList(STATUS_INDEX, 3, i, tempbool);   //outs 1-8
                    changed = true;
                    AdcChanged = true;
                    }
                    SensorABSValues[i] = temppp;
                    SensorTemps[i] = gettemplm(temppp, Calibration[i], 10, VCC);
                    SensorPercents[i] = calculate_percent(temppp);
                    SensorLights[i] = 100-SensorPercents[i]; //inverted percent
                    SensorTempsLM35[i]=gettemplm35(temppp);
                    SensorTempsMCP9700[i]=calculate_MCP9700(temppp);
                    SensorTempsMCP9701[i]=calculate_MCP9701(temppp);
                    SensorVolts[i]=calculate_voltage(temppp);
//                    UpdateADC(i, 5, 0, 10, SensorViewL[i],SensorViewH[i],SensorViewOK[i],GRAPHICS_OBJECTS_MAX);
                    i++;
                }
/*        int m=0;
        while (OtherViews[m].event_name!=null)
            {
            CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(OtherViews[m]);
            CurrentViewStat[CurrentViewStatIndex].Label_text=ChangeDynamic(CurrentViewStat[CurrentViewStatIndex].Label_text,CurrentViewStat[CurrentViewStatIndex]);
            CurrentViewStatIndex++;
            if(CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) 
            {
				EhouseTCP.FinishedEthernet=true;
                return false;
            //break;
            }
            m++;
            }*/
if (changed)  
            {
                 XmlChanged=true;
            
            }
        EhouseTCP.FinishedEthernet=true;
        return changed;
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * 
 * Check address for current device cache instance verification
 * 
 * @param devadrh - high address
 * @param devadrl - low address
 * @return true if address match
 */
        public  boolean isAddress(int devadrh, int devadrl)
        {
            int devadrhh,devadrll;
            devadrhh=devadrh;
            devadrll=devadrl;
       if (devadrhh<0) devadrhh=256+devadrhh;
       if (devadrll<0) devadrll=256+devadrll;
        if ((devadrhh==DevAdrH) && (devadrll==DevAdrL))             
            return true;
            
        
        
        return false;

        }
    
    }

