/** 
 * Ehouse 1 Device Status / rs485 cache status instance 
 * roommanager , heatmanager, externalmanager status EhDev array 
 * author: Robert Jarzabek / iSys.Pl
 * 
 * http://www.isys.pl/      -   strona domowa producenta systemu eHouse
 * http://home-automation.isys.pl/  - eHouse producer & manufacturer home page 
 * http://www.ehouse.pro/ - 
 * http://inteligentny-dom.ehouse.pro/  - inteligentny dom eHouse przyklady, projekty, zrob to sam, programowanie, projektowanie, przyklady
 * http://home-automation.ehouse.pro/   - eHouse home automation DIY, programming, designing, tips&trips, examples of usage
 * http://forum.eHouse.pro/
 * 
 */
package ehouse4openremote;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Calendar;
public  class StatusEhouse
  {         
    /**
     * Indexes locations in status buffer - offset in binary status buffer
     * 
     */
final static int STATUS_OFFSET                 = 1+1;                     //byte index location of binary status query results
final static int RM_STATUS_ADC                 = 1  + STATUS_OFFSET;    //start of adc measurement
final static int RM_STATUS_OUT  	       = 17 + STATUS_OFFSET;    //RM start of outputs
final static int HM_STATUS_OUT                 = 33 + STATUS_OFFSET;    //HM start of outputs
final static int RM_STATUS_IN                  = 20 + STATUS_OFFSET;    //RM start of inputs
final static int RM_STATUS_INT                 = 21 + STATUS_OFFSET;    //rm start of inputs (fast)
final static int RM_STATUS_OUT25               = 22 + STATUS_OFFSET;    //rm starts of outputs => 25-32
final static int RM_STATUS_LIGHT               = 23 + STATUS_OFFSET;    //rm light level start
final static int RM_STATUS_ZONE_PGM            = 26 + STATUS_OFFSET;    //rm current zone
final static int RM_STATUS_PROGRAM             = 27 + STATUS_OFFSET;    //rm current program
final static int RM_STATUS_INPUTEXT_A_ACTIVE   = 28 + STATUS_OFFSET;    //em input extenders A status active inputs
final static int RM_STATUS_INPUTEXT_B_ACTIVE   = 32 + STATUS_OFFSET;    //em input extenders B status active inputs
final static int RM_STATUS_INPUTEXT_C_ACTIVE   = 36 + STATUS_OFFSET;    //em input extenders C status active inputs
final static int RM_STATUS_INPUTEXT_A          = 40 + STATUS_OFFSET;    //em --||-
final static int RM_STATUS_INPUTEXT_B          = 50 + STATUS_OFFSET;    //em 
final static int RM_STATUS_INPUTEXT_C          = 60 + STATUS_OFFSET;    //em 
final static int HM_STATUS_PROGRAM             = 36 + STATUS_OFFSET;    //hm current program
final static int HM_STATUS_KOMINEK 	       = 46 + STATUS_OFFSET;    //hm status bonfire
final static int HM_STATUS_RECU 	       = 48 + STATUS_OFFSET;    //hm status recu
final static int HM_WENT_MODE		       = 49 + STATUS_OFFSET;    //hm went mode
public String XML="";
public String charset="";
public  long CheckSum=0;                  //CheckSum for UDP transmision veryfication 
            public String recent="";            //recently updated
            public boolean InitializedMatrix=false;
            int STATUS_INDEX = -1;
            final static int OUTPUTS_COUNT_RM       = 35; 	//max nr of digital outputs for device
            final static int INPUTS_COUNT_RM        = 16;   //max nr of digital inputs for device
            final static int SENSORS_COUNT_RM       = 16;   //max nr of adc inputs for device
            final static int SENSORS_COUNT_ALARM    = 128;  //max nr of alarm sensors inputs for external manager
            final static int STATUS_SIZE            = 256;  //max length of query status received
//            final static int GRAPHIC_OBJECTS_OTHER  = 1000; //max nr of unconditional graphic objects for device (unconditional cache)
//            final static int GRAPHIC_OBJECTS_CURRENT= 3000; //max nr ob graphic objects for device (dynamic cache)
            final static int DIMMERS_COUNT_RM       = 3;    //max nr of pwm dimmers per device 
            final static int DIMMERS_RGB_COUNT_RM   = 1;    //max nr of rgb - pwm dimer for device
            final static int PROGRAMS_COUNT_RM      = 24;   //programs count
        /// All necessary data for visualisation (names and status)
            public String   DeviceName;                                                 //device name
            public String[] ProgramNames        = new String[PROGRAMS_COUNT_RM];        //program names list
            public String[] OutputNames         = new String[OUTPUTS_COUNT_RM];         //names of outputs list
            public String[] DimmerNames         = new String[DIMMERS_COUNT_RM];         //names of dimmers list
            public String[] DimmerRGBNames      = new String[DIMMERS_RGB_COUNT_RM];     //names of RGB Dimmers list
            public int[]    Dimmer              = new int[DIMMERS_COUNT_RM];            //dimmers values
            public boolean XmlChanged=false;        //flag for xml file generation
            public int[]    DimmerRGB           = new int[DIMMERS_RGB_COUNT_RM];        //RGB dimmer values
            public int      OtherViewsIndex     = 0;    //index of last item in unconditional cache buffer
            public int      CurrentViewStatIndex = 0;   //index of last item in device cache buffer
            public String[] InputNames      = new String[INPUTS_COUNT_RM];  //names of inputs list
            public String[] SensorNames     = new String[SENSORS_COUNT_RM]; //ADC Sensors (inputs) names
            public int[] SensorType=new int[SENSORS_COUNT_RM];  //type of adc sensor (%, invert %, value, absolute value, voltage, temp, light etc)
            public int[] SensorABSValues            = new int[SENSORS_COUNT_RM];    //sensors absolute values 0..1023
            public int[] Calibration                = new int[SENSORS_COUNT_RM];    //calibration * 100 value for acurate measurement calculation
            public Double[] SensorTemps             = new Double[SENSORS_COUNT_RM]; //sensor Temp value calculated
            public Double[] SensorPercents          = new Double[SENSORS_COUNT_RM]; //sensor percent value calculate comparing to power supply
            public Double[] SensorLights            = new Double[SENSORS_COUNT_RM]; //inverted percent value calculation for negative scale sensors
            public Double[] SensorVolts             = new Double[SENSORS_COUNT_RM]; //voltage value calculated
            public Boolean[] OutputStates           = new Boolean[OUTPUTS_COUNT_RM];//digital output states / values (0/1) ON/OFF 
            public Boolean[] InputStates            = new Boolean[INPUTS_COUNT_RM]; //digital input states / values (0/1) ON/OFF
            public Boolean[] AlarmSensorsActive     = new Boolean[SENSORS_COUNT_ALARM]; //alarm sensors inputs active state (0/1) for information / no alarm, warning, monitoring, sms
            public Boolean[] AlarmSensorsWarning = new Boolean[SENSORS_COUNT_ALARM];    //alarm sensors inputs warning state (0/1) 
            public Boolean[] MonitoringSensorsWarning = new Boolean[SENSORS_COUNT_ALARM];   //alarm sensors inputs states for monitoring output
            public Boolean[] AlarmSensorsAlarm = new Boolean[SENSORS_COUNT_ALARM];  //alarm sensor inputs alarm states (0/1) for alarm output
            public String[] AlarmSensorsNames = new String[SENSORS_COUNT_ALARM];    //alarm sensor inputs names list

            public int DevAdrH;                                             //device addr h
            public int DevAdrL;                                             //device addr l
            public boolean changed;                                            //flag to set if changed value
            public boolean AdcChanged;                                      //adc measurement values was changed from previous status - flag
            //public static DateTime LastRead;
            public int CurrentProgram;                                      //current program of device
            public String CurrentProgramName;                               //current program name of device
            public int CurrentZone;                                         //curent zone nr (for EM)
            public String CurrentZoneName;                                  //curent zone name (for em)
            public Byte[] CurrentStatus = new Byte[STATUS_SIZE];            //current status binary (received from host not decoded for comparison)
            //public Byte[] PreviousStatus = new Byte[STATUS_SIZE];
            public boolean IsBatch;                                         //not device but batch file
            public boolean IsRM;                                            // device is RoomManager
            public boolean IsHM;                                            // device is HeatManager
            public boolean IsEM;                                            // device is ExternalMaanger
            public boolean IsAlarm;                                         // device is SecurityManager
            public boolean IsEthernet;
            public int VCC; //power suply for calibration * 100

////////////////////////////////////////////////////////////////////////////////////////////////////////////
        final static int ADC_N_OFFSET = 4;                                  //binary status byte index offset from 0 ADC Results 2 bytes per input
        final static int O_N_OFFSET=ADC_N_OFFSET+16;                        //offset for digital outputs
        final static int I_N_OFFSET=O_N_OFFSET+35;                          //offset for digital inputs
//////////////////////////////////////////////////////////////////////////////////////////////////////////        
      public void SetStatusIndex(int nr)
      {
        STATUS_INDEX=nr;

      }
//////////////////////////////////////////////////////////////////////////////////////////////////////////      
/**
 * Check ADC changed flag
 * @return 
 */      
      
////////////////////////////////////////////////////////////////////////////////////////////////////////////
// check adc changed flag and reset the flag if it was changed
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
// search for device name return true if dname is the saame as current device name
//
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
/***
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// get temperature calculation from lm335 digital temperature sensor
// dta (absolute value 0..1023) 
// calibration - for calculation value including calibration
// VCC real power supply for reference
// round to 1 digit of fraction
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////
*/
private double gettemplm(int dta, int calibration, double k, int VCC)
    {
    double temp;
    temp=(dta*VCC/(1023*k))+calibration/100;
//t=Math.Round(temp*10);
//temp=(t/10);
    temp = ((double)Math.round(temp*10))/10;
    return temp;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//check integer and convert str to integer
/***
 * Check Integer in string  and parse to int
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
/**
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// read alarm sensors names from file and initialization of values;
//
 */
public void ReadAlarmSensorsNames(String pat)
{
    int i=0;
    int z=0;
    if (ehousecommunication.FileExists(pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(pat,"");
                        while (AlarmS[i]!=null)
                        {
                            {
                                AlarmSensorsActive[i] = false;
                                AlarmSensorsAlarm[i] = false;
                                AlarmSensorsWarning[i] = false;
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
                            AlarmSensorsNames[i] = "";
                            i++;
                            }
                            
                        }
}                  
/////////////////////////////////////////////////////////////////////////////////
/**
 * read heatmanager temperature sensors names, output names and program names for initialization
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void ReadHMSensorsNames()
{
    int i=0;
    int z=0;
    String pat="hmtemps.txt";
    //String charsets[]=ehousecommunication.getfile(ehousecommunication.locpath+pat,"heatmanager.charset");
    if (ehousecommunication.FileExists(ehousecommunication.locpath+pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(ehousecommunication.locpath+pat,charset);
                        while (AlarmS[i]!=null)
                        {
                            

                            SensorNames[i] = AlarmS[i].trim();
                            i++;
                            if (i == SENSORS_COUNT_RM) break;
                           
                            
                        }
                            while (i<SENSORS_COUNT_RM)
                            {
                            
                            SensorNames[i] = "";
                            i++;
                            }
                            
                        }
    i=0;
    pat="hmouts.txt";
    if (ehousecommunication.FileExists(ehousecommunication.locpath+pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(ehousecommunication.locpath+pat,charset);
                        while (AlarmS[i]!=null)
                        {
                            

                            OutputNames[i] = AlarmS[i].trim();
                            i++;
                            if (i == OUTPUTS_COUNT_RM) break;
                           
                            
                        }
                            while (i<OUTPUTS_COUNT_RM)
                            {
                            
                            OutputNames[i] = "";
                            i++;
                            }
                            
                        }
    i=0;
    pat="heater.evs";
    if (ehousecommunication.FileExists(ehousecommunication.locpath+pat))
                        {
                         String[] AlarmS=ehousecommunication.getfile(ehousecommunication.locpath+pat,charset);
                        while (AlarmS[i]!=null)
                        {
                            

                            ProgramNames[i] = AlarmS[i].trim();
                            i++;
                            if (i == PROGRAMS_COUNT_RM) break;
                           
                            
                        }
                            while (i<PROGRAMS_COUNT_RM)
                            {
                            
                            ProgramNames[i] = "";
                            i++;
                            }

                        }
    
}                  

/******************************************************************************************************/
/**
 * load configuration names for current controller 
 */
public void LoadDta(String name)
        {
            
            String temp;
            int i;
            {

            if (ehousecommunication.FileExists(name))
                {
                    String[] SteH=ehousecommunication.getfile(name,charset);

                    DeviceName = SteH[0].trim();                                   //device name
                    DevAdrH = check_in(SteH[1],0);                                             //device addr h
                    DevAdrL = check_in(SteH[2],0);                                             //device addr l
                    temp = SteH[3].trim();
                    if (temp.compareTo("HM")==0) IsHM = true;   //heatmanager flag
                    if (temp.compareTo("EM")==0) IsEM = true;   //externalmanager flag
                    if (temp.compareTo("RM")==0) IsRM = true;   //roommanager flag
                    for (i = 0; i < SENSORS_COUNT_RM; i++)      //read sensor names and reset values to 0
                        {
                        SensorNames[i] = SteH[4+i].trim();
                        if (SensorNames[i]==null) SensorNames[i]="@ADC"+String.valueOf(i+1);
                        if (SensorNames[i].length()==0) SensorNames[i]="@ADC"+String.valueOf(i+1);
                        SensorABSValues[i] = 0;
                        SensorTemps[i] = 0.0;      //sensor Temp value calibrated
                        SensorPercents[i] = 0.0;   //sensor percent value
                        SensorLights[i] = 0.0;
                        SensorVolts[i]=0.0;
                        Calibration[i] = -27315;   //!!!! future use - read calibration offset from file
                        }
                    VCC = 5000;// 4810; ;           //!!! future use - read power supply voltage from file for calibration
                    for (i = 0; i < OUTPUTS_COUNT_RM; i++)  //read all output names and reset values to 0
                        {
                        OutputNames[i] = SteH[4+i+SENSORS_COUNT_RM].trim();
                        if (OutputNames[i]==null) OutputNames[i]="@OUT"+String.valueOf(i+1);
                        if (OutputNames[i].length()==0) OutputNames[i]="@OUT"+String.valueOf(i+1);
                        OutputStates[i] = false;
                        }
                    for (i = 0; i < INPUTS_COUNT_RM; i++)   //read all inputs names and reset values to 0
                        {
                        InputNames[i] = SteH[4+i+OUTPUTS_COUNT_RM+SENSORS_COUNT_RM].trim();
                        if (InputNames[i]==null) InputNames[i]="@IN"+String.valueOf(i+1);
                        if (InputNames[i].length()==0) InputNames[i]="@IN"+String.valueOf(i+1);
                        InputStates[i] = false;
                        }
                        
                    changed = false;                                            //flag to set if changed value
//                    AdcChanged = false;
                    //LastRead = 0;
                    CurrentProgram = 0;
                    CurrentProgramName = "";
                    for (i = 0; i<STATUS_SIZE; i++)
                    {
                        CurrentStatus[i] = 0;
//                        PreviousStatus[i] = 0;
                    }
                    IsBatch = false;
                    IsAlarm = false;
                    IsEthernet = false;
                if(ehousecommunication.EnableXMLStatus) MakeXml.start();
                }
            }
            
        }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
/** calculate percent value in reference to power supply vcc rounds to 2 fractional digit */
//
public double calculate_percent(int dta)
            {
    
        
             double tmp = ((double)Math.round((double)((dta * 100 ) / 1023)*100))/100;  //rounding to  2 digits of fractional part
                //tmp = (tmp) / 100;
            return tmp;
            }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
/** calculate percent value in reference to power supply vcc rounds to 2 fractional digit */
//
public double calculate_voltage(int dta)
            {
    
        
             double tmp = Math.round((double)((dta *  VCC ) / 1023)*100);///10000000;  //do 2 miejsc po przecinku
             tmp=tmp/100000;
                //tmp = (tmp) / 100;
            return tmp;
            }


////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// calculate light level value 
//
public double getlight(int dta)
  {
 
  //double dtta=(1023-dta)*100;
      double dtta = (dta) * 100;
  dtta = ((((double)dtta) / 1023));
    return ((double)Math.round(dtta*10))/10;
  }

////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
/**
 * Return adc value for current input
 * 
 * @param index index of input
 * @return value in string with suffix
 */
//
//
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// get adc value of sensor at index
//
//
public String ADCVal(int index)
{
if (!IsHM) 
    if (index==0) 
        return SensorLights[index].toString()+" %"; //if index 0 and not HM default sensor is Light Sensor
    return SensorTemps[index].toString()+" C"; // otherwise temperature value
        
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * get digital input and output state (stored in bits of status)
 *
 * 
 * @param dta - byte
 * @param offset - offset for single bit
 * @return 
 */
//
//////////////////////////////////////////////////////////////////////////////////////////////////////////////

public boolean GetInOutValue(int dta, int offset)
    {
    int temp=dta>>offset;
    if ((temp & 0x01)>0) 
            return true;
    else return false;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// 
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
/**
 * perform binary query status decoding
 * 
 * 
 * 
 * @param dta - status data bytes
 * @param InitFromPrevious - unconditional update
 * @return 
 */
///
////////////////////////////////////////////////////////////////////////////////////////////////////////////

public boolean WriteCheckIfChangedAndCpy(byte[] dta, boolean InitFromPrevious)

        { int i,k;
        EhouseTCP.FinishedEH1=false;
        int sss=0;
        boolean tempboolean = false;
        boolean unconditional = InitFromPrevious;
        int Max_ADC = 8;
        Calendar c = Calendar.getInstance(); 
        recent=c.getTime().toLocaleString();
//            boolean changedd=false;
            if (InitFromPrevious) changed = true;   //unconditional update
            else changed=false;
            if (!InitFromPrevious)  //normal query
              //if (dta != null)
                for (i = 0; i < STATUS_SIZE; i++) //compare binary status data from query byte by byte
                    {
                     if (CurrentStatus[i] != dta[i])    //if different from previous
                        {
                            CurrentStatus[i] = dta[i];  //update
                            changed = true;             // set changed flag
                        }
                    }
        
        if (changed) //data changed or unconditional
        {   //update data
            
        if ((IsRM) || (IsEM)) 
            {
            Max_ADC=8;
            if ((IsRM) && (!InitFromPrevious))
                CurrentStatus[STATUS_SIZE-1] = 0;
            for (k = 0; k < 8; k++) //update output and input states and visualization objects
                {
                    OutputStates[k] = GetInOutValue(CurrentStatus[RM_STATUS_OUT]&0xff, k);
                    OutputStates[k + 8]  = GetInOutValue(CurrentStatus[RM_STATUS_OUT + 1]&0xff, k); 
                    OutputStates[k + 16] = GetInOutValue(CurrentStatus[RM_STATUS_OUT + 2]&0xff, k); 
                    OutputStates[k + 24] = GetInOutValue(CurrentStatus[RM_STATUS_OUT25]&0xff, k); 
                    InputStates[k]       = GetInOutValue(CurrentStatus[RM_STATUS_IN]&0xff, k);
                    InputStates[k+8]     = GetInOutValue(CurrentStatus[RM_STATUS_INT]&0xff, k);
                if (IsEM)   //for em additionally update input extenders alarm sensor inputs states and visualization objects
                    {
                    AlarmSensorsActive[k] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A_ACTIVE]&0xff, k);
                    AlarmSensorsActive[k + 8] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A_ACTIVE + 1]&0xff, k);
                    AlarmSensorsActive[k + 16] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A_ACTIVE + 2]&0xff, k);
                    AlarmSensorsActive[k + 24] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A_ACTIVE + 3]&0xff, k);
                    AlarmSensorsActive[k + 32] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A + 4]&0xff, k);
                    AlarmSensorsActive[k + 40] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B_ACTIVE + 0]&0xff, k);
                    AlarmSensorsActive[k + 48] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B_ACTIVE + 1]&0xff, k);
                    AlarmSensorsActive[k + 56] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B_ACTIVE + 2]&0xff, k);
                    AlarmSensorsActive[k + 64] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B_ACTIVE + 3]&0xff, k);
                    AlarmSensorsActive[k + 72] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 4]&0xff, k);
                    AlarmSensorsActive[k + 80] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_C_ACTIVE + 0]&0xff, k);
                    AlarmSensorsActive[k + 88] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_C_ACTIVE + 1]&0xff, k);
                    AlarmSensorsActive[k + 96] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_C_ACTIVE + 2]&0xff, k);
                    AlarmSensorsActive[k + 104] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_C_ACTIVE + 3]&0xff, k);
                    AlarmSensorsActive[k + 112] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_C + 4]&0xff, k);
                    AlarmSensorsAlarm[k] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A]&0xff, k);
                    AlarmSensorsAlarm[k + 8] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A + 1]&0xff, k);
                    AlarmSensorsAlarm[k + 16] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A + 2]&0xff, k);
                    AlarmSensorsAlarm[k + 24] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A + 3]&0xff, k);
                    AlarmSensorsAlarm[k + 32] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_A + 4]&0xff, k);
                    AlarmSensorsAlarm[k + 40] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 0]&0xff, k);
                    AlarmSensorsAlarm[k + 48] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 1]&0xff, k);
                    AlarmSensorsAlarm[k + 56] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 2]&0xff, k);
                    AlarmSensorsAlarm[k + 64] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 3]&0xff, k);
                    AlarmSensorsAlarm[k + 72] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 4]&0xff, k);
                    AlarmSensorsAlarm[k + 80] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 0]&0xff, k);
                    AlarmSensorsAlarm[k + 88] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 1]&0xff, k);
                    AlarmSensorsAlarm[k + 96] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 2]&0xff, k);
                    AlarmSensorsAlarm[k + 104] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 3]&0xff, k);
                    AlarmSensorsAlarm[k + 112] = GetInOutValue(CurrentStatus[RM_STATUS_INPUTEXT_B + 4]&0xff, k);
                    }
                }

            CurrentProgram=CurrentStatus[RM_STATUS_PROGRAM]&0xff;    //update current program nr
            CurrentZone = CurrentStatus[RM_STATUS_ZONE_PGM]&0xff;    //update current zone
            CurrentProgramName="";//na razie                    //load current program name from list
            CurrentZoneName="";                                 //load cuurrent zones from list
            
            }
        if (IsEM)
        {
            CurrentProgramName = "";
        }
        if (IsHM)   //heatmanager
            {
            Max_ADC=16;
            CurrentProgram = CurrentStatus[HM_STATUS_PROGRAM]&0xff;  //current program for heatmanager
            CurrentProgramName = "";//na razie                  //current program name for heatmanager
            for (k = 0; k < 8; k++) //update output states and visualization objects
                {
                int mm = 0;
                OutputStates[k] = GetInOutValue(CurrentStatus[HM_STATUS_OUT]&0xff, k);
                OutputStates[k + 8] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 1]&0xff, k);
                OutputStates[k + 16] = GetInOutValue(CurrentStatus[HM_STATUS_OUT + 2]&0xff, k);
