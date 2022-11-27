import java.io.*;
import java.net.Socket;

public class User implements Runnable{

    private Socket user;
    private BufferedReader in;
    private PrintWriter out;
    private boolean fin;

    @Override
    public void run() {

        try{
            user = new Socket("127.0.0.1", 6666);
                out = new PrintWriter(user.getOutputStream(),true);
                    in = new BufferedReader(new InputStreamReader(user.getInputStream()));
                inHandler handler = new inHandler();
                //single thread, one handler
                Thread t = new Thread(handler);
            t.start();
                String inMessage;
            while((inMessage = in.readLine()) != null){
                System.out.println(inMessage);
            }
        }catch(IOException e){
            //exit();
        }
    }
    public void exit(){
        fin = true;
        try{
            in.close();
            out.close();
            if(!user.isClosed()){
                user.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    class inHandler implements Runnable{
        @Override
        public void run() {
            try{
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while(!fin){
                String message = inReader.readLine();
                if(message.equals("/exit")){
                        inReader.close();
                        exit();
                    }else{
                    out.println(message); //send to server
                    }
                }
            }catch(IOException e){
                exit();
            }
        }
    }

    public static void main(String[] args){

        User user1 = new User();
        user1.run();


    }
}
