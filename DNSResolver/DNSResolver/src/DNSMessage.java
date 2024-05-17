import java.io.*;
import java.util.*;

public class DNSMessage {
    // DNSMessage class
public DNSHeader messageHeader;
public ArrayList<DNSQuestion> messageQuestion;
//public DNSRecord[] messageAnswers;
public ArrayList<DNSRecord> messageAnswers;
public ArrayList<DNSRecord>  messageAuthority;
public ArrayList<DNSRecord> messageAdditional;
//public DNSQuestion[] messageAuthority;


public static byte [] received;
//Constructor
    public DNSMessage(){
        this.messageHeader = new DNSHeader();
        this.messageQuestion = new ArrayList<>();
        this.messageAnswers = new ArrayList<>();

        this.messageAuthority = new ArrayList<>();
        this.messageAdditional = new ArrayList<>();
    }
    static DNSMessage decodeMessage(byte[] bytes) {
        received = bytes;
        InputStream input = new ByteArrayInputStream(bytes);
        try {
            DNSMessage dnsMessage = new DNSMessage();

            // Decoding the header
            dnsMessage.messageHeader = DNSHeader.decodeHeader(input);
            int questionCount = dnsMessage.messageHeader.QDCOUNT;
            int answersCount = dnsMessage.messageHeader.ANCOUNT;
            int authorityCount = dnsMessage.messageHeader.NSCOUNT;
            int additionalCount = dnsMessage.messageHeader.ARCOUNT;


            // Read domain name
//            String[] domainName = dnsMessage.readDomainName(input);
//            for (String piece : domainName) {
//                System.out.println("Domain piece: " + piece);
//            }
//            System.out.println("Domain Name: " + String.join(".", domainName));

            // Decoding questions
            System.out.println("decoding questions ---------");
            for (int i = 0; i < questionCount; i++) {
                dnsMessage.messageQuestion.add(DNSQuestion.decodeQuestion(input, dnsMessage));
            }

            // Decoding Answers
            for (int i = 0; i < answersCount; i++) {
                dnsMessage.messageAnswers.add(DNSRecord.decodeRecord(input, dnsMessage));
            }

            // Decoding authority
            for (int i = 0; i < authorityCount; i++) {
                dnsMessage.messageAuthority.add(DNSRecord.decodeRecord(input, dnsMessage));
            }

            // Decoding additional
            for (int i = 0; i < additionalCount; i++) {
                dnsMessage.messageAdditional.add(DNSRecord.decodeRecord(input, dnsMessage));
            }

            return dnsMessage;
        } catch (IOException e) {
            e.printStackTrace(); // Print the stack trace for debugging purposes
            return null; // Return null to indicate failure
        }
    }


    String[] readDomainName(InputStream input) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(input);
        ArrayList<String> domainParts = new ArrayList<>();
        int labelLength = dataInputStream.readByte();

        System.out.println("check label length....");
        while (labelLength > 0) {
//            if (labelLength < 0) {
//                throw new IOException("Invalid label length: " + labelLength);
//            }
            System.out.println("length: " +  labelLength);

            byte[] labelBytes = new byte[labelLength];

            for (int i = 0; i < labelLength; i++) {
                labelBytes[i] = dataInputStream.readByte();
            }
            domainParts.add(new String(labelBytes));
            labelLength = dataInputStream.readByte();
        }

        for (String piece : domainParts) {
            System.out.printf("part DNSMessage : " + piece);
//            domainParts.add(piece);
        }
//        for(String c : domainParts.toArray(new String[0]) ){
//            System.out.printf("domainParts.toArray(new String[0]) : " + c);
//        }
//        System.out.printf("domainParts.toArray(new String[0]) : " + domainParts.toArray(new String[0]));
        return domainParts.toArray(new String[0]);
//        return domainParts;
    }


    String[] readDomainName(int firstByte) throws IOException{

    ByteArrayInputStream domainNameByte = new ByteArrayInputStream(received, firstByte, received.length - firstByte);
        return readDomainName(domainNameByte);
    }
/*--build a response based on the request and the answers you intend to send back.*/
    static DNSMessage buildResponse(DNSMessage request, DNSRecord[] answers) {
        DNSMessage response = new DNSMessage();
        // Copy the header and questions from the request
        response.messageHeader = DNSHeader.buildHeaderForResponse(request, response);
        response.messageQuestion = request.messageQuestion;
        // Copy the answers to the response
        for(DNSRecord c : answers){
            response.messageAnswers.add(c);
//            System.out.println("answers: " + c);
        }

        // Copy the authority and additional sections if needed
        response.messageAuthority = request.messageAuthority;
        response.messageAdditional = request.messageAdditional;

        return response;
    }

/*get the bytes to put in a packet and send back*/
byte[] toBytes() throws IOException {

    try {

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
         DataOutputStream dataOutputStream = new DataOutputStream(byteArray);
        Map<String, Integer> domainOffsets = new HashMap<>();
        // Encode the DNS header
        messageHeader.encodeHeader(dataOutputStream);

        // Encode the questions
        for (DNSQuestion question : messageQuestion) {
            question.encodeQuestion(dataOutputStream);
        }

        // Encode the answers (with null check)
        if (messageAnswers != null) {
            for (DNSRecord answer : messageAnswers) {
                if (answer != null) {
                    answer.encodeRecord(dataOutputStream, domainOffsets);
                }
            }
        }

        // Encode the authority
        for (DNSRecord authority : messageAuthority) {
            authority.encodeRecord(dataOutputStream, domainOffsets);
        }

        // Encode the additional
        for (DNSRecord additional : messageAdditional) {
            additional.encodeRecord(dataOutputStream, domainOffsets);
        }

        return byteArray.toByteArray();
    }
    catch (IOException e){
        e.printStackTrace();
        return null;
    }
}



    /*If this is the first time we've seen this domain name in the packet, write it using the DNS encoding
     (each segment of the domain prefixed with its length, 0 at the end), and add it to the hash map.
     Otherwise, write a back pointer to where the domain has been seen previously.*/
    static void writeDomainName(ByteArrayOutputStream output, HashMap<String, Integer> domainLocations, String[] domainPieces) {
        // Check if the domain name exists in the domainLocations
        if (domainLocations.containsKey(String.join(".", domainPieces))) {
            // Write back pointer
            int backPointer = domainLocations.get(String.join(".", domainPieces));
            writeBackPointer(output, backPointer);
        } else {
            // Write domain name using DNS encoding
            writeDomainNameWithDNSFormat(output, domainPieces);

            // Add the domain name and its position to the domainLocations
            int currentPosition = output.size();
            domainLocations.put(String.join(".", domainPieces), currentPosition);
        }
    }

    static void writeBackPointer(ByteArrayOutputStream output, int position) {
        // Back pointer format in DNS encoding
        int backPointerFormat = 0xC000 | position;
        output.write((backPointerFormat >> 8) & 0xFF);
        output.write(backPointerFormat & 0xFF);
    }

    static void writeDomainNameWithDNSFormat(ByteArrayOutputStream output, String[] domainPieces) {
        for (String piece : domainPieces) {
            // Write length of the domain label
            output.write(piece.length());

            // Write each character of the domain label
            for (char c : piece.toCharArray()) {
                output.write(c);
            }
        }

        // Write the 0 at the end
        output.write(0);
    }


    String joinDomainName(String[] pieces) throws IOException {

        return String.join(".", pieces);
    }

    @Override
    public String toString() {

        return ("DNSMessage: \n" + " MESSAGEHEADER: " + messageHeader + "\n" + "MESSAGEQUESTIONS= " + messageQuestion + "\n" + "MESSAGEANSWERS= " + messageAnswers + "\n" );
    }



}
