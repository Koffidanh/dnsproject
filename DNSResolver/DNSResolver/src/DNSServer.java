import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DNSServer {
    private DNSCache cache;

    public DNSServer() {
        this.cache = new DNSCache();
    }

    public static void main(String[] args) {
        DNSServer dnsServer = new DNSServer();
        dnsServer.startServer();
    }

    public void startServer() {
        try {
            DatagramSocket socket = new DatagramSocket(8053);

            while (true) {
                byte[] buffer = new byte[512];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                // Receive DNS request
                socket.receive(packet);

                // Handle DNS request
                handleRequest(socket, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(DatagramSocket socket, DatagramPacket packet) {
        try {
            byte[] requestData = packet.getData();
            DNSMessage request = DNSMessage.decodeMessage(requestData);

            // Print received request
            System.out.println("Received DNS Request:");
            System.out.println(request);

            // Check if there are questions in the DNS message
            if (!request.messageQuestion.isEmpty()) {
                // Get the first question from the list
                DNSQuestion firstQuestion = request.messageQuestion.get(0);

                // Check cache for a valid record
                DNSRecord cachedRecord = cache.queryCache(firstQuestion);

                if (cachedRecord != null) {
                    // If a valid record is found in the cache, send the cached record as the response
                    DNSMessage response = DNSMessage.buildResponse(request, new DNSRecord[]{cachedRecord});
                    sendResponse(socket, packet, response.toBytes());
                } else {
                    // If no valid record is found, forward the request to Google's DNS
                    forwardRequestToGoogle(socket, packet, request);
                }
            } else {
                System.out.println("DNS request does not contain any questions.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void forwardRequestToGoogle(DatagramSocket socket, DatagramPacket packet, DNSMessage request) {
        try {
            // Print information before forwarding the request to Google
            System.out.println("Forwarding request to Google's DNS:");
            System.out.println(request);
            // Google DNS address
            InetAddress googleDNSAddress = InetAddress.getByName("8.8.8.8");
            int googleDNSPort = 53; // DNS port

            // Create a new socket to forward the request to Google's DNS
            DatagramSocket googleSocket = new DatagramSocket();

            // Send the DNS request to Google's DNS
            DatagramPacket googleRequestPacket = new DatagramPacket(packet.getData(), packet.getLength(), googleDNSAddress, googleDNSPort);
            googleSocket.send(googleRequestPacket);

            // Receive the response from Google's DNS
            byte[] googleResponseData = new byte[512];
            DatagramPacket googleResponsePacket = new DatagramPacket(googleResponseData, googleResponseData.length);
            googleSocket.receive(googleResponsePacket);
            System.out.println("GoogleRsp: " + new String(googleResponseData));


            // Decode the response from Google
            DNSMessage googleResponse = DNSMessage.decodeMessage(googleResponseData);

            // Update cache by inserting the ans from google
            DNSQuestion firstQuestion = request.messageQuestion.get(0);
            DNSRecord cachedRecord = cache.queryCache(firstQuestion);

            // Process the response and send it back to the client
            sendResponse(socket, packet, googleResponseData);

            // Close the socket used to communicate with Google's DNS
            googleSocket.close();

            // Print information after receiving the response from Google
            System.out.println("Received response from Google's DNS:");
            System.out.println(googleResponse);
            System.out.println("google response");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendResponse(DatagramSocket socket, DatagramPacket packet, byte[] response) {
        try {
            // Print information about the response being sent to the client
            System.out.println("Sending DNS Response:");
            System.out.println(new String(response)); // Convert byte array to string for printing

            // Set the response data and length in the packet
            packet.setData(response);
            packet.setLength(response.length);

            // Send the response back to the client
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
