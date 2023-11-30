package UDP_Client_Server.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Util {
    public static final int N_CLIENTS = 3, DEFAULT_PORT = 9999;
    public static final int PACKET_SIZE = 1<<14;
    public static final String SERVER_IP = "ip of server";
    public static final String NO_DATA = "NOTHING";
    public static final int MAX_FRAME_TIME = 16;
    private static final String EXTRA_DATA_MARKER = "#";

    public enum ConnectionState {
        WAIT_TO_SEND(true), WAIT_TO_READ(false);
        private boolean state;
        ConnectionState(boolean state) {
            this.state = state;
        }

    }

    public static class Connection{

        int port;
        DatagramSocket socket;

        public ConnectionState getState() {
            return state;
        }

        private volatile ConnectionState state;
        private InetAddress targetAddress;
        private String currentMessage = Util.NO_DATA;
        private Thread listener = new Thread(){
            public void run(){
                while(true){
                    DatagramPacket packet = createDatagram();
                    try {
                        socket.receive(packet);
                        assert (state == ConnectionState.WAIT_TO_READ);
                        assert (currentMessage.equals(NO_DATA));
                        if(targetAddress == null){
                            targetAddress = packet.getAddress();
                        }else{
                            assert(targetAddress == packet.getAddress());
                        }
                        byte[] raw = packet.getData();
                        StringBuilder sb = new StringBuilder();
                        for(int i=0;i<PACKET_SIZE;++i){
                            if(raw[i]==0){
                                break;
                            }
                            sb.append((char)(raw[i]));
                        }
                        currentMessage = sb.toString();
                        state = ConnectionState.WAIT_TO_SEND;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        public String getLastMessage(){
            String cp = new String(currentMessage);
            currentMessage = NO_DATA;
            return cp;
        }
        public Connection(int default_port, InetAddress targetAddress){
            this.targetAddress = targetAddress;
            this.state = ConnectionState.WAIT_TO_SEND;
            port = default_port;
            while(true){
                try{
                    socket = new DatagramSocket(port);
                    break;
                }catch (Exception exception){
                    --port;
                }
            }
            listener.start();
        }

        public Connection(int default_port){
            this.targetAddress = null;
            this.state = ConnectionState.WAIT_TO_READ;
            port = default_port;
            while(true){
                try{
                    socket = new DatagramSocket(port);
                    break;
                }catch (Exception exception){
                    --port;
                }
            }
            listener.start();
        }

        public void send(String what) throws IOException {
            assert(targetAddress!=null);
            assert(state==ConnectionState.WAIT_TO_SEND);
            state=ConnectionState.WAIT_TO_READ;
            socket.send(createDatagram(what,targetAddress,port));
        }

    }

    public static DatagramPacket createDatagram(){
        return new DatagramPacket(new byte[Util.PACKET_SIZE],Util.PACKET_SIZE);
    }
    public static DatagramPacket createDatagram(String what, InetAddress address, int port){
        if(what.length()>Util.PACKET_SIZE){
            System.err.println("String too large for packet!");
            return null;
        }
        byte[] bytesToSend = new byte[Util.PACKET_SIZE];
        byte[] bytesUsed = what.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytesUsed, 0, bytesToSend, 0, bytesUsed.length);
        return new DatagramPacket(bytesUsed,bytesUsed.length,address,port);
    }
}