//                InputStates[k] = GetInOutValue(CurrentStatus[RM_STATUS_IN ], k);
  //              InputStates[k + 8] = GetInOutValue(CurrentStatus[RM_STATUS_INT ], k);
                }
            }
        i=0;
        while (i < Max_ADC)         //perform all adc results (2bytes per result)
                {                                               //offset od 2                       //offset od 2
                //int temppp=(CurrentStatus[i * 2 + 2] << 8) + CurrentStatus[i * 2 + 1 + 2];
                int temppp= CurrentStatus[i * 2 + RM_STATUS_ADC+1 ]&0xff;
                if (temppp<0) temppp+=256;
                temppp+=((CurrentStatus[RM_STATUS_ADC+i * 2 ]&0xff) << 8);
                
                if (SensorABSValues[i] != temppp) //compare to previous value
                    {
                    AdcChanged = true;  //changed flag for ADC
                    }
               SensorABSValues[i] = temppp;    //update value
               SensorTemps[i] = gettemplm(temppp, Calibration[i], 10, VCC);    //calculate temp value
               //SensorLights[i] = getlight(temppp); //calculate light level value
               SensorPercents[i] = calculate_percent(temppp);  //calculate percent value
			   SensorLights[i]=100-SensorPercents[i];
               SensorVolts[i]=calculate_voltage(temppp);
               //SensorTempsLM35[i]=gettemplm35(temppp);
               i++;
               }
        //wrlog();
        int m=0;
        for (int ii=0;ii<DIMMERS_RGB_COUNT_RM;ii++) DimmerRGB[ii]=0;
        
        for (int ii=0;ii<DIMMERS_COUNT_RM;ii++)         //set dimmer values for single dimmer and RGB consists of 3 single
            {
            DimmerRGB[ii/3]=CurrentStatus[RM_STATUS_LIGHT+(ii%3)]&0xff;
            if ((ii%3)!=2) 
                    DimmerRGB[ii/3]=DimmerRGB[ii/3]<<8;
            Dimmer[ii]= (int) CurrentStatus[RM_STATUS_LIGHT+(ii)]&0xff;
             }
        
        
        }
        if (changed) XmlChanged=true;
        EhouseTCP.FinishedEH1=true;
        return changed;
        }
////////////////////////////////////////////////////////////////////////////////////////////////////////////
///
/**
 check if this is correct device by address

 * 
 * @param devadrh - address high
 * @param devadrl - address low
 * @return true if address matches
 */
//
////////////////////////////////////////////////////////////////////////////////////////////
        public  boolean isAddress(int devadrh, int devadrl)
        {
        if ((devadrh==DevAdrH) && (devadrl==DevAdrL)) return true;
            return false;

        }
         
    
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
        sleep(100);
        }
    catch (Exception e) 
        {
        }
    if (ehousecommunication.Terminate) return;
    }
String eol,Event;
XmlChanged=false;
eol="\r\n";
XML="";

String header,footer,body,bodyheader,bodyfooter;    
header="";
if (DevAdrH==2)
    header="\r\n";
header+="<?xml version=\"1.0\" encoding=\"UTF-8\" ?> \r\n <eHouse xmlns=\"http://www.isys.pl\" \r\n xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n            xsi:schemaLocation=\"http://www.openremote.org protocol.xsd\">";
header+="<Device>"+eol;
header+="<Name>"+DeviceName+"</Name>"+eol;
header+="<Address>"+DevAdrH+","+DevAdrL+"</Address>"+eol;
//header+="<IPAddress>192.168."+DevAdrH+"."+DevAdrL+"</IPAddress>"+eol;            
//header+="<AddressCombined>"+DevAdr+"</AddressCombined>"+eol;
header+="<StateModified>"+ehousecommunication.bo(changed)+"</StateModified>"+eol;
header+="<ADCModified>"+ehousecommunication.bo(AdcChanged)+"</ADCModified>"+eol;
header+="<CurrentProgram>"+ehousecommunication.i(CurrentProgram)+"</CurrentProgram>"+eol;
header+="<CurrentProgramName> "+ CurrentProgramName+" </CurrentProgramName>"+eol;
//header+="<ADCCurrentProgram>"+i(ADCCurrentProgram)+"</ADCCurrentProgram>"+eol;
//header+="<ADCCurrentProgramName> "+ ADCCurrentProgramName+" </ADCCurrentProgramName>"+eol;
header+="<CurrentStatus>"+ehousecommunication.hx(CurrentStatus,STATUS_SIZE)+"</CurrentStatus>"+eol;
header+="<RecentStatus>"+recent+"</RecentStatus>"+eol;
if (IsRM) header+="<Devicetype>"+"RoomManager"+"</Devicetype>"+eol;
if (IsEM) header+="<Devicetype>"+"ExternalManager"+"</Devicetype>"+eol;
if (IsHM ) header+="<Devicetype>"+"HeatManager"+"</Devicetype>"+eol;
//if (IsLevelManager) header+="<Devicetype>"+"LevelManager"+"</Devicetype>"+eol;
if (IsEthernet)  header+="<InterfaceType>"+"Ethernet"+"</InterfaceType>"+eol;;
        if (IsEM)       //ExternalManager
            {
            header+="<CurrentZone>"+ehousecommunication.i(CurrentZone)+"</CurrentZone>"+eol;
            header+="<CurrentZoneName>"+ CurrentZoneName+"</CurrentZoneName>"+eol;

            }
