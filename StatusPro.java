/**
 * eHouse.PRO status cache instance for single eHousePRO server gathering all names, states, values, calculation 
 * @author Robert Jarzabek 
 * 2014.11.25
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
public class StatusPro
  {         
            public static String Charset="UTF-8";
            public static int INT_SIZE=2;                                   //size of int in "C" for future referrence
            public String recent="";                                        //recent status date/time
            public static String sep=System.getProperty("file.separator");  //file system separator slash or backslash
            public boolean InitializedMatrix=false; 
            public long CheckSum=0;
            public long PrevCheckSum=0;
            public boolean XmlChanged=false;
            public double MCP9700x=10000;           // uV/C
            public double MCP9701x=19500;           // uV/C
            public double MCP9700_Offset=-500;      //Offset voltage at 0C [mV]
            public double MCP9701_Offset=-400;      //Offset voltage at 0C [mV]
            int STATUS_INDEX = -1;
//            final static int GRAPHICS_OBJECTS_MAX = 5;      //maximal graphic object per signal
            static int OUTPUTS_COUNT_PRO = 256;             //maximal count of outputs in current controller
            static int ADC_PROGRAMS_COUNT_PRO=256;          //adc programs count in current controller
            static int INPUTS_COUNT_PRO = 256;              //input count in current controller
            static int SENSORS_COUNT_PRO = 256;             //adc sensors / inputs count in current controller
            static int ZONES_COUNT_PRO=256;                 //max zones count in current controller (for CM)
            static final int DIMMERS_COUNT_PRO=256;         //maximal single dimmers count in current controller
            static final int DIMMERS_RGB_COUNT_PRO=DIMMERS_COUNT_PRO/3;    //maximal rgb (triple) dimmers count in current controller
            final static int PROGRAMS_COUNT_PRO      = 256; //maximal programs count in current controller
            final static int SECU_PROGRAMS_COUNT_PRO = 256;
            public String[] ProgramNames        = new String[PROGRAMS_COUNT_PRO];       //program names in cache
            public String[] SecuProgramNames    = new String[SECU_PROGRAMS_COUNT_PRO];       //secu-rollers programs
            public String[] ZoneNames           = new String[ZONES_COUNT_PRO];          //zone names for eHouse.PRO
            public String[] ADCProgramNames     = new String[ADC_PROGRAMS_COUNT_PRO];   //adc program names
            public String[] DimmerNames         = new String[DIMMERS_COUNT_PRO];        //dimmers names
            public String[] DimmerRGBNames      = new String[DIMMERS_RGB_COUNT_PRO];    //RGB dimmers names
            public String[] RollerNames=new String[OUTPUTS_COUNT_PRO/2];       //roller 
            //static int ADC_SENSORS_COUNT = 16;
            static int SENSORS_COUNT_ALARM_PRO = 256;       //security sensors count
            static int STATUS_SIZE = 5000;                  //size of binary status buffer 
//            final static int GRAPHIC_OBJECTS_OTHER=3000;    //nr of graphic objects without assignment
//            final static int GRAPHIC_OBJECTS_CURRENT=10000; //nr of graphic objects with assignment 
            public int[] Dimmer                 = new int[DIMMERS_COUNT_PRO];        //dimmer value
            public int[] DimmerRGB              = new int[DIMMERS_RGB_COUNT_PRO];    //rgb dimmer value
            public String XML="";
        /// All necessary data for visualisation (names and status)
            public String DeviceName;                   //current device name
            public int StatusCurrentSize=0;             //status size (prefix to current status buffer)
            public String[] OutputNames     = new String[OUTPUTS_COUNT_PRO];    //name of outputs
//            public GraphicObject[]   OtherViews=new GraphicObject[GRAPHIC_OBJECTS_OTHER];   //not assigned objects array
            public int OtherViewsIndex=0;       //index not assigned objects
            public int CurrentViewStatIndex=0;  //index of assigned objects
//            public GraphicObject[]   CurrentViewStat=new GraphicObject[GRAPHIC_OBJECTS_CURRENT];                //cache of current view objects for device
//          public GraphicObject[][]  OutputView =new GraphicObject[OUTPUTS_COUNT_RM][GRAPHICS_OBJECTS_MAX];    ///graphical object for output
//            public GraphicObject[][]  OutputViewL   =new GraphicObject[OUTPUTS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
//            public GraphicObject[][]  OutputViewH   =new GraphicObject[OUTPUTS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public String[]           InputNames    =new String[INPUTS_COUNT_PRO];
//          public GraphicObject[][]  InputView =new GraphicObject[INPUTS_COUNT_RM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
//            public GraphicObject[][]  InputViewL =new GraphicObject[INPUTS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output                        
//            public GraphicObject[][]  InputViewH =new GraphicObject[INPUTS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output                        
            public String[] SensorNames     = new String[SENSORS_COUNT_PRO];
//          public GraphicObject[][]  SensorView =new GraphicObject[SENSORS_COUNT_RM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
//            public GraphicObject[][]  SensorViewL =new GraphicObject[SENSORS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
//            public GraphicObject[][]  SensorViewOK =new GraphicObject[SENSORS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
//            public GraphicObject[][]  SensorViewH =new GraphicObject[SENSORS_COUNT_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
            public int[] SensorType= new int[SENSORS_COUNT_PRO];
//            public Double[] SensorLM335Temps     = new Double[SENSORS_COUNT_RM];
            public Double[] SensorTempsLM35     = new Double[SENSORS_COUNT_PRO];     //adc calculated value for LM35 temperature sensor connected to the input
            public Double[] SensorTempsMCP9700     = new Double[SENSORS_COUNT_PRO];  //adc calculated value for mcp9700 temperature sensor connected to the input
            public Double[] SensorTempsMCP9701     = new Double[SENSORS_COUNT_PRO];  //adc calculated value for mcp9701 temperature sensor connected to the input
            public String[] SensorTypeName  = new String[SENSORS_COUNT_PRO];         //Name of sensor type
            public int[] SensorABSValues    = new int[SENSORS_COUNT_PRO];            //Sensor absolute value 0..1023 (for 10bit ADC)
            public int[] Calibration        = new int[SENSORS_COUNT_PRO];            //calibrations value * 100 for each ADC input
            public Double[] SensorTemps     = new Double[SENSORS_COUNT_PRO];         //adc calculated value for LM335 temperature sensor connected to the input
            public Double[] SensorPercents  = new Double[SENSORS_COUNT_PRO];         //adc calculated percent value
            public Double[] SensorLights    = new Double[SENSORS_COUNT_PRO];         //adc calculated inverted percent (or light) value for fototransistor sensor
            public Double[] SensorVolts     = new Double[SENSORS_COUNT_PRO];         //adc calculated value for voltage in reference to Power supply value or Vref
            public Boolean[] OutputStates   = new Boolean[OUTPUTS_COUNT_PRO];        //output states
            public Boolean[] InputStates    = new Boolean[INPUTS_COUNT_PRO];         //input states
            public Boolean[] InputInverts    = new Boolean[INPUTS_COUNT_PRO];        //input inversion flags
            public Boolean[] AlarmSensorsActive     = new Boolean[SENSORS_COUNT_ALARM_PRO];     //alarm sensors activity state
            public Boolean[] AlarmSensorsSMS   = new Boolean[SENSORS_COUNT_ALARM_PRO];          //alarm sensors activity state
            public Boolean[] AlarmSensorsSilent   = new Boolean[SENSORS_COUNT_ALARM_PRO];       //Silent
            public Boolean[] AlarmSensorsEarlyWarning   = new Boolean[SENSORS_COUNT_ALARM_PRO];       //Early Warning
            public Boolean[] AlarmSensorsHorn   = new Boolean[SENSORS_COUNT_ALARM_PRO];               //Horn
  /*  
            public GraphicObject[][]  HornViewSensors =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  HornViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  HornViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
            
            public GraphicObject[][]  EarlyViewSensors =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  EarlyViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  EarlyViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
            
            public GraphicObject[][]  SMSViewSensors =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  SMSViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  SMSViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
            
            public GraphicObject[][]  SilentViewSensors =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  SilentViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  SilentViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            

            
            public GraphicObject[][]  AlarmViewSensors =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  AlarmViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  AlarmViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
    */
			public Boolean[] AlarmSensorsWarning = new Boolean[SENSORS_COUNT_ALARM_PRO];        //alarm sensors warning state
            public Boolean[] AlarmSensorsMonitoring= new Boolean[SENSORS_COUNT_ALARM_PRO];      //alarm sensors monitoring state
            //public Boolean[] MonitoringSensorsWarning = new Boolean[SENSORS_COUNT_ALARM];   //
            public Boolean[] AlarmSensorsAlarm = new Boolean[SENSORS_COUNT_ALARM_PRO];          // alarm sensors alarm states
            public String[] AlarmSensorsNames = new String[SENSORS_COUNT_ALARM_PRO];            //Alarm sensors names
