/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package ehouse4openremote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.core.buffer.IoBuffer;

/**
 * The class that will accept and process clients in order to properly
 * track the memory usage.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class UDPMonitor {

    private static final long serialVersionUID = 1L;

    public static final int PORT = 6789;

    public UDPMonitor() throws IOException {

        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.setHandler(new UDPMonitorHandler(this));

        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        //chain.addLast("logger", new LoggingFilter());

        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);


        acceptor.bind(new InetSocketAddress(PORT));
        System.out.println("[startup] Starting UDP status listener on port: " + PORT);
    }
/**
 * Receive udp status
 * @param clientAddr
 * @param update 
 */
    protected void recvUpdate(SocketAddress clientAddr, IoBuffer update) {

        
        //System.out.print("[UDP] Received packet "+ String.valueOf(update.limit())+" bytes. ");
        //String ss = "";
       // EhouseTCP.StatusCurrentSize=update.get(0)&0xff;
        //if (EhouseTCP.StatusCurrentSize<0) EhouseTCP.StatusCurrentSize=256+EhouseTCP.StatusCurrentSize;
        int SizeSize=update.limit();
            //update.get(EhouseTCP.QueryBuff, 0, SizeSize);
        long CalcCheckSum=0;//=update.get(0)&0xff;
        //update.get(EhouseTCP.QueryBuff, 0, SizeSize);
        for (int i=0;i<SizeSize-2;i++)      //calculate checksum from data
            {
            byte ch= update.get(i);
            EhouseTCP.QueryBuff[i] =ch;
            CalcCheckSum+=EhouseTCP.QueryBuff[i]&0xff;
            //ss = ss + String.valueOf(ehousecommunication.ConvertAsciHex(EhouseTCP.QueryBuff[i]));
            }
        long CheckSum;                                  //received checksum
        byte ch=update.get(SizeSize-2);
        CheckSum=ch&0xff;
        CheckSum=CheckSum<<8;
        ch=update.get(SizeSize-1);
        CheckSum+=ch&0xff;
        int Devadrh=EhouseTCP.QueryBuff[1]&0xff;
        int Devadrl=EhouseTCP.QueryBuff[2]&0xff;
        CalcCheckSum&=0xffff;
        
         //System.out.println(ss);
        if (CheckSum==CalcCheckSum)
            {
               System.out.println("[UDP] ("+String.valueOf(Devadrh)+","+String.valueOf(Devadrl)+") - OK");
                //System.out.println("[UDP] Valid checksum " );
               
               EhouseTCP.QueryReceived(CheckSum);    //decode query data and fill devices arrays
                }
        else
            {
                //System.out.println("[UDP] INVALID  checksum !!!!!!" );
               System.out.println("[UDP] ("+String.valueOf(Devadrh)+","+String.valueOf(Devadrl)+") - WARNING: checksum failed, status ignored.");
                }

    }


    protected void addClient(SocketAddress clientAddr) {
//        System.out.println("[UDP] Received packet from: "+String.valueOf(clientAddr));

    }

}