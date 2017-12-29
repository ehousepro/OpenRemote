package ehouse4openremote;


import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;



/**
 *
 * Non blocking TCP server (multi thread) for receiving statuses from eHouse system

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
 * eHouse protocol for Open Remote
 * query command via tcp/ip ascii string hex representation of value eg. 00fe0102 case insensitive
 * aabbccdd    - query command for eHouse Java Server
 * aa - eHouse device high address 00-FF
 * bb - ehouse device low address 00-FF
 * cc - sensor query code for object in Open remote (***) - signal type
 * dd - Item Number /signal nr (i / o / adc eg.)
 * 
 * server connection is kept for each sensor
 * return state of signal capture to eHouse java sever cache
 * report state depending on signal type on/off for digital inputs, outputs,
 * string representation for analog and numerical values 
 * 
 * in case of changing signal state of controller automatically return state to socket connection while connection is active
 * 
 * some signal types can be change directly from socket sending command on, off, toggle,0, 1 or number for numeric signal (dimmers, programs)
 * 
 * 
 * 
 * aabbccddeeffgghhiijj - sending direct events to controller via eHouse Java Server
 * aa - eHouse device high address 00-FF
 * bb - ehouse device low address 00-FF
 * cc - event code 00-ff
 * dd - arg1 for event 00-ff
 * ee - arg2 for event 00-ff
 * ff - arg3 for event 00-ff
 * gg - arg4 for event 00-ff
 * hh - arg5 for event 00-ff
 * ii - arg6 for event 00-ff
 * jj - arg7 for event 00-ff
 * 
 */






public class TCPStatusServerHandler extends IoHandlerAdapter {
    final static int EVENT_SETLIGHT=4;              //set RGB dimmers light level  for chanel //hh ll nr rr gg bb 00 00 00 00 (single channels * 3)
    final static int EVENT_CHANGE_OUTPUT=0x21;      //set RGB dimmers light level  for chanel //hh ll nr rr gg bb 00 00 00 00 (single channels * 3)
    /*
     * Sensor query code (***)  commands
     * 
     */
    final static int SIGNAL_OUTPUT_STATE=1;         //query output state - events accepted 0,1,2,on,off,toggle while socket is opened
    final static int SIGNAL_INPUT_STATE=2;          //query input state -no events
    final static int SIGNAL_DIMMER_LEVEL=3;         //dimmer level - events accepted change dimmer value 0-255
    final static int SIGNAL_CURRENT_PROGRAM=4;      //current program
    final static int SIGNAL_ADC_VALUE=5;            //Analog to digital converter value <0..1023>
    final static int SIGNAL_LM335_VALUE=6;          //LM335 sensor Temperature calculated value
    final static int SIGNAL_VOLTAGE_VALUE=7;        //voltage value
    final static int SIGNAL_PERCENT_VALUE=8;        //percent value in reference to power supply
    final static int SIGNAL_INVPERCENT_VALUE=9;     //inverted percent value 100-x %
    final static int SIGNAL_MCP9700_VALUE=10;       //MCP9700 temperature sensor calculated value
    final static int SIGNAL_MCP9701_VALUE=11;       //MCP9701 temperature sensor calculated value
    
    final static int SIGNAL_WHOLE_STATUS=12;        //whole binary (converted to ascii string) status from controller
    final static int SIGNAL_SEND_XML=13;            //query status RGB dimmers chanel
    final static int SIGNAL_RGB_DIMMER_LEVEL=14;    //query status RGB dimmers chanel
    final static int SIGNAL_OTHER_EVENT=255;        //Other system events
    final static int SIGNAL_ACTIVE_STATE=15;        //query commmanager active state -no events
    final static int SIGNAL_WARNING_STATE=16;       //query commmanager warning state -no events
    final static int SIGNAL_MONITORING_STATE=17;    //query commmanager monitoring state -no events
    final static int SIGNAL_ALARM_STATE=18;         //query commmanager alarm state -no events
    final static int SIGNAL_CURRENT_ZONE=19;        //query for active zone
    final static int SIGNAL_CURRENT_ADC_PROGRAM=20; //query for active adc program
    final static int SIGNAL_LM35_VALUE=21;          //LM35 temperature sensor calculated value
    final static boolean ConfirmEvent=true;
        boolean TransactionError=false;
                         