/*            //public GraphicObject[][]  WarningSensors =new GraphicObject[SENSORS_COUNT_ALARM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
//          public GraphicObject[][]  WarningViewSensors =new GraphicObject[SENSORS_COUNT_ALARM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  WarningViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  WarningViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
            //public GraphicObject[][]  MonitoringSensors =new GraphicObject[SENSORS_COUNT_ALARM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
//          public GraphicObject[][]  MonitoringViewSensors =new GraphicObject[SENSORS_COUNT_ALARM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  MonitoringViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  MonitoringViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
//            public GraphicObject[][]  ActiveViewSensors =new GraphicObject[SENSORS_COUNT_ALARM][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  ActiveViewSensorsL =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output
            public GraphicObject[][]  ActiveViewSensorsH =new GraphicObject[SENSORS_COUNT_ALARM_PRO][GRAPHICS_OBJECTS_MAX];  ///graphical object for output            
*/
            public int DevAdrH;                                                 //device address h
            public int DevAdrL;                                                 //device address l
            public String DevAdr;                                               //combined device address
            public boolean changed;                                             //flag to set if changed value from previous query status
            public boolean AdcChanged;                                          //adc changed from previous query status
            //public static DateTime LastRead;
            public int CurrentProgram=0;                                          //Current program no.
            public String CurrentProgramName="";                                //current program name
            public int CurrentSecuProgram=0;                                          //Current program no.
            public String CurrentSecuProgramName="";                                //current program name
            public int ADCCurrentProgram=0;                                       //ADC current program
            public String ADCCurrentProgramName="";                             //ADC current program name
            public int CurrentZone=0;                                             //current security zone no.
            public String CurrentZoneName="";                                   //current security zone name
            public Byte[] CurrentStatus = new Byte[STATUS_SIZE];                //current status received from the controller (binary)
            //public Byte[] PreviousStatus = new Byte[STATUS_SIZE];
//devices types            
            public boolean IsBatch; //batch not common device
            public boolean IsAlarm;
            public boolean IsEthernet;
            public int VCC; //power suply for calibration * 100

            final static int STATUS_PRO_OFFSET  =6;                   //offset in current status for data mark 0xff55ff55+addrh+addrl
            final static int STATUS_ADC_PRO     =0;    			//adc values * 2B
            final static int STATUS_ADC_PRO_END	=256;                    //end of adc values
        final static int STATUS_OUT_PRO			=STATUS_ADC_PRO_END;        //i2c output buffers states  //max=256 outputs
        final static int STATUS_INPUTS_PRO		=STATUS_OUT_PRO+OUTPUTS_COUNT_PRO/8;        //spi input buffers states //max 256 inputs
        final static int STATUS_HORN_PRO		=STATUS_INPUTS_PRO+INPUTS_COUNT_PRO/8;      //--|--- for horn states 
        final static int STATUS_WARNING_PRO		=STATUS_HORN_PRO+INPUTS_COUNT_PRO/8;        //--|--- for warning states 
        final static int STATUS_MONITORING_PRO	        =STATUS_WARNING_PRO+INPUTS_COUNT_PRO/8;     //--|--- for monitoring 
        final static int STATUS_SILENT_PRO	        =STATUS_MONITORING_PRO+INPUTS_COUNT_PRO/8;  //--|--- for silent
        final static int STATUS_EARLY_PRO		=STATUS_SILENT_PRO+INPUTS_COUNT_PRO/8;      //--|--- for early warning
        final static int STATUS_SMS_PRO                 =STATUS_EARLY_PRO+INPUTS_COUNT_PRO/8;       //--|--- for sms
        final static int STATUS_ALARM_PRO		=STATUS_SMS_PRO+INPUTS_COUNT_PRO/8;         //--|--- for alarm states 
        final static int STATUS_DIMMERS_PRO             =STATUS_ALARM_PRO+INPUTS_COUNT_PRO/8;       //dimmers status
        
        final static int STATUS_PROGRAM_NR		=STATUS_DIMMERS_PRO+DIMMERS_COUNT_PRO;      //--|--- for alarm states in case of CM
        final static int STATUS_ADC_PROGRAM		=STATUS_PROGRAM_NR+1*INT_SIZE;              //current adc program no.
        final static int STATUS_SECU_PROGRAM_NR         =STATUS_PROGRAM_NR+2*INT_SIZE;              //current secu-roller program
        final static int STATUS_ZONE_NR			=STATUS_PROGRAM_NR+3*INT_SIZE;              //current zone
        
        final static int STATUS_HW_STATES		=STATUS_PROGRAM_NR+4*INT_SIZE;              //hardware output warning,horn,
        final static int STATUS_FUTURE                  =STATUS_PROGRAM_NR+5*INT_SIZE;              //future info
        final static int STATUS_ADDITIONAL              =STATUS_FUTURE+180;                         //additional data
        
        final static int STATUS_PROFILE_RM              =180-1;
        final static int  STATUS_LIGHT                  =(180-5);

    
        final static int  STATUS_ZONE_NO		=(180-2);			//NUMER STREFY ZABEZPIECZEŃ

////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final static int ADC_N_OFFSET = 4;              //adc current levels index in status (except CM)
        final static int O_N_OFFSET=ADC_N_OFFSET+16;    //direct output states index in status (except CM)
        final static int I_N_OFFSET=O_N_OFFSET+35;      //direct input states index in status (except CM)
