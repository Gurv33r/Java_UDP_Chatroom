package cs158;
import java.net.*;
import java.io.*;
import java.util.*;
import com.google.gson.Gson;
public class Main {
    public static boolean inChat = true, display;
    public static DatagramSocket socket;
    public static Gson gson = new Gson();
    public static void main(String[] args) throws IOException{
        socket = new DatagramSocket(5501); // establish udp socket
        Scanner scanner = new Scanner(System.in);
        // DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000);
        // Login
        //prompt user for username
        System.out.println("Enter username: ");
        // establish data to send
        String username = scanner.nextLine();
        ReceiveThread rt = new ReceiveThread();
        rt.start();
        Message request = new Message(0,username, "");
        byte[] data = gson.toJson(request).getBytes();
        // construct a packet from the message
        // datagram packet constructor takes parameters: message byte[], length int, ipAddress string, port int
        DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 5502);
        // send the packet
        socket.send(packet);
        while(inChat) {
            //prompt user for message
            if (display) {
                System.out.println("@" + username + ": ");
                request.setContent(scanner.nextLine());
                if (request.getContent().compareTo("exit") == 0) {
                    request.setType(2);
                } else {
                    request.setType(1);
                }
                data = new Gson().toJson(request).getBytes();
                // construct a packet from the message
                // datagram packet constructor takes parameters: message byte[], length int, ipAddress string, port int
                packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 5502);
                // send the packet
                socket.send(packet);
                display = false;
            }
        }
        socket.close();
    }
    public static class ReceiveThread extends Thread{
        @Override
        public void run(){
            try {
                while(inChat) {
                    display = false;
                    DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000);
                    socket.receive(buffer);
                    String data = new String(buffer.getData());
                    data = data.substring(data.indexOf('{'), data.indexOf('}')+1);
                    Message response = gson.fromJson(data, Message.class);
                    if (response.getType() <= 1) {
                        System.out.println(response.getUsername() + ": " + response.getContent());
                    } else if (response.getType() == 2) {
                        System.out.println(response.getUsername() + " has left the server!");
                        inChat = false;
                    }
                    display = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