bodyheader=eol+"<Outputs>"+eol;
body="";
int maxio=OUTPUTS_COUNT_RM;
if ((IsRM) || (IsEM)) maxio=35;
if (IsHM) maxio=21;
for (int i=0;i<maxio;i++)
{
    if (((!ehousecommunication.IgnoreAtChar) || (OutputNames[i].indexOf("@")<0)))    
    
    {
    int tmp[]={DevAdrH,DevAdrL,0x1,0,1,0,0,0,0,0};
    tmp[3]=i;
    String state="0"; String value="Off";   if (OutputStates[i]) {state="1";value="On";}
    body+="    <Item>"+eol+
    "        <Name>"+OutputNames[i]+"</Name>"+eol+
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <State>"+state+"</State>"+eol+
    "        <Value>"+value+"</Value>"+eol;
    if (!IsHM)
    {
    Event=ehousecommunication.hix(tmp,10);
    body+="        <EventOn>"+Event+"</EventOn>"+eol;
    tmp[4]=0;Event=ehousecommunication.hix(tmp,10);
    body+="        <EventOff>"+Event+"</EventOff>"+eol;
    tmp[4]=2;Event=ehousecommunication.hix(tmp,10);
    body+="        <EventToggle>"+Event+"</EventToggle>"+eol            ;
            }
    body+="    </Item>"+eol+eol;
}
}
bodyfooter=eol+"</Outputs>"+eol;

