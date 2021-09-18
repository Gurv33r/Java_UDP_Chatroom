package cs158;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import com.google.gson.Gson;

public class Server{
    public static DatagramSocket socket;
    public static boolean running;
    public static BlockingDeque<String> msgq = new LinkedBlockingDeque<>();
    public static Gson gson = new Gson();

    // abstraction over DatagramSocket.send() to make it simpler to send Messages
    public static void send(InetAddress a, int port, Message m) throws IOException{
        byte[] message = gson.toJson(m).getBytes();
        socket.send(new DatagramPacket(message, message.length, a, port));
    }

    public static void main(String[] args) throws IOException{
        Map<String, String[]> chatroom = new HashMap<>(); // HashMap<Username, [IP, Port]>
        socket = new DatagramSocket(5502); // establish udp socket
        System.out.println("Server is running!");
        running = true;
        // begin receiving requests
        CatchThread ct = new CatchThread();
        ct.start();
        try {
            // message handling starts here
            while (running) {
                // take a message out of the message queue
                String data = msgq.takeFirst();
                String msg = data.split(" ")[0].replaceAll("-", " "), header = data.split(" ")[1]; // msg = json data, header = ip and pot
                Message request = gson.fromJson(msg, Message.class), response;
                if (request.getType() == 0) { // join case
                    // save ip and port
                    String[] info = header.split("@");
                    // display
                    System.out.println(msg + " has been sent from " + info[0] + " @ " + info[1]);
                    info[0] = info[0].substring(1);
                    // store user in chatroom
                    chatroom.put(request.getUsername(), info);
                    // set reponse to a broadcast message from the server saying that <user> has entered the chat and display the lobby
                    response = new Message(0, "Server", request.getUsername() + " has entered the chat. Lobby = " + chatroom.keySet());
                } else if (request.getType() == 2) { // exit case
                    // create response message letting user know they can leave
                    response = new Message(2, request.getUsername(), "ready to leave");
                    // send the message
                    String[] info = chatroom.get(request.getUsername());
                    Server.send(
                            InetAddress.getByName(info[0]),
                            Integer.parseInt(info[1]),
                            response
                    );
                    //remove user from chatroom
                    chatroom.remove(request.getUsername());
                    response.setContent(request.getUsername() + " has left the chat!\nLobby = " + chatroom.keySet());
                    System.out.println(response.getContent());
                    response.setType(1);
                } else { // post case
                    System.out.println(request.getUsername() + " posted " + request.getContent());
                    // create response that relays user's message to everyone else
                    response = request;
                }
                //traverse clients in chat room
                for (String user : chatroom.keySet()) {
                    //send message content to everyone
                    String[] info = chatroom.get(user);
                    Server.send(
                            InetAddress.getByName(info[0]),
                            Integer.parseInt(info[1]),
                            response
                    );
                }
            }
        } catch (Exception e){
            socket.close(); // close the socket
            e.printStackTrace();
        }
    }
    public static class CatchThread extends Thread{
        @Override
        public void run() {
            try{
                while(running) {
                    //create and receive buffer
                    DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000);
                    socket.receive(buffer);
                    //validate data
                    String data = new String(buffer.getData());
                    if (data.contains("{") && data.contains("}")){
                        //sanitize input
                        data = data.substring(data.indexOf("{"), data.indexOf("}")+1);
                        data = data.replaceAll(" ", "-");
                        msgq.put(data + " " + buffer.getAddress() + "@" + buffer.getPort());
                    }
                }
            } catch(Exception e){
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
