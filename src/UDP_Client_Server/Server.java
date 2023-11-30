package UDP_Client_Server;

import java.io.IOException;
import java.util.Scanner;

import static UDP_Client_Server.util.Util.*;

public class Server {

    private static class ClientInstance{

        public Connection connection;
        public int port, player_type;
        public ClientInstance(int port, int player_type){
            this.port=port;
            this.player_type=player_type;
            connection = new Connection(port);
        }

    }

    private ClientInstance[] clients;

    private class MainLoop extends Thread{
        @Override
        public void run() {
            while(true){
                StringBuilder response = new StringBuilder();
                Thread waiters[] = new Thread[N_CLIENTS];
                System.out.println("Loop step");
                for(int i=0;i<N_CLIENTS;++i) {
                    System.out.println("Initializing waiter "+i);
                    int num_client = i;
                    waiters[i] = new Thread(() -> {
                        System.out.println("Wait for client " + num_client);
                        while (clients[num_client].connection.getState() == ConnectionState.WAIT_TO_READ) {} // wait
                        String msg = clients[num_client].connection.getLastMessage();
                        System.out.println("Client "+num_client+" said: "+msg);
                        response.append(msg);
                    });
                    waiters[i].start();
                }
                for(int i=0;i<N_CLIENTS;++i){
                    try {
                        waiters[i].join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                System.out.println("All inputs collected. Response: "+response);
                for(ClientInstance client : clients){
                    try {
                        client.connection.send(response.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public Server() throws IOException {
        clients = new ClientInstance[N_CLIENTS];
        for(int i=0;i<N_CLIENTS;++i){
            clients[i] = new ClientInstance(DEFAULT_PORT - i, i);
        }
        MainLoop mainLoop = new MainLoop();
        mainLoop.start();
    }
    public static void main(String[] args) throws IOException{
        Thread t= new Thread(){
            @Override
            public void run(){
                while(true){
                    Scanner sc = new Scanner(System.in);
                    if(sc.hasNextLine()){
                        if(sc.nextLine().equals("exit")){
                            System.exit(0);
                        }
                    }
                }
            }
        };
        t.start();
        Server server = new Server();
    }
}