header+=bodyheader+body+bodyfooter+eol;
bodyheader=eol+"<ADCInputs>"+eol;
body=eol;
bodyfooter=eol+"</ADCInputs>"+eol;
maxio=SENSORS_COUNT_RM;
if (!IsHM) maxio=8;
for (int i=0;i<maxio;i++)
{
    
if (((!ehousecommunication.IgnoreAtChar) || (SensorNames[i].indexOf("@")<0)))    
    {
    
  body+="    <Item>"+eol+
    "        <Name>"+SensorNames[i]+"</Name>"+eol+
    "        <ADCValue>"+SensorABSValues[i]+"</ADCValue>"+eol+
    "        <ADCTempValue>"+SensorTemps[i]+"</ADCTempValue>"+eol+
    "        <ADCLM335TempValue>"+SensorTemps[i]+"</ADCLM335TempValue>"+eol+
    /*"        <ADCLM35TempValue>"+SensorTempsLM35[i]+"</ADCLM35TempValue>"+eol+            
    "        <ADCMCP9700TempValue>"+SensorTempsMCP9700[i]+"</ADCMCP9700TempValue>"+eol+
    "        <ADCMCP9701TempValue>"+SensorTempsMCP9701[i]+"</ADCMCP9701TempValue>"+eol+*/
    "        <ADCPercentValue>"+SensorPercents[i]+"</ADCPercentValue>"+eol+
    "        <ADCInvertedPercentValue>"+SensorLights[i]+"</ADCInvertedPercentValue>"+eol+
    "        <ADCVoltageValue>"+SensorVolts[i].toString()+"</ADCVoltageValue>"+eol+
    "        <ADCCalibrationValue>"+Calibration[i]+"</ADCCalibrationValue>"+eol+
    
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <SensorType>"+SensorType[i]+"</SensorType>"+eol;
  
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
     body+="    </Item>"+eol+eol;}
}

