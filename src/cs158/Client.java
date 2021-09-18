package cs158;
import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;

public class Client {
    public static boolean inChat = true, display;
    public static DatagramSocket socket;
    public static Gson gson = new Gson();
    public static void main(String[] args) throws IOException{
        // establish udp socket
        socket = new DatagramSocket(5503);
        //begin scanning for login input
        Scanner scanner = new Scanner(System.in);
        //prompt user for username
        System.out.println("Enter username: ");
        // establish data to send
        String username = scanner.nextLine();
        // begin concurrently receiving requests
        ReceiveThread rt = new ReceiveThread();
        rt.start();
        // generate login request
        Message request = new Message(0,username, "");
        byte[] data = gson.toJson(request).getBytes();
        // construct a packet for the login request
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 5502);
        // send the packet
        socket.send(packet);
        while(inChat) {
            // prompt user for message
            if (display) { // ensure that server response display doesn't conflict with user input
                System.out.println("Enter message: ");
                // scan input
                request.setContent(scanner.nextLine());
                // exit case check
                if (request.getContent().compareTo("exit") == 0) {
                    request.setType(2);
                } else {
                    request.setType(1);
                }
                // construct a new packet to send
                data = new Gson().toJson(request).getBytes();
                packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 5502);
                // send the packet
                socket.send(packet);
                display = false;
            }
        }
        socket.close(); // close socket to ensure client has exited
    }
    public static class ReceiveThread extends Thread{
        @Override
        public void run(){
            try {
                while(inChat) {
                    display = false;
                    // prepare buffer and collect server response
                    DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000);
                    socket.receive(buffer);
                    //convert byte stream into Message object
                    String data = new String(buffer.getData());
                    data = data.substring(data.indexOf('{'), data.indexOf('}')+1);
                    Message response = gson.fromJson(data, Message.class);
                    // output based on type of response
                    if (response.getType() <= 1) { // 0,1 case = print content to System.out
                        System.out.println(response.getUsername() + ": " + response.getContent());
                    } else if (response.getType() == 2) { // 2 case = tell user they have exited the server
                        System.out.println(response.getUsername() + " has left the chat!");
                        inChat = false; // break while loop
                        this.interrupt(); // stop the receiving thread
                    }
                    display = true;
                }
            } catch (Exception e) {
                socket.close();
                e.printStackTrace();
            }
        }
    }
}
