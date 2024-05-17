//import java.io.*;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.InetAddress;
//
//public class Main {
//    public static void main(String[] args) throws IOException {
//        int server_port = 8053;
//        InetAddress server_host = InetAddress.getByName("localhost");
//        System.out.println("Sending to " + server_host + " " + server_port);
//        DatagramSocket socket = new DatagramSocket(server_port);
//
//        byte[] buffer = new byte[512];
//        DatagramPacket pkt = new DatagramPacket(buffer, buffer.length);
//        socket.receive(pkt);
//
//        try {
//
//            DNSMessage dnsMessage = DNSMessage.decodeMessage(pkt.getData());
//
//
//            for (int count = 1; true; count++) {
//                socket.receive(pkt);
//                System.out.println(count + " Heard from " + pkt.getAddress() + " " + pkt.getPort());
//                for (int i = 0; i < pkt.getLength(); i++) {
//                    System.out.printf("%x", buffer[i]);
//                    System.out.print(" ");
////                    System.out.println("dnsmessageto byte: "+ dnsMessage.toBytes()[i]);
//                }
//
//
//            }
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