//////////////////////////////////////////////////////////////////////////////////////////////////////////        
public void SetStatusIndex(int nr)
          {
          STATUS_INDEX=nr;
          }
//////////////////////////////////////////////////////////////////////////////////////////////////////////      
public double calculate_voltage(int dta)
            {
            double tmp = Math.round((double)((dta *  VCC ) / 1023)*100);    //2 digits precisions
            tmp=tmp/100000;
            return tmp;
            }
////////////////////////////////////////////////////////////////////////////////////////
public double calculate_MCP9700(int dta)
            {    
            double tmp = ((double)((dta *  VCC ) / 1023))+MCP9700_Offset;
             //get voltage in [mv]
            tmp=((double)Math.round((tmp/MCP9700x)*10*1000))/10;    //1 digit precision
                
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
header+="<CurrentProgram>"+ehousecommunication.i(CurrentProgram+1)+"</CurrentProgram>"+eol;
header+="<CurrentProgramName> "+ CurrentProgramName+" </CurrentProgramName>"+eol;
header+="<CurrentSecuProgram>"+ehousecommunication.i(CurrentSecuProgram+1)+"</CurrentSecuProgram>"+eol;
header+="<CurrentSecuProgramName> "+ CurrentSecuProgramName+" </CurrentSecuProgramName>"+eol;
header+="<ADCCurrentProgram>"+ehousecommunication.i(ADCCurrentProgram+1)+"</ADCCurrentProgram>"+eol;
header+="<ADCCurrentProgramName> "+ ADCCurrentProgramName+" </ADCCurrentProgramName>"+eol;
header+="<CurrentStatus>"+ehousecommunication.hx(CurrentStatus,STATUS_SIZE)+"</CurrentStatus>"+eol;
header+="<RecentStatus>"+recent+"</RecentStatus>"+eol;
header+="<Devicetype>"+"eHousePRO"+"</Devicetype>"+eol;
if (IsEthernet)  header+="<InterfaceType>"+"Ethernet"+"</InterfaceType>"+eol;;
            {
            header+="<CurrentZone>"+ehousecommunication.i(CurrentZone+1)+"</CurrentZone>"+eol;
            header+="<CurrentZoneName>"+ CurrentZoneName+"</CurrentZoneName>"+eol;
            }
bodyheader=eol+"<Outputs>"+eol;
body="";
int maxio=OUTPUTS_COUNT_PRO;
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
maxio=0;                    //SENSORS_COUNT_PRO; ADC NOT SUPPORTED YET
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
     body+="    </Item>"+eol+eol;
    }
}