header+=bodyheader+body+bodyfooter+eol;



bodyheader=eol+"<Inputs>"+eol;
body=eol;
bodyfooter=eol;
maxio=INPUTS_COUNT_RM;
if ((IsRM) || (IsHM) || (IsEM)) maxio=12;
for (int i=0;i<maxio;i++)
{
 if (((!ehousecommunication.IgnoreAtChar) || (InputNames[i].indexOf("@")<0)))    
{
//    int tmp[]={DevAdrH,DevAdrL,0x21,0,1,0,0,0,0,0};
  //  tmp[3]=i;
    String state="0"; String value="On";   
    if ((IsEM)) 
        {
        if (!((InputStates[i])))
            {state="1";value="Off";} 
        else
            {state="0";value="On";}
    
        }
    else
        {
        if ((!InputStates[i]))
            {state="0";value="On";} 
        else
            {state="1";value="Off";}
        }
    
    body+="    <Item>"+eol+
    "        <Name>"+InputNames[i]+"</Name>"+eol+
            
    "        <No>"+String.valueOf(i+1)+"</No>"+eol+
    "        <State>"+state+"</State>"+eol+
    "        <Value>"+value+"</Value>"+eol;
    if ((IsEM) ) 
        {

            
            
        if (!((AlarmSensorsActive[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}
        body+="        <ActiveState>"+state+"</ActiveState>"+eol+
        "        <ActiveValue>"+value+"</ActiveValue>"+eol;
            
            
            
        if (!((AlarmSensorsWarning[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}
        body+="        <WarningState>"+state+"</WarningState>"+eol+
        "        <WarningValue>"+value+"</WarningValue>"+eol;
        
        //MonitoringSensorsWarning
        
        body+="        <MonitoringState>"+state+"</MonitoringState>"+eol+
        "        <MonitoringValue>"+value+"</MonitoringValue>"+eol;
        
        
       if (!((AlarmSensorsAlarm[i])))
            {state="0";value="Off";} 
        else
            {state="1";value="On";}
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
    //if (InputInverts[i]) inversion="Inverted";
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
XML=header+footer;
}
}

    
 
};    

    }

