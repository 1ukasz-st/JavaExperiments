package UDP_Client_Server;

import UDP_Client_Server.util.KeyBoard;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

import static UDP_Client_Server.util.Util.*;

public class Client{

    private ArrayList<GameUnit> units = new ArrayList<>();
    private KeyBoard kb = new KeyBoard();
    private int playerType = -1;
    private Connection connection;
    private void scaleUnits(){
        for(GameUnit unit : units){
            unit.x = (int)(SCR_W*(double)(unit.x)/100.0);
            unit.y = (int)(SCR_H*(double)(unit.y)/100.0);
        }
    }

    public Client() throws IOException {

        connection = new Connection(DEFAULT_PORT,InetAddress.getByName(SERVER_IP));

        Scanner sc = new Scanner(System.in);
        if (sc.hasNextLine()) {
            connection.send(sc.nextLine());
        }
        while(true){
            if(connection.getState() == ConnectionState.WAIT_TO_SEND) {
                System.out.println("Response from server: "+connection.getLastMessage());
                if (sc.hasNextLine()) {
                    connection.send(sc.nextLine());
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        new Client();
    }
}
