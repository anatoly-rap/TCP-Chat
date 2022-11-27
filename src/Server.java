import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

        private ArrayList<ConHandler> connection;
        private ServerSocket svr;
        private boolean done;

        public Server(){
            connection = new ArrayList<>();
        }

    @Override
    public void run(){
        try{
            while(!done){

            svr = new ServerSocket(6666);
            ExecutorService pool = Executors.newCachedThreadPool();
            Socket clientSocket = svr.accept();
            ConHandler handle = new ConHandler(clientSocket);
            connection.add(handle);
            pool.execute(handle);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public class ConHandler implements Runnable{

            private Socket clientSocket;
            private String clientName;
            private BufferedReader in; //get stream from socket(client)
            private PrintWriter out;    //write to client, out

            //instance, so can handle multiple clients
            public ConHandler(Socket clientSocket){
                this.clientSocket = clientSocket;
            }

        @Override
        public void run() {
            try{
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println("Enter a nickname....");
                do{
                    clientName = in.readLine();
                    if (checkName(clientName)){
                        break;
                    }else{
                        System.out.println("Username must contain at least one integer");
                    }
                }while(true);
                    System.out.println(clientName + " connected");
                    broadcastMsg(clientName + " joined the chat");
                    String message;
                    while((message = in.readLine()) != null){
                        if(message.startsWith("/name")){
                            String[] mesSplit = message.split(" ",2);
                            if(mesSplit.length == 2 && checkName(mesSplit[1])){
                                broadcastMsg(clientName + " renamed to " + mesSplit[1]);
                                System.out.println(clientName + "renamed to " + mesSplit[1]);
                                clientName = mesSplit[1];
                                out.println("sucessfully changed name to " + clientName);
                            }
                        }else if(message.startsWith("/users")){
                            clientList();
                        }else if(message.startsWith("/quit")){
                            broadcastMsg(clientName + " disconnected");
                            closeClient();
                        }else{
                            broadcastMsg(clientName + ": " + message);
                        }
                    }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
        public void broadcastMsg(String message){
            for(ConHandler ch: connection){
                if(ch != null){
                    ch.sendMsg(message);

                }
            }
        }
        public void shutDown() throws IOException {
            done = true;
            if(!svr.isClosed()){
                svr.close();
            }
            for(ConHandler ch: connection){
                ch.closeClient();
            }
        }
        public void closeClient() throws IOException{
                in.close();
                out.close();
                if(!clientSocket.isClosed()){
                    clientSocket.close();
                }
        }

        public void sendMsg(String message){
            out.println(message);
        }
        public void clientList(){
                for(ConHandler ch: connection){
                    broadcastMsg("Current Clients: " + ch.clientName);
                }
        }

    }

public boolean checkName(String clientName){
    char[] chars = clientName.toCharArray();
    StringBuilder sb = new StringBuilder();
    for(char c : chars){
        if(Character.isDigit(c)){
            sb.append(c);
        }
    }
    return sb.length() > 0;
    }

    public static void main(String[] args) {

            Server sv1 = new Server();
            sv1.run();

    }
}