        int currentconection=0;
        String DirectEventTemplate="";              //template of event for current signal query
	String message;
        int CurrentInstance;
        int State=0;                                //integer state of sensor
        double StateADC=0;                          //double state of sensor
        //static String ORS="\r\n" ;                //End String separator for OR
        static String ORS="";//\r\n" ;              //End String separator for OR
        boolean AutoDisconnect=false;               //Automatical disconnection after sending status to OpenRemote
        final static int CHECK_UPDATES_INTERVAL=50; //Check socket read intervals in milliseconds
        final static int TIME_OUT_SINGLE_READ=1000/CHECK_UPDATES_INTERVAL;  //Complete read socket timeout miliseconds
        final static int INACTIVITY_TIMEOUT=60;     //multiplication of TIME_OUT_SINGLE_READ*CHECK_UPDATES_INTERVAL ;
        int InactivityTimeOut=INACTIVITY_TIMEOUT;   //Inactivity timer for automatic disconnection
        String LogTransaction="";
        
////////////////////////////////////////////////////////////////////////////////////
/**
 * Current server instance log transaction for sorting socket instance
 * @param str - string to append to log for current server instance
 */        
 void l(String str)
{
LogTransaction+=str+ "\t\r\n";
}
///////////////////////////////////////////////////////////////////////////////////        
 /**
  * Append Exception trace to log together with description
  * @param e - Exception
  * @param desc - Description 
  */
void dex(Exception e,String desc)
{
String stt="";
TransactionError=true;    
try
  {
  stt=desc+":\r\n"+   e.getMessage()+"\r\n";
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

        

//////////////////////////////////////////////////////////////////////////////////
/**
 * Submitting event via tcp directly to controller 
 * @param EventName - name of event for logging
 * @param DirectEvent - direct event command to controller
 */
void suEvenet(String EventName,String DirectEvent)                  // 
    {
    Byte[] schedule=new Byte[3];
    schedule[0]=0;
    schedule[1]=0;
    schedule[2]=0;
    ehousecommunication.AddAsciiEvent(DirectEvent);                 //Add event to sending queue
    int adrh=ehousecommunication.GetHexU(DirectEvent,0);
    if ((adrh>0) && (adrh<86))                                      //if adress h (1,86) Old RoomManagers (eHouse1)
                        ehousecommunication.SendWiFi("", 0);        // sends via commmanager or eHouse PC server
    else                                                            //adrh =0 or >85 Ethernet Rooom Manager etc.
        {                                                           //sends directly to dedicated controller
        int adrl=ehousecommunication.GetHexU(DirectEvent,2);
        ehousecommunication.SendWiFi("192.168."+String.valueOf(adrh)+"."+String.valueOf(adrl), 0);
        }
    }
////////////////////////////////////////////////////////////////////////////////        
/**
 * Checking index of eHouse 1 (eHDev) and ethernet ehouse (eHDevTCP) controller from internal cache (
 * @param devh - high address
 * @param devl - low address
 * 
 */
        int CheckEH1Index(int devh,int devl)
        {
        for (int i = 0; i < EhouseTCP.RM_COUNT; i++)  
            {
            if (EhouseTCP.EhDev[i]!=null)
                if (EhouseTCP.EhDev[i].isAddress(devh, devl))
                    {
                        return i;
                    } 
            } 
            return -1;
        }
/**
 * Check index of Ethernet Ehouse controller 
 * @param devh - address high
 * @param devl - address low
 * @return index in Ehou
 */        
        int CheckTCPIndex(int devh,int devl)
        {
            for (int i = 0; i < EhouseTCP.TCP_COUNT; i++)  
            {
                if (EhouseTCP.EhDevTCP[i]!=null)
                    if (EhouseTCP.EhDevTCP[i].isAddress(devh, devl))
                    {
                        return i;
                    }
            }
            return -1;
        }
        
/***
 * Get Ehouse 1 devices state Open remote query
 * @return direct results for relevant query
 */        
        
String GetEhouse1State(int Ehouse1DevIndex, int iNrItem, int SignalType)
        {
        boolean aStateBool=false;                    //Boolean state of sensor

        if (Ehouse1DevIndex<0) return "";            //only for ehouse1 devices otherwise exit
        l("Ehouse 1 Device Index: "+String.valueOf(Ehouse1DevIndex));
        l("NrItem: "+String.valueOf(iNrItem));
        l("SignalType: "+String.valueOf(SignalType));
            
        if (Ehouse1DevIndex>=EhouseTCP.EhDev.length) return "eHouse1 WARNING: ID out of range of known devices";
        if (EhouseTCP.EhDev[Ehouse1DevIndex]==null) return  "eHouse1 WARNING: device unknown (null)";
        double prevstatedouble=StateADC;
            int ind=Ehouse1DevIndex;
            switch (SignalType)    
            {
                case SIGNAL_OUTPUT_STATE: //output state
                {
                l("SIGNAL_OUTPUT: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                 if (iNrItem>=EhouseTCP.EhDev[ind].OutputStates.length) return "-";   
                 try
                    {                  
                    aStateBool=EhouseTCP.EhDev[ind].OutputStates[iNrItem];
                    }
                 catch (Exception e)
                    { 
                     dex( e,"Error eHouse1 output");
                    }
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrH)
                                        +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrL)
                                        +"01"
                                        +ehousecommunication.ConvertAsciHex(iNrItem+1)
                                        +"%%0000000000";

                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }
                case SIGNAL_INPUT_STATE:  //input state
                    {
                    l("SIGNAL_INPUT: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDev[ind].InputStates.length) return "-";
                    try 
                        {
                        aStateBool=EhouseTCP.EhDev[ind].InputStates[iNrItem];
                        }
                    catch (Exception e)
                        {
                        dex( e,"Error eHouse1 input");
                        }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }
                case SIGNAL_DIMMER_LEVEL:     //dimmer level
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].Dimmer.length) return "-";
                    State=EhouseTCP.EhDev[ind].Dimmer[iNrItem];
                    return String.valueOf(State)+ORS;
                    }
                case SIGNAL_RGB_DIMMER_LEVEL:     //rgb dimmer level
                    {
                    
                    if (iNrItem*3>=EhouseTCP.EhDev[ind].Dimmer.length) return "-";
                    String StateStr=  String.valueOf(EhouseTCP.EhDev[ind].Dimmer[iNrItem*3])+" "+
                            String.valueOf(EhouseTCP.EhDev[ind].Dimmer[iNrItem*3+1])+" "+
                            String.valueOf(EhouseTCP.EhDev[ind].Dimmer[iNrItem*3+2]);
                    l("SIGNAL_RGB_DIMMER :"+ StateStr);
                    //if (prevstatestr!=StateStr) Changed=true;                
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrH)
                                    +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrL)
                                    +"04"  //set rgb dimmers levels                     EVENT_SETLIGHT
                                    +ehousecommunication.ConvertAsciHex(iNrItem+1) //channels multiplexed by 3
                                    +"%%%%%%000000";
                    return StateStr+ORS;
                    }                    
                                
                case SIGNAL_CURRENT_PROGRAM:    //current program
                    {
                    State=EhouseTCP.EhDev[ind].CurrentProgram;
                    if (!EhouseTCP.EhDev[ind].IsHM)
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrH)
                                        +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrL)
                                        +"02"
                                        +ehousecommunication.ConvertAsciHex(iNrItem+1)
                                        +"%%0000000000";
                    else 
                        DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrH)
                                            +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDev[ind].DevAdrL)
                                            +"AA"
                                            +ehousecommunication.ConvertAsciHex(iNrItem)
                                            +"%%0000000000";
                    l("SIGNAL_CURRENT :"+String.valueOf(State));
                    return String.valueOf(State)+ORS;
                    }
                case SIGNAL_ADC_VALUE:   //ADC value 0..1023
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].SensorABSValues.length) return "-";
                    StateADC=EhouseTCP.EhDev[ind].SensorABSValues[iNrItem];
                    l("SIGNAL_ADC :"+String.valueOf(StateADC));
                    return String.valueOf(StateADC)+ORS;
                    }

                case SIGNAL_LM335_VALUE:   //ADC LM335 Value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].SensorTemps.length) return "-";
                    StateADC=EhouseTCP.EhDev[ind].SensorTemps[iNrItem];
                    l("SIGNAL_LM335 :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_VOLTAGE_VALUE:  //ADC Voltage value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].SensorVolts.length) return "-";
                    StateADC=EhouseTCP.EhDev[ind].SensorVolts[iNrItem];
                    l("SIGNAL_VOLTAGE:"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_PERCENT_VALUE:  //ADC percent value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].SensorPercents.length) return "-";
                    StateADC=EhouseTCP.EhDev[ind].SensorPercents[iNrItem];
                    l("SIGNAL_PERCENT :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_INVPERCENT_VALUE: //ADC InvPercent value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDev[ind].SensorLights.length) return "-";
                    StateADC=EhouseTCP.EhDev[ind].SensorLights[iNrItem];
                    l("SIGNAL_INVPERVENT :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
    case SIGNAL_WHOLE_STATUS:       //whole status
                    {
                  
                    l("SIGNAL_WHOLE_STATUS");                   
                    return ehousecommunication.hib(EhouseTCP.EhDev[ind].CurrentStatus,EhouseTCP.EhDev[ind].STATUS_SIZE)+ORS;
                    }                    
    case SIGNAL_SEND_XML:
                    {
                    l("SIGNAL_SEND_XML");                   
                    return  EhouseTCP.EhDev[ind].XML+ORS;
                    }
    /*case SIGNAL_ACTIVE_STATE:  //input state
                {//TransactionError=true;
                    l("SIGNAL_ACTIVE_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                if (iNrItem>=EhouseTCP.EhDev[ind].AlarmSensorsActive.length) return "-";
                try 
                {
                    
                aStateBool=EhouseTCP.EhDev[ind].AlarmSensorsActive[iNrItem];
                
                }
                catch (Exception e)
                 {
                     dex( e,"Error active state");
                 
                 }
                if (prevstatebool!=aStateBool) Changed=true;
                if (aStateBool) return "on"+ORS;
                else return "off"+ORS;
                }    
      */  
/*case SIGNAL_WARNING_STATE:  //input state
                {
    //TransactionError=true;
                    l("SIGNAL_WARNING_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                if (iNrItem>=EhouseTCP.EhDev[ind].AlarmSensorsWarning.length) return "-";
                try 
                {
                    
                aStateBool=EhouseTCP.EhDev[ind].AlarmSensorsWarning[iNrItem];
                
                }
                catch (Exception e)
                 {
                     dex( e,"Error warning state");
                 
                 }
                if (prevstatebool!=aStateBool) Changed=true;
                if (aStateBool) return "on"+ORS;
                else return "off"+ORS;
                }        */
/*case SIGNAL_MONITORING_STATE:  //input state
                {
                //TransactionError=true;
                    l("SIGNAL_MONITORING_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                if (iNrItem>=EhouseTCP.EhDev[ind].AlarmSensorsMonitoring.length) return "-";
                try 
                {
                    
                aStateBool=EhouseTCP.EhDev[ind].AlarmSensorsMonitoring[iNrItem];
                
                }
                catch (Exception e)
                 {
                     dex( e,"Error monitoring state");
                 
                 }
                if (prevstatebool!=aStateBool) Changed=true;
                if (aStateBool) return "on"+ORS;
                else return "off"+ORS;
                }        
  */           
/*case SIGNAL_ALARM_STATE:  //input state
                {
                //TransactionError=true;
                    l("SIGNAL_ALARM_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                if (iNrItem>=EhouseTCP.EhDev[ind].AlarmSensorsAlarm.length) return "-";
                try 
                {
                    
                aStateBool=EhouseTCP.EhDev[ind].AlarmSensorsAlarm[iNrItem];
                
                }
                catch (Exception e)
                 {
                     dex( e,"Error alarm state");
                 
                 }
                if (prevstatebool!=aStateBool) Changed=true;
                if (aStateBool) return "on"+ORS;
                else return "off"+ORS;
                }        
  */      
                
    //final static int SIGNAL_CURRENT_ZONE=19;            //query for active zone
   
            }
   TransactionError=true; 
   l("UNKNOWN_SIGNAL :"+String.valueOf(SignalType));      
    
    return "-"+ORS;    
        }        
        
        

/////////////////////////////////////////////////////////////////////////////////        
        /**
         * Get Ethernet eHouse devices states and values for open remote and assigning EventTemplate
         * @return String = state 
 * 
 * @param EthernetDevIndex - index in EhouseTCP.EhDevTCP matrix
 * @param iNrItem - number of item 
 * @param SignalType - type of signal
 * @return 
 */
String GetEthernetEhouseState(int EthernetDevIndex, int iNrItem, int SignalType)
        {
        boolean aStateBool=false;                    //Boolean state of sensor
        if (EthernetDevIndex<0) return "";      //only for ethernet ehouse devices otherwise exit
        l("Ethernet Device Index: "+String.valueOf(EthernetDevIndex));
        l("NrItem: "+String.valueOf(iNrItem));
        l("SignalType: "+String.valueOf(SignalType));            
        if (EthernetDevIndex>=EhouseTCP.TCP_COUNT) return "";
        if (EhouseTCP.EhDevTCP[EthernetDevIndex]==null) return "";
            int ind=EthernetDevIndex;
            switch (SignalType)    
            {
                case SIGNAL_OUTPUT_STATE: //output state
                    {
                    l("SIGNAL_OUTPUT: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].OutputStates.length) return "-";   
                    try{

                    aStateBool=EhouseTCP.EhDevTCP[ind].OutputStates[iNrItem];
                    }
                    catch (Exception e)
                    { 
                        dex( e,"Error output");
                    }
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrH)
                                        +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrL)
                                        +"21"
                                        +ehousecommunication.ConvertAsciHex(iNrItem)
                                        +"%%0000000000";

                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }
                case SIGNAL_INPUT_STATE:  //input state
                    {
                    l("SIGNAL_INPUT: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].InputStates.length) return "-";
                    try 
                        {
                        aStateBool=EhouseTCP.EhDevTCP[ind].InputStates[iNrItem];
                        }
                    catch (Exception e)
                        {
                        dex( e,"Error input");
                        }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }
                case SIGNAL_DIMMER_LEVEL:     //dimmer level
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].Dimmer.length) return "-";
                    State=EhouseTCP.EhDevTCP[ind].Dimmer[iNrItem];
                    return String.valueOf(State)+ORS;
                    }
                case SIGNAL_RGB_DIMMER_LEVEL:     //RGB dimmer level
                    {                    
                    if (iNrItem*3>=EhouseTCP.EhDevTCP[ind].Dimmer.length) return "-";
                    String StateStr=  String.valueOf(EhouseTCP.EhDevTCP[ind].Dimmer[iNrItem*3])+" "+
                            String.valueOf(EhouseTCP.EhDevTCP[ind].Dimmer[iNrItem*3+1])+" "+
                            String.valueOf(EhouseTCP.EhDevTCP[ind].Dimmer[iNrItem*3+2]);
                    l("SIGNAL_RGB_DIMMER :"+ StateStr);
                    //if (prevstatestr!=StateStr) Changed=true;                
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrH)
                                    +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrL)
                                    +"04"  //set rgb dimmers levels                     EVENT_SETLIGHT
                                    +ehousecommunication.ConvertAsciHex(iNrItem) //channels multiplexed by 3
                                    +"%%%%%%000000";
                    return StateStr+ORS;
                    }                                                    
                case SIGNAL_CURRENT_PROGRAM:    //current program
                    {
                    State=EhouseTCP.EhDevTCP[ind].CurrentProgram;
                    if (!EhouseTCP.EhDevTCP[ind].IsEHM)
                    DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrH)
                                        +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrL)
                                        +"02"
                                        +ehousecommunication.ConvertAsciHex(iNrItem)
                                        +"%%0000000000";
                    else 
                        DirectEventTemplate=ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrH)
                                    +ehousecommunication.ConvertAsciHex(EhouseTCP.EhDevTCP[ind].DevAdrL)
                                    +"AA"
                                    +ehousecommunication.ConvertAsciHex(iNrItem)
                                    +"%%0000000000";
                    l("SIGNAL_CURRENT :"+String.valueOf(State));
                    return String.valueOf(State)+ORS;
                    }
                case SIGNAL_ADC_VALUE:   //ADC value 0..1023
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorABSValues.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorABSValues[iNrItem];
                    l("SIGNAL_ADC :"+String.valueOf(StateADC));
                    return String.valueOf(StateADC)+ORS;
                    }

                case SIGNAL_LM335_VALUE:   //ADC LM335 Value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorTemps.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorTemps[iNrItem];
                    l("SIGNAL_LM335 :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_VOLTAGE_VALUE:  //ADC Voltage value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorVolts.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorVolts[iNrItem];
                    l("SIGNAL_VOLTAGE:"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_PERCENT_VALUE:  //ADC percent value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorPercents.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorPercents[iNrItem];
                    l("SIGNAL_PERCENT :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
                case SIGNAL_INVPERCENT_VALUE: //ADC InvPercent value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorLights.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorLights[iNrItem];
                    l("SIGNAL_INVPERVENT :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
            case SIGNAL_MCP9700_VALUE:  //mcp9700 value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorTempsMCP9700.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorTempsMCP9700[iNrItem];
                    l("SIGNAL_MCP9700 :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
    
    
            case SIGNAL_MCP9701_VALUE:       //mcp9701 value
                    {
                    //TransactionError=true;
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].SensorTempsMCP9701.length) return "-";
                    StateADC=EhouseTCP.EhDevTCP[ind].SensorTempsMCP9701[iNrItem];
                    l("SIGNAL_MCP9701 :"+String.valueOf(StateADC));                   
                    return String.valueOf(StateADC)+ORS;
                    }
            case SIGNAL_WHOLE_STATUS:       //whole status
                    {
                  
                    l("SIGNAL_WHOLE_STATUS");                   
                    return ehousecommunication.hib(EhouseTCP.EhDevTCP[ind].CurrentStatus, EhouseTCP.EhDev[ind].STATUS_SIZE)+ORS;
                    }                    
            case SIGNAL_SEND_XML:
                    {
                    l("SIGNAL_SEND_XML");                   
                    return  EhouseTCP.EhDevTCP[ind].XML+ORS;
                    }
            case SIGNAL_ACTIVE_STATE:  //input state
                    {
                    //TransactionError=true;
                        l("SIGNAL_ACTIVE_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].AlarmSensorsActive.length) return "-";
                    try 
                    {

                    aStateBool=EhouseTCP.EhDevTCP[ind].AlarmSensorsActive[iNrItem];

                    }
                    catch (Exception e)
                    {
                        dex( e,"Error active state");

                    }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }    

            case SIGNAL_WARNING_STATE:  //input state
                    {
                    //TransactionError=true;
                    l("SIGNAL_WARNING_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].AlarmSensorsWarning.length) return "-";
                    try 
                    {

                    aStateBool=EhouseTCP.EhDevTCP[ind].AlarmSensorsWarning[iNrItem];

                    }
                    catch (Exception e)
                    {
                        dex( e,"Error warning state");

                    }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }        
            case SIGNAL_MONITORING_STATE:  //input state
                    {
                    //TransactionError=true;
                    l("SIGNAL_MONITORING_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].AlarmSensorsMonitoring.length) return "-";
                    try 
                    {

                    aStateBool=EhouseTCP.EhDevTCP[ind].AlarmSensorsMonitoring[iNrItem];

                    }
                    catch (Exception e)
                    {
                        dex( e,"Error monitoring state");

                    }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }        
             
            case SIGNAL_ALARM_STATE:  //input state
                    {
                    //TransactionError=true;
                        l("SIGNAL_ALARM_STATE: "+String.valueOf(ind)+" >> "+String.valueOf(iNrItem));
                    if (iNrItem>=EhouseTCP.EhDevTCP[ind].AlarmSensorsAlarm.length) return "-";
                    try 
                    {

                    aStateBool=EhouseTCP.EhDevTCP[ind].AlarmSensorsAlarm[iNrItem];

                    }
                    catch (Exception e)
                    {
                        dex( e,"Error alarm state");

                    }
                    if (aStateBool) return "on"+ORS;
                    else return "off"+ORS;
                    }        


        //final static int SIGNAL_CURRENT_ZONE=19;            //query for active zone

            }
   TransactionError=true; 
   l("UNKNOWN_SIGNAL :"+String.valueOf(SignalType));      
    
    return "-"+ORS;    
        }        

public void writeMsgString(IoSession session, String strMessage) throws CharacterCodingException
{
    IoBuffer buffer = IoBuffer.allocate(25);
    buffer.setAutoExpand(true);
    buffer.putString(strMessage, Charset.forName("UTF-8").newEncoder());
    buffer.flip();
    session.write(buffer);
    buffer.free();
} 
//////////////////////////////////////////////////////////////////////////////////////////
@Override 
public void messageReceived(IoSession session, Object message) throws Exception             //Server connection 
	{

            int aNrItem=-1;                              //Signal Number of Item
            int aSignalType=-1;                          //Signal type for query from open remote    
            TransactionError=ehousecommunication.DebugServers;
            DirectEventTemplate="";
            State=0;
            StateADC=0;
            InactivityTimeOut=INACTIVITY_TIMEOUT;
            LogTransaction="";
            l("-----------------------------------");
            l("[TCP] Request received");
            State=0;
            StateADC=0;

            String msgOR = "";
            IoBuffer buf = (IoBuffer) message;

            while (buf.hasRemaining()) {
                msgOR = msgOR + (char) buf.get();
            }
            if (msgOR.length()>0)
            {
                //Thread.sleep(50);
                msgOR=msgOR.trim().replaceAll(" ","");          //ignore spaces for easier implementation in open remote 
                msgOR=msgOR.toUpperCase();
                int devadrh=ehousecommunication.GetHexU(msgOR,0);         //decode hex address high
                int devadrl=ehousecommunication.GetHexU(msgOR, 2);        //decode hex address low         
                l("Message converted: "+msgOR+" Size: "+String.valueOf(msgOR.length()));
                aSignalType=ehousecommunication.GetHexU(msgOR,4);          //get signal type for query
                aNrItem=ehousecommunication.GetHexU(msgOR, 6)-1;           //get Nr of this signal type

                if ((msgOR.length()>7) && (msgOR.length()<20))   //query command for checking devices signal states
                {

                                                           //get indexes of contollers from cache

                            //System.out.println("ORS Message : "+message);
                            String resEth=GetEthernetEhouseState(CheckTCPIndex(devadrh,devadrl), aNrItem, aSignalType);                     //result string for Ethernet eHouse controller current signal State
                            String resEhouse1=GetEhouse1State(CheckEH1Index(devadrh,devadrl), aNrItem, aSignalType);                         //result string for eHouse1 controller current signal State
                            TransactionError = true;
                            l("Eth: "+resEth+" ; Eh1: "+resEhouse1 );
                            try {
                                if (resEth.length()>0) writeMsgString(session,resEth);       
                                if (resEhouse1.length()>0) writeMsgString(session, resEhouse1);
                                } catch (Exception e){
                                dex(e, "[TCP] ERROR while getting device state");
                                 }                          
                            
                            //if (AutoDisconnect) break;
                            TransactionError = false;
                            }
                    else 
                            if (msgOR.length()==20)   //      Direct event to submit
                            {   
                                msgOR=msgOR.toUpperCase();
                                suEvenet(msgOR,msgOR);      //submit direct event to ehouse controllers
                                if (ConfirmEvent)
                                    {
                                    //outs.write("+".getBytes());       
                                    //outs.flush();
                                    }
                                //if (AutoDisconnect) break;
                                }
                            else //other issues
                                if (msgOR.length()>0)         //received other data => directly change state of signal
                                {
                                String Event="";
                                if (aSignalType==SIGNAL_OUTPUT_STATE)        //for outputs
                                    {
                                    boolean enventtorun=false;    
                                    if (msgOR.toLowerCase().equals("on")) 

                                                {
                                                    Event=DirectEventTemplate.replaceAll("%%", "01");   //get template of event for current signal
                                                    enventtorun=true;
                                                }
                                    if (msgOR.toLowerCase().equals("off")) {Event=DirectEventTemplate.replaceAll("%%", "00");enventtorun=true;}
                                    if (msgOR.toLowerCase().equals("toggle")) {Event=DirectEventTemplate.replaceAll("%%", "02");enventtorun=true;}
                                    if (msgOR.toLowerCase().equals("sw")) {Event=DirectEventTemplate.replaceAll("%%", "02");enventtorun=true;}
                                    if (msgOR.toLowerCase().equals("1")) {Event=DirectEventTemplate.replaceAll("%%", "01");enventtorun=true;}
                                    if (msgOR.toLowerCase().equals("0")) {Event=DirectEventTemplate.replaceAll("%%", "00");enventtorun=true;}
                                    if (msgOR.toLowerCase().equals("2")) {Event=DirectEventTemplate.replaceAll("%%", "02");enventtorun=true;}
                                    if (enventtorun)                        //if match items
                                        {
                                        suEvenet(Event,Event);              //submit direct event
                                        }
                                    //if (AutoDisconnect) break;
                                    }
                                }
                        else
                                if (msgOR.length()>0)      
                                { 
                                l("message other size")    ;
                                }
                                else
                                if (msgOR.length()==0)                      // nothing received after timeout
                                {
                                    l("Read timeout") ;
                                String resEth=GetEthernetEhouseState(CheckTCPIndex(devadrh,devadrl), aNrItem, aSignalType);     //checking if state was changed
                                String resEhouse1=GetEhouse1State(CheckEH1Index(devadrh,devadrl), aNrItem, aSignalType);

                                if (resEth.length()>0)    {
                                    writeMsgString(session,resEth);
                                }
                                if (resEhouse1.length()>0)    {
                                    writeMsgString(session,resEhouse1);
                                }    

                                //if (AutoDisconnect) break;
                       }
            }
           if (ehousecommunication.DebugServers|TransactionError) ehousecommunication.l(LogTransaction);
           session.close(false);
     }
	
  

   //private final static Logger LOGGER = LoggerFactory.getLogger(TCPStatusServerHandler.class);
    
    @Override
    public void sessionCreated(IoSession session) {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        // We're going to use SSL negotiation notification.
        //session.setAttribute(SslFilter.USE_NOTIFICATION);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        l("[TCP] Session CLOSED");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        l("[TCP] Session OPENED");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        l("[TCP] Session *** IDLE #" + session.getIdleCount(IdleStatus.BOTH_IDLE) + " ***");
          if (status == IdleStatus.READER_IDLE) session.close(true);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        l("[TCP] problem: ");
        cause.printStackTrace();
        session.close(true);
    }
 }
