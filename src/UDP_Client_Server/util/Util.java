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
    public static final String SERVER_IP = "167.99.253.72";
    public static final int ROCK = 0, SCISSORS = 1, PAPER = 2, UNIT_W = 64, UNIT_H = 105, UNITS_PER_PLAYER = 7;
    public static final String NO_DATA = "NOTHING";
    public static final int MAX_FRAME_TIME = 16;

    public static final int SCR_W = 800, SCR_H = 600;
    private static final String EXTRA_DATA_MARKER = "#";

    public static class GameUnit {
        public int x , y , type;

        public GameUnit(int x, int y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

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

    public static void sendBackUnits(ArrayList<GameUnit> toSend, String extraData, DatagramSocket socket, InetAddress ip, int port) throws IOException {
        StringBuilder message = null;
        if(extraData.isEmpty()){
            if(toSend.isEmpty()){
                socket.send(Util.createDatagram(Util.NO_DATA,ip,port));
                return;
            }
            message = new StringBuilder();
        }else{
            message = new StringBuilder(extraData + "#");
        }
        for(GameUnit unit : toSend){
            message.append("{").append(unit.x).append(",").append(unit.y).append(",").append(unit.type).append("};");
        }
        message = new StringBuilder(message.substring(0, message.length() - 1));
        DatagramPacket packetToSend = Util.createDatagram(message.toString(),ip,port);
        socket.send(packetToSend);
    }

    public static ArrayList<GameUnit> receiveUnitsInfo(DatagramSocket serverSocket, StringBuilder extraDataContainer) throws IOException {
        ArrayList<GameUnit> units = new ArrayList<>();
        DatagramPacket unitsInfo = createDatagram();
        serverSocket.receive(unitsInfo);
        String data = datagramStr(unitsInfo);
        data = data.substring(0,data.indexOf(0));
        System.out.println(data);
        if(data.equals(NO_DATA)){
            return units;
        }
        if(data.contains(EXTRA_DATA_MARKER)){
            if(extraDataContainer!=null) {
                String extra = data.substring(0, data.indexOf(EXTRA_DATA_MARKER) - 1);
                extraDataContainer.append(extra);
            }
            data = data.substring(data.indexOf(EXTRA_DATA_MARKER)+1);
        }
        String[] dataArray = data.split(";");
        for (String item : dataArray) {
            String[] values = item.replaceAll("[{}]", "").split(",");
            System.out.println(item);
            int x = Integer.parseInt(values[0]);
            int y = Integer.parseInt(values[1]);
            int type = Integer.parseInt(values[2]);
            units.add(new GameUnit(x,y,type));
        }
        return units;
    }

    public static DatagramPacket createDatagram(){
        return new DatagramPacket(new byte[Util.PACKET_SIZE],Util.PACKET_SIZE);
    }
    public static DatagramPacket createDatagram(String what){
        if(what.length()>Util.PACKET_SIZE){
            System.err.println("String too large for packet!");
            return null;
        }
        byte[] bytesToSend = new byte[Util.PACKET_SIZE];
        byte[] bytesUsed = what.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(bytesUsed, 0, bytesToSend, 0, bytesUsed.length);
        return new DatagramPacket(bytesUsed,bytesUsed.length);
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


    public static String datagramStr(DatagramPacket packet){
        return new String(packet.getData(), StandardCharsets.UTF_8);
    }
}