header+=bodyheader+body+bodyfooter+eol;
bodyheader=eol+"<Inputs>"+eol;
body=eol;
bodyfooter=eol;
maxio=INPUTS_COUNT_PRO;
for (int i=0;i<maxio;i++)
{
   
if (((!ehousecommunication.IgnoreAtChar) || (InputNames[i].indexOf("@")<0)))    
    {
    String state="0"; String value="Off";   
    if (((InputStates[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}    
    body+="    <Item>"+eol+
    "        <Name>"+InputNames[i]+"</Name>"+eol+            
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <State>"+state+"</State>"+eol+
    "        <Value>"+value+"</Value>"+eol;
        {
    if (((AlarmSensorsActive[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}            
        body+="        <ActiveState>"+state+"</ActiveState>"+eol+
        "        <ActiveValue>"+value+"</ActiveValue>"+eol;

    if (((AlarmSensorsWarning[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}            
        body+="        <WarningState>"+state+"</WarningState>"+eol+
        "        <WarningValue>"+value+"</WarningValue>"+eol;
        
    if (((AlarmSensorsMonitoring[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}
        body+="        <MonitoringState>"+state+"</MonitoringState>"+eol+
        "        <MonitoringValue>"+value+"</MonitoringValue>"+eol;
        
    if (((AlarmSensorsAlarm[i])))
            {state="1";value="On";} 
        else
            {state="0";value="Off";}
        body+="        <AlarmState>"+state+"</AlarmState>"+eol+
        "        <AlarmValue>"+value+"</AlarmValue>"+eol;
        }
        
    if (((AlarmSensorsSMS[i])))
            {
            state="1";value="On";
            } 
         else
            {state="0";value="Off";}
        body+="        <SMSState>"+state+"</SMSState>"+eol+
        "        <SMSValue>"+value+"</SMSValue>"+eol;
             
if (((AlarmSensorsSilent[i])))
            {
            state="1";value="On";
            } 
         else
            {
            state="0";value="Off";
            }
        body+="        <SilentState>"+state+"</SilentState>"+eol+
        "        <SilentValue>"+value+"</SilentValue>"+eol;
        
if (((AlarmSensorsEarlyWarning[i])))
            {
            state="1";value="On";
            } 
         else
            {
            state="0";value="Off";
            }
        body+="        <EarlyState>"+state+"</EarlyState>"+eol+
        "        <EarlyValue>"+value+"</EarlyValue>"+eol;
        
    if (((AlarmSensorsHorn[i])))
            {
            state="1";value="On";
            } 
         else
            {
            state="0";value="Off";
            }
        body+="        <HornState>"+state+"</HornState>"+eol+
        "        <HornValue>"+value+"</HornValue>"+eol;
        
    String inversion="Normal";
    if (InputInverts[i]) inversion="Inverted";          //server sends calculated input state with inversion
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
    } 
catch (Exception e) 
    {
    //log()
    }
XML= header+footer;
}
}
};    
//////////////////////////////////////////////////////////////////////////////////
public String CHDYN(String str)
{
if (str==null)     return "";
String Line=str;    
if (Line.indexOf("%Output")>=0)
    for (int i=0;i<OUTPUTS_COUNT_PRO;i++) Line.replaceAll("%Output"+String.valueOf(i+1)+"%",OutputNames[i]);
if (Line.indexOf("%Input")>=0)
    for (int i=0;i<INPUTS_COUNT_PRO;i++) Line.replaceAll("%Input"+String.valueOf(i+1)+"%",InputNames[i]);
if (Line.indexOf("%ADCSensor")>=0)
    for (int i=0;i<SENSORS_COUNT_PRO;i++) Line.replaceAll("%ADCSensor"+String.valueOf(i+1)+"%",SensorNames[i]);
if (Line.indexOf("%AlarmSensor")>=0)
    for (int i=0;i<SENSORS_COUNT_ALARM_PRO;i++) Line.replaceAll("%AlarmSensor"+String.valueOf(i+1)+"%",AlarmSensorsNames[i]);
if (Line.indexOf("%Dimmer")>=0)
    for (int i=0;i<DIMMERS_COUNT_PRO;i++) Line.replaceAll("%Dimmer"+String.valueOf(i+1)+"%",DimmerNames[i]);
if (Line.indexOf("%DimmerRGBNames")>=0)
    for (int i=0;i<DIMMERS_RGB_COUNT_PRO;i++) Line.replaceAll("%DimmerRGBNames"+String.valueOf(i+1)+"%",DimmerRGBNames[i]);
if (Line.indexOf("%Program")>=0)
    for (int i=0;i<PROGRAMS_COUNT_PRO;i++) Line.replaceAll("%Program"+String.valueOf(i+1)+"%",ProgramNames[i]);
if (Line.indexOf("%ADCProgram")>=0)
    for (int i=0;i<PROGRAMS_COUNT_PRO;i++) Line.replaceAll("%ADCProgram"+String.valueOf(i+1)+"%",ADCProgramNames[i]);
return Line;    
} 
////////////////////////////////////////////////////////////////////////////////
//convert ints[] to hex string
public String hix(int[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]);
        }
    return res;
}
////////////////////////////////////////////////////////////////////////////////
// convert bytes[] to hex
public String hx(Byte[] dta,int size)
{
    String res="";
    for (int i=0;i<size;i++)
        {
        res+=ehousecommunication.ConvertAsciHex(dta[i]);
        }
    return res;
}
////////////////////////////////////////////////////////////////////////////////
//convert integer to string
public String i(int data)
{
    return String.valueOf(data);
}
public String bo(boolean data)
{
    if (data) return "1";
    else return "0";
    
}
///////////////////////////////////////////////////////////////////////////////
//
// clear all visualisation obj for devices must run re-read to load again
// 
///////////////////////////////////////////////////////////////////////////////////////////////////////////
/*public void ClearAllVisualisationItems()
{
    if (DeviceName==null) return;
    if (DeviceName=="") return;
    for (int z=0;z<GRAPHIC_OBJECTS_OTHER;z++)
                                {
                                OtherViews[z].event_name=null;
                                OtherViews[z].DeviceName=null;
                                OtherViews[z].object_type=0;
                                }
    for (int z=0;z<GRAPHIC_OBJECTS_CURRENT;z++)
                                {
                                CurrentViewStat[z].event_name=null;                            
                                CurrentViewStat[z].DeviceName=null;                            
                                CurrentViewStat[z].object_type=0;
                                }
    CurrentViewStatIndex=0;
    OtherViewsIndex=0;
    for (int m=0;m<OUTPUTS_COUNT_PRO;m++)
                            {
                             for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                {
                                OutputViewH[m][z].event_name=null;
                                OutputViewL[m][z].event_name=null;
                                OutputViewH[m][z].DeviceName=null;
                                OutputViewL[m][z].DeviceName=null;
                                OutputViewH[m][z].object_type=0;
                                OutputViewL[m][z].object_type=0;
                                }
                            }
    for (int m=0;m<INPUTS_COUNT_PRO;m++)
                            {
                             for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)                            
                                {
                                InputViewH[m][z].event_name=null;
                                InputViewL[m][z].event_name=null;                                    
                                InputViewH[m][z].DeviceName=null;
                                InputViewL[m][z].DeviceName=null;                                    
                                InputViewH[m][z].object_type=0;
                                InputViewL[m][z].object_type=0;                                    
                                }
                            }
    for (int m=0;m<SENSORS_COUNT_PRO;m++)
                            {
                              for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                {
                                SensorViewL[m][z].event_name=null;
                                SensorViewH[m][z].event_name=null;                                    
                                SensorViewOK[m][z].event_name=null;
                                SensorViewL[m][z].DeviceName=null;
                                SensorViewH[m][z].DeviceName=null;                                    
                                SensorViewOK[m][z].DeviceName=null;
                                SensorViewL[m][z].object_type=0;
                                SensorViewH[m][z].object_type=0;                                    
                                SensorViewOK[m][z].object_type=0;
                                }
                            }
    for (int m=0;m<SENSORS_COUNT_ALARM_PRO;m++)
                            {
                             for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                {
                                ActiveViewSensorsL[m][z].event_name=null;
                                ActiveViewSensorsH[m][z].event_name=null;                                    
                                AlarmViewSensorsH[m][z].event_name=null;
                                AlarmViewSensorsL[m][z].event_name=null;
                                WarningViewSensorsH[m][z].event_name=null;
                                WarningViewSensorsL[m][z].event_name=null;
                                MonitoringViewSensorsH[m][z].event_name=null;
                                MonitoringViewSensorsL[m][z].event_name=null;
                                
                                SMSViewSensorsL[m][z].event_name=null;
                                SMSViewSensorsH[m][z].event_name=null;                                    
                                HornViewSensorsH[m][z].event_name=null;
                                HornViewSensorsL[m][z].event_name=null;
                                EarlyViewSensorsH[m][z].event_name=null;
                                EarlyViewSensorsL[m][z].event_name=null;
                                SilentViewSensorsH[m][z].event_name=null;
                                SilentViewSensorsL[m][z].event_name=null;

                                ActiveViewSensorsL[m][z].object_type=0;
                                ActiveViewSensorsH[m][z].object_type=0;                                    
                                AlarmViewSensorsH[m][z].object_type=0;
                                AlarmViewSensorsL[m][z].object_type=0;
                                WarningViewSensorsH[m][z].object_type=0;
                                WarningViewSensorsL[m][z].object_type=0;
                                MonitoringViewSensorsH[m][z].object_type=0;
                                MonitoringViewSensorsL[m][z].object_type=0;
                                
                                SMSViewSensorsL[m][z].object_type=0;
                                SMSViewSensorsH[m][z].object_type=0;                                    
                                HornViewSensorsH[m][z].object_type=0;
                                HornViewSensorsL[m][z].object_type=0;
                                EarlyViewSensorsH[m][z].object_type=0;
                                EarlyViewSensorsL[m][z].object_type=0;
                                SilentViewSensorsH[m][z].object_type=0;
                                SilentViewSensorsL[m][z].object_type=0;

                                
                                ActiveViewSensorsL[m][z].DeviceName=null;
                                ActiveViewSensorsH[m][z].DeviceName=null;                                    
                                AlarmViewSensorsH[m][z].DeviceName=null;
                                AlarmViewSensorsL[m][z].DeviceName=null;
                                WarningViewSensorsH[m][z].DeviceName=null;
                                WarningViewSensorsL[m][z].DeviceName=null;
                                MonitoringViewSensorsH[m][z].DeviceName=null;
                                MonitoringViewSensorsL[m][z].DeviceName=null;
                                
                                SMSViewSensorsL[m][z].DeviceName=null;
                                SMSViewSensorsH[m][z].DeviceName=null;                                    
                                HornViewSensorsH[m][z].DeviceName=null;
                                HornViewSensorsL[m][z].DeviceName=null;
                                EarlyViewSensorsH[m][z].DeviceName=null;
                                EarlyViewSensorsL[m][z].DeviceName=null;
                                SilentViewSensorsH[m][z].DeviceName=null;
                                SilentViewSensorsL[m][z].DeviceName=null;
                                }
                            }
}
///////////////////////////////////////////////////////////////////////////////////////////
//
// clear active visualisation items CurrentView Matrix
//
//////////////////////////////////////////////////////////////////////////////////////////
public void ClearActiveVisualization()
{
for (int z=0;z<GRAPHIC_OBJECTS_CURRENT;z++)
    {            
    CurrentViewStat[z].event_name=null;                            
    CurrentViewStat[z].DeviceName=null;             
    CurrentViewStat[z].object_type=0;
    }
CurrentViewStatIndex=0;
}   
/////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Initialize all visulization object matrixes for device 
 * 
 */
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void InitVisualisationItems()
{
   if (InitializedMatrix) return;
   for (int m=0;m<DIMMERS_COUNT_PRO;m++) DimmerNames[m]="";
    for (int m=0;m<DIMMERS_RGB_COUNT_PRO;m++) DimmerRGBNames[m]="";
    for (int m=0;m<PROGRAMS_COUNT_PRO;m++)  ProgramNames[m]= "";
    for (int m=0;m<PROGRAMS_COUNT_PRO;m++)  SecuProgramNames[m]= "";
    for (int m=0;m<ADC_PROGRAMS_COUNT_PRO;m++)  ADCProgramNames[m]= "";
    for (int m=0;m<ZONES_COUNT_PRO;m++)  ZoneNames[m]= "";
    for (int z=0;z<GRAPHIC_OBJECTS_OTHER;z++)
                                {
                                OtherViews[z]=new GraphicObject("");
                                }
    for (int z=0;z<GRAPHIC_OBJECTS_CURRENT;z++)
                                {            
                                CurrentViewStat[z]=new GraphicObject("");
                                }
    for (int m=0;m<OUTPUTS_COUNT_PRO;m++)
                            {
                             OutputNames[m]="";
                             OutputStates[m]   = false;  
                             for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                {
                                OutputViewH[m][z]=new GraphicObject("");
                                OutputViewL[m][z]=new GraphicObject("");
                                }
                            }
    for (int m=0;m<INPUTS_COUNT_PRO;m++)
                            {
                            InputNames[m]="";   
                            InputStates[m]    = false;
                            for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)                            
                                {
                                InputViewH[m][z]=new GraphicObject("");
                                InputViewL[m][z]=new GraphicObject("");                                    
                                }                            
                            }                            
    for (int m=0;m<SENSORS_COUNT_PRO;m++)
                                {
                                SensorNames[m]="";                                   
                                for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                    {
                                    SensorViewL[m][z]=new GraphicObject("");
                                    SensorViewH[m][z]=new GraphicObject("");                              
                                    SensorViewOK[m][z]=new GraphicObject("");
                                    }
                                }                            
    
    for (int m=0;m<SENSORS_COUNT_ALARM_PRO;m++)
                                {
                                AlarmSensorsNames[m]="";   
                                for (int z=0;z<GRAPHICS_OBJECTS_MAX;z++)
                                    {
                                    AlarmSensorsActive[m]=false;
                                    AlarmSensorsWarning[m]=false;
                                    AlarmSensorsMonitoring[m]=false;
                                    AlarmSensorsAlarm[m] = false;
                                    AlarmSensorsHorn[m]=false;
                                    AlarmSensorsSilent[m]=false;
                                    AlarmSensorsSMS[m] = false;
                                    AlarmSensorsEarlyWarning[m]=false;
                                    
                                    
                                    ActiveViewSensorsL[m][z]=new GraphicObject("");
                                    ActiveViewSensorsH[m][z]=new GraphicObject("");                              
                                    AlarmViewSensorsH[m][z]=new GraphicObject("");
                                    AlarmViewSensorsL[m][z]=new GraphicObject("");
                                    WarningViewSensorsH[m][z]=new GraphicObject("");
                                    WarningViewSensorsL[m][z]=new GraphicObject("");
                                    MonitoringViewSensorsH[m][z]=new GraphicObject("");
                                    MonitoringViewSensorsL[m][z]=new GraphicObject("");                                   
                                    EarlyViewSensorsL[m][z]=new GraphicObject("");
                                    EarlyViewSensorsH[m][z]=new GraphicObject("");                              
                                    SilentViewSensorsH[m][z]=new GraphicObject("");
                                    SilentViewSensorsL[m][z]=new GraphicObject("");
                                    SMSViewSensorsH[m][z]=new GraphicObject("");
                                    SMSViewSensorsL[m][z]=new GraphicObject("");
                                    HornViewSensorsH[m][z]=new GraphicObject("");
                                    HornViewSensorsL[m][z]=new GraphicObject("");
                                    
                                    }
                            }
InitializedMatrix=true;                            
return;
}    
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//cache visualization object of current status
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void CreateVisualization()
{
    EhouseTCP.UpdateViewsOnProgres++;
    ClearActiveVisualization();
            for (int z=0;z<OtherViewsIndex;z++)
                                {
                                if (OtherViews[z].object_type>0)
                                //if (OtherViews[z].event_name!=null)
                                    {
                                CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(OtherViews[z]);
                                CurrentViewStat[CurrentViewStatIndex].Label_text=ChangeDynamic(CurrentViewStat[CurrentViewStatIndex].Label_text);
                                CurrentViewStatIndex++;
                                    }
                                else break;
                                }
            for (int m=0;m<OUTPUTS_COUNT_PRO;m++)
                                {
                                UpdateIOStatus(OutputStates[m], /*m,*/ OutputViewH[m],OutputViewL[m],GRAPHICS_OBJECTS_MAX);
                                }
            for (int m=0;m<INPUTS_COUNT_PRO;m++)
                                {
                                UpdateIOStatus(InputStates[m], /*m,*/ InputViewH[m],InputViewL[m],GRAPHICS_OBJECTS_MAX);
                                }

            for (int m=0;m<SENSORS_COUNT_PRO;m++)
                                {
                                UpdateIOStatus(true, /*m,*/ SensorViewL[m],SensorViewH[m],GRAPHICS_OBJECTS_MAX);                                        
                                }
            for (int m=0;m<SENSORS_COUNT_ALARM_PRO;m++)
                {                                
                UpdateIOStatus(AlarmSensorsActive[m], /*m,*/ ActiveViewSensorsH[m],ActiveViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsWarning[m], /*m,*/ WarningViewSensorsH[m],WarningViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsMonitoring[m], /*m,*/ MonitoringViewSensorsH[m],MonitoringViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsAlarm[m], /*m,*/ AlarmViewSensorsH[m],AlarmViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                
                UpdateIOStatus(AlarmSensorsSMS[m], /*m,*/ SMSViewSensorsH[m],SMSViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsSilent[m], /*m,*/ SilentViewSensorsH[m],SilentViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsHorn[m], /*m,*/ HornViewSensorsH[m],HornViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                UpdateIOStatus(AlarmSensorsEarlyWarning[m], /*m,*/ EarlyViewSensorsH[m],EarlyViewSensorsL[m],GRAPHICS_OBJECTS_MAX);                                
                }
            int Max_ADC=0;
            for (int i=0; i< Max_ADC;i++)
                {                                            
                    UpdateADC(i, 5, 0, 10, SensorViewL[i],SensorViewH[i],SensorViewOK[i],GRAPHICS_OBJECTS_MAX);
                }
EhouseTCP.UpdateViewsOnProgres--;            
//return;
}    
*/
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
 * Read alarm sensor names from file and init data
 * @param pat 
 */
public void ReadAlarmSensorsNames(String pat)
{
    int i=0;
    int z=0;
    if (ehousecommunication.FileExists(pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(pat,Charset);
                        while (AlarmS[i]!=null)
                            {
                            AlarmSensorsActive[i] = false;
                            AlarmSensorsAlarm[i] = false;
                            InputInverts[i]=false;
                            AlarmSensorsWarning[i] = false;
                            AlarmSensorsMonitoring[i]=false;
                            AlarmSensorsNames[i] = AlarmS[i];
                            i++;
                            if (i == SENSORS_COUNT_ALARM_PRO) break;
                            }
                        while (i<SENSORS_COUNT_ALARM_PRO)
                            {
                            AlarmSensorsActive[i] = false;
                            AlarmSensorsAlarm[i] = false;
                            AlarmSensorsWarning[i] = false;
                            AlarmSensorsMonitoring[i]=false;
                            AlarmSensorsSMS[i] = false;
                            AlarmSensorsHorn[i] = false;
                            AlarmSensorsSilent[i] = false;
                            AlarmSensorsEarlyWarning[i]=false;
                            InputInverts[i]=false;
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
            String st[]=new String[300];
            String temp;
            int i;
//            try
            {
                if (ehousecommunication.FileExists(name))
                
                {
                    InitVisualisationItems();
                    
                    st=ehousecommunication.getfile(name+sep+"names/name.txt",Charset);
                    DeviceName = st[0];                            //device name
                    if (st[1]==null) st[1]="000200";
                    if (st[1].length()<6) st[1]="000200";
                    String adrh=st[1].substring(0,3);
                    String adrl=st[1].substring(3) ;                           
                    DevAdrH = check_in(adrh, 0);                                             //device addr h
                    DevAdrL = check_in(adrl, 0);                                             //device addr l
                    DevAdr = String.valueOf(DevAdrL); ;
                    DevAdr=st[1];
                    IsAlarm=true;
                    IsEthernet=true;
                    ehousecommunication.ProName=DeviceName;
                    String senstypes[]=new String[1000];    
                    if (ehousecommunication.FileExists(ehousecommunication.path+"ADCSensorTypes.txt"))          
                        {
                        senstypes=ehousecommunication.getfile(ehousecommunication.path+"ADCSensorTypes.txt",Charset); //adc sensor types list
                        }
                    else for (i=0;i<256;i++) senstypes[i]="N/A";    
            
                    String senstype[]=new String[1000];//
                    if (ehousecommunication.FileExists(ehousecommunication.path+"AdcConfig.cfg"))          
                        {
                        senstype=ehousecommunication.getfile(ehousecommunication.path+"AdcConfig.cfg",Charset);        //adc sensors type
                        }
                    else for (i=0;i<256;i++) senstypes[i]="0";    
                    
                    if (ehousecommunication.FileExists(name+sep+"names/adcs.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/adcs.txt",Charset);//adc input names 
                        }
                    else for (i=0;i<256;i++) st[i]="";
                        
                    for (i = 0; i < SENSORS_COUNT_PRO; i++)
                         {
                        if (st[i]==null) st[i]="@ADC"+String.valueOf(i+1);
                        if (st[i].length()==0) st[i]="@ADC"+String.valueOf(i+1);
                        SensorNames[i] = st[i].trim();
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
                    
                    VCC = 3300;
                    // Set output names and initialize
                    if (ehousecommunication.FileExists(name+sep+"names/outputs.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/outputs.txt",Charset);//outputs names 
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    
                    for (i = 0; i < OUTPUTS_COUNT_PRO; i++)
                        {
                        if (st[i]==null) st[i]="@Out"+String.valueOf(i+1);
                        if (st[i].length()==0) st[i]="@Out"+String.valueOf(i+1);
                        OutputNames[i] = st[i].trim();
                        OutputStates[i] = false;
                        }
                    ///Get input names 
                    if (ehousecommunication.FileExists(name+sep+"names/inputs.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/inputs.txt",Charset);//input names 
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    
                    
                    //String inv[]=ehousecommunication.getfile(ehousecommunication.path + DevAdr +sep+ "SensorsInverts.cfg");
                    for (i =0; i < INPUTS_COUNT_PRO; i++)
                    
                            {
                            
                            if (st[i]==null) st[i]="@In"+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@In"+String.valueOf(i+1);
                            AlarmSensorsNames[i] = st[i].trim();
                            InputNames[i] = st[i].trim();
                            InputStates[i]=false;
                            InputInverts[i]=false;
//                          if (inv[i]!=null)
//                           if (inv[i].trim().equalsIgnoreCase("1")) 
                             //AlarmSensorsStates[i]=false;
                            InputInverts[i]=false;
                            AlarmSensorsActive[i] = false;
                            AlarmSensorsWarning[i] = false;
                            AlarmSensorsMonitoring[i] = false;
                            AlarmSensorsAlarm[i] = false;
                            AlarmSensorsHorn[i] = false;
                            AlarmSensorsEarlyWarning[i] = false;
                            AlarmSensorsSilent[i] = false;
                            AlarmSensorsSMS[i] = false;
                            }
                    
                    ///Read program names
                    if (ehousecommunication.FileExists(name+sep+"names/programs.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/programs.txt",Charset);
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    for (i =0; i < PROGRAMS_COUNT_PRO; i++)
                            {
                            if (st[i]==null) st[i]="@Program"+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@Program"+String.valueOf(i+1);
                            ProgramNames[i] = st[i].trim();
                            }
                    if (ehousecommunication.FileExists(name+sep+"names/rollers.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/rollers.txt",Charset);
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    for (i =0; i < OUTPUTS_COUNT_PRO/2; i++)
                            {
                            if (st[i]==null) st[i]="@Roller "+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@Roller "+String.valueOf(i+1);
                            RollerNames[i] = st[i].trim();
                            }
                    
                    
                    if (ehousecommunication.FileExists(name+sep+"names/secuprograms.txt"))              //secu -rollers programs names 
                        {
                        st=ehousecommunication.getfile(name+sep+"names/secuprograms.txt",Charset);
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    for (i =0; i < PROGRAMS_COUNT_PRO; i++)
                            {
                            if (st[i]==null) st[i]="@SecuProgram"+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@SecuProgram"+String.valueOf(i+1);
                            SecuProgramNames[i] = st[i].trim();
                            }
                    
                    if (ehousecommunication.FileExists(name+sep+"names/adcprograms.txt"))          //adc programs
                        {
                        st=ehousecommunication.getfile(name+sep+"names/adcprograms.txt",Charset);
                        }
                    else for (i=0;i<256;i++) st[i]="";

                    for (i =0; i < ADC_PROGRAMS_COUNT_PRO; i++)
                            {
                            if (st[i]==null) st[i]="@ADC Program "+String.valueOf(i+1);
                            if (st[i].length()==0) st[i]="@ADC Program "+String.valueOf(i+1);
                            ADCProgramNames[i] = st[i].trim();
                            }
                    
                    //read security zones names
                    if (ehousecommunication.FileExists(name+sep+"names/zones.txt"))          
                        {
                        st=ehousecommunication.getfile(name+sep+"names/zones.txt",Charset);
                        }
                    else for (i=0;i<256;i++) st[i]="";
                    
                    for (i =0; i < ZONES_COUNT_PRO; i++)
                                {
                                if (st[i]==null) st[i]="@Zone "+String.valueOf(i+1);
                                ZoneNames[i] = st[i].trim();
                                }
                    changed = false;                                            //flag to set if changed value
                    //                    AdcChanged = false;
                    //LastRead = 0;
                    CurrentProgram = 0;
                    CurrentZone=0;
                    CurrentSecuProgram=0;
                    ADCCurrentProgram=0;
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
    double    tmp=ltmp/100;  //rounding to  2 digits of fractional part
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
    //if (IsCommManager)  return val^inv;
    //else return (!val)^inv;
    return val;
    }
/////////////////////////////////////////////////////////////////////
public int[] GetOptsIndex(String EventName,int AlarmState)
    {
    int i;
    int[] m=new int[2];
    for (i=0;i<OUTPUTS_COUNT_PRO;i++)
        {
        if (OutputNames[i].length()>0)
            if (EventName.compareTo(OutputNames[i])==0)
                {
                m[1]=i;
                m[0]=1;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
                }
        }
    for (i=0;i<INPUTS_COUNT_PRO;i++)
        {
        if (InputNames[i].length()>0)
            if (EventName.compareTo(InputNames[i])==0)
                {
                m[1]=i;
                m[0]=2;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
                }
        }    
    for (i = 0; i < SENSORS_COUNT_PRO; i++)
        {
        if (SensorNames[i].length() > 0)
            if (EventName.compareTo(SensorNames[i]) == 0)
                {
                m[1] = i;
                m[0] = 3;
                return m;// (STATUS_INDEX<<16)+(1<<8)+i;
                }
        }
    for (i = 0; i < SENSORS_COUNT_ALARM_PRO;i++ )
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
/*void UpdateIOStatus(boolean State,  GraphicObject[] ON,GraphicObject[] OFF,int size)
{
    int i=0;
    GraphicObject gr=new GraphicObject("");
    if (CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) return;
    while (i<size)
        {
        if (State==true)   gr=new GraphicObject(ON[i]);
        else gr=new GraphicObject(OFF[i]);
        if (gr.object_type>0)
//        if (gr.event_name!=null)
//            if (gr.event_name.length()>0)
                {
                CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(gr);
                CurrentViewStatIndex++;
                if (CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) 
                    return;
               }
            else return;
//        else return;
        i++;
        }
}*/
/////////////////////////////////////////////////////////////////////////////////
/*String ChangeDynamic(String st)
{
String str=st;
int kk=str.indexOf("%");
int mm=-1;
int indeee=0;
if (kk>=0) mm=str.indexOf("%", kk+1);

if ((kk<0) || (mm<0)) return str;
//str=str.replaceFirst("%TEMP"+indeee+"%", SensorTemps[indeee].toString());
if (str.indexOf("%HEATER_") >= 0)
                {
                str = str.replaceFirst("%HEATER_KOMSTAT%", EhouseTCP.HEATER_KOMSTAT);
                str = str.replaceFirst("%HEATER_RECUPERATOR_MODE%", EhouseTCP.HEATER_RECUPERATOR_MODE);
                str = str.replaceFirst("%HEATER_RECUPERATOR_SPEED%", EhouseTCP.HEATER_RECUPERATOR_SPEED);
                str = str.replaceFirst("%HEATER_RECUPERATOR_TEMP%", EhouseTCP.HEATER_RECUPERATOR_TEMP);
                str = str.replaceFirst("%HEATER_PROGRAM%", EhouseTCP.HEATER_PROGRAM);
                }
if (str.indexOf("%EXTERNAL_") >= 0)
                {
                str = str.replaceFirst("%EXTERNAL_CURRENTZONE%", EhouseTCP.EXTERNAL_CURRENTZONE);
                str = str.replaceFirst("%EXTERNAL_CURRENTZONENAME%", EhouseTCP.EXTERNAL_CURRENTZONENAME);
                str = str.replaceFirst("%EXTERNAL_CURRENTPROGRAM%", EhouseTCP.External_currentprogram);
                str = str.replaceFirst("%EXTERNAL_CURRENTPROGRAMNAME%", EhouseTCP.External_currentprogramname);
                }
str = str.replaceFirst("%CURRENTPROGRAM%", String.valueOf(CurrentProgram+1));
str = str.replaceFirst("%CURRENTPROGRAMNAME%", CurrentProgramName);
str = str.replaceFirst("%CURRENTSECUPROGRAM%", String.valueOf(CurrentSecuProgram+1));
str = str.replaceFirst("%CURRENTSECUPROGRAMNAME%", CurrentSecuProgramName);
str = str.replaceFirst("%CURRENTADCPROGRAM%", String.valueOf(ADCCurrentProgram+1));                       
str = str.replaceFirst("%CURRENTADCPROGRAMNAME%", ADCCurrentProgramName);
str = str.replaceFirst("%CURRENTZONE%", String.valueOf(CurrentZone+1));
str = str.replaceFirst("%CURRENTZONENAME%", CurrentZoneName);

kk=str.indexOf("%");
mm=-1;
if (kk>=0) 
    mm=str.indexOf("%", kk+1);
while ((kk>=0) && (mm>=0))
                    {
                    str=str.replaceFirst("%TEMP"+indeee+"%", SensorTemps[indeee].toString());
                    str=str.replaceFirst("%ADC"+indeee+"%", String.valueOf(SensorABSValues[indeee]));
                    str=str.replaceFirst("%PERCENT"+indeee+"%", SensorPercents[indeee].toString());
                    str=str.replaceFirst("%LIGHT"+indeee+"%", SensorLights[indeee].toString());
                    str=str.replaceFirst("%INVADC"+indeee+"%", String.valueOf(1023-SensorABSValues[indeee]));
                    str=str.replaceFirst("%INVPERCENT"+indeee+"%", String.valueOf(100-SensorPercents[indeee])); 
                    str=str.replaceFirst("%VOLT"+indeee+"%", String.valueOf(SensorVolts[indeee])); 
                    str=str.replaceFirst("%MCP9700_"+indeee+"%", SensorTempsMCP9700[indeee].toString());
                    str=str.replaceFirst("%MCP9701_"+indeee+"%", SensorTempsMCP9701[indeee].toString());
                    kk=str.indexOf("%");mm=-1;
                    if (kk>=0) mm=str.indexOf("%", kk+1);
                    indeee++;
                    if (indeee>=SENSORS_COUNT_PRO) break;
                    }          
return str;                    
}*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*void UpdateADC(int InputNr, float Value, float Min, float Max, GraphicObject[] LOW,GraphicObject[] HIGH,GraphicObject[] OK,int size)
{
    int i=0;
    GraphicObject gr=new GraphicObject("");
    if (CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) return;
    while (i<size)
        {
        if (Value<Min) 
            gr=new GraphicObject(LOW[i]);
        else 
            if (Value>Max) 
                gr=new GraphicObject(HIGH[i]);
        else 
                gr=new GraphicObject(OK[i]);
        
        if (gr.DeviceName!=null)
            //if (gr.event_name.length()>0)
            {
            if (gr.Label_text!=null)
                if (gr.Label_text.indexOf("%")>=0)
                    {
                    String str=gr.Label_text;
                    str=str.replaceFirst("%TEMP%", SensorTemps[InputNr].toString());
                    
                    //printf(str);
                    str=str.replaceFirst("%ADC%", String.valueOf(SensorABSValues[InputNr]));
                    str=str.replaceFirst("%PERCENT%", SensorPercents[InputNr].toString());
                    str=str.replaceFirst("%LIGHT%", SensorLights[InputNr].toString());
                    str=str.replaceFirst("%INVADC%", String.valueOf(1023-SensorABSValues[InputNr]));
                    str=str.replaceFirst("%INVPERCENT%", String.valueOf(100-SensorPercents[InputNr]));
                    str=str.replaceFirst("%VOLT%", SensorVolts[InputNr].toString());
                    str=str.replaceFirst("%TEMPLM35%", SensorTempsLM35[InputNr].toString());
                    str=str.replaceFirst("%TEMPMCP9700%", SensorTempsMCP9700[InputNr].toString());
                    str=str.replaceFirst("%TEMPMCP9701%", SensorTempsMCP9701[InputNr].toString());
                    str=ChangeDynamic(str);
                    
                    gr.Label_text=str;
                    }
            CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(gr);
            CurrentViewStatIndex++;
            if (CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) 
                    return;
           }
            else return;
        i++;
        }
}*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Decode binary status from UDP broadcast or TCP status and update internal cache
 * @param dta - status data from eHouse devices
 * @param InitFromPrevious - unconditional update status
 * @param CommManager - Offset for CommManager in case of Ehouse 1 under CommManager supervision (CommManager status attached  after eHouse1 controller status)
 * @return 
 */
public boolean WriteCheckIfChangedAndCpy(byte[] dta, boolean InitFromPrevious,int size)

        { int i,k;
        int sss=0;
        EhouseTCP.FinishedPro=false;
        boolean tempboolean = false;
        boolean unconditional = InitFromPrevious;
        int TotalOffset=STATUS_PRO_OFFSET;
        int Max_ADC = 
                (STATUS_ADC_PRO_END-STATUS_ADC_PRO)/2;
        int CurrentSize=size;
        Calendar c = Calendar.getInstance(); 
        recent=c.getTime().toLocaleString();
//            boolean changedd=false;
            if (InitFromPrevious) changed = true;
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
        if (changed) 
        {   //update data
//        ClearActiveVisualization();            
        {
            for (k = 0; k < 8; k++)
                {   ///output states
                    int off=0;
                    for (sss=0;sss<OUTPUTS_COUNT_PRO;sss+=8)
                        {
                        OutputStates[k+sss] = GetInOutValue(CurrentStatus[TotalOffset+STATUS_OUT_PRO+off]&0xff, k);
 //                       UpdateIOStatus(OutputStates[k+sss],OutputViewH[k+sss],OutputViewL[k+sss],GRAPHICS_OBJECTS_MAX);
                        off++;
                        }
                }    
            for (k = 0; k < 8; k++)
                {   ///output states
                    int off=0;
                    for (sss=0;sss<INPUTS_COUNT_PRO;sss+=8)
                        {
                        InputStates[k+sss]= ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_INPUTS_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(InputStates[k+sss],InputViewH[k+sss],InputViewL[k+sss],GRAPHICS_OBJECTS_MAX);
                        off++;
                        }
                }
            for (k = 0; k < 8; k++)
                {   ///output states
                    int off=0;
                    for (sss=0;sss<INPUTS_COUNT_PRO;sss+=8)
                        {
                        AlarmSensorsActive[k+sss]= ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_INPUTS_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsActive[k+sss],InputViewH[k+sss],InputViewL[k+sss],GRAPHICS_OBJECTS_MAX);
                        AlarmSensorsAlarm[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_ALARM_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsAlarm[k+sss],AlarmViewSensorsH[k+sss],AlarmViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsWarning[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_WARNING_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsWarning[k+sss],WarningViewSensorsH[k+sss],WarningViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsMonitoring[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_MONITORING_PRO+off]&0xff, k),InputInverts[k+sss]);
//                       UpdateIOStatus(AlarmSensorsMonitoring[k+sss],MonitoringViewSensorsH[k+sss],MonitoringViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsHorn[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_HORN_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsHorn[k+sss],HornViewSensorsH[k+sss],HornViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsSilent[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_SILENT_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsSilent[k+sss],SilentViewSensorsH[k+sss],SilentViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsEarlyWarning[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_EARLY_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsEarlyWarning[k+sss],EarlyViewSensorsH[k+sss],EarlyViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        AlarmSensorsSMS[k+sss] =ginv( GetInOutValue(CurrentStatus[TotalOffset+STATUS_SMS_PRO+off]&0xff, k),InputInverts[k+sss]);
//                        UpdateIOStatus(AlarmSensorsSMS[k+sss],SMSViewSensorsH[k+sss],SMSViewSensorsL[k+sss],GRAPHICS_OBJECTS_MAX);                        
                        off++;
                        }
                }
                CurrentProgram=CurrentStatus[TotalOffset+STATUS_PROGRAM_NR]&0xff;
                CurrentZone = CurrentStatus[TotalOffset+STATUS_ZONE_NR]&0xff;            
                CurrentSecuProgram=CurrentStatus[TotalOffset+STATUS_SECU_PROGRAM_NR]&0xff;
                ADCCurrentProgram=CurrentStatus[TotalOffset+STATUS_ADC_PROGRAM]&0xff;
                
                CurrentProgramName="";
               if (CurrentProgram<PROGRAMS_COUNT_PRO)
                    CurrentProgramName=ProgramNames[CurrentProgram];
               else CurrentProgramName="-";
               if (CurrentProgramName==null) CurrentProgramName="-";
               
               CurrentSecuProgramName="";
               if (CurrentSecuProgram<SECU_PROGRAMS_COUNT_PRO)
                    CurrentSecuProgramName=SecuProgramNames[CurrentSecuProgram];
               else CurrentSecuProgramName="-";
               if (CurrentSecuProgramName==null) CurrentSecuProgramName="-";
               
               
                if (CurrentZone<ZONES_COUNT_PRO)
                    CurrentZoneName=ZoneNames[CurrentZone];
                else CurrentZoneName="-";
            if (CurrentZoneName==null) CurrentZoneName="-";
            ADCCurrentProgramName="";
            
            }
        i=0;
        while (i < Max_ADC)
                {                                               //offset od 2                       //offset od 2
                int temppp= CurrentStatus[TotalOffset+i * 2 + 1 ]&0xff;
                //if (temppp<0) temppp+=256;
                 temppp+=((CurrentStatus[TotalOffset+i * 2 ]&0xff) << 8);
                if (SensorABSValues[i] != temppp)
                        {
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
        int m=0;
        while (OtherViews[m].event_name!=null)
            {
            CurrentViewStat[CurrentViewStatIndex]=new GraphicObject(OtherViews[m]);
            CurrentViewStat[CurrentViewStatIndex].Label_text=ChangeDynamic(CurrentViewStat[CurrentViewStatIndex].Label_text);
            CurrentViewStatIndex++;
            if(CurrentViewStatIndex>=GRAPHIC_OBJECTS_CURRENT) 
            {
                EhouseTCP.FinishedPro=true;
                return false;
            //break;
            }
            m++;
            }
        }
        if (changed)  
            {
                 XmlChanged=true;
            
            }
        EhouseTCP.FinishedPro=true;
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

