import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DNSQuestion {
    //
    public String QName;
    public int QType;
    public int QClass;



    /* static DNSQuestion decodeQuestion(InputStream, DNSMessage)
    -- read a question from the input stream. Due to compression,
    you may have to ask the DNSMessage containing this question
    to read some of the fields.*/
    static DNSQuestion decodeQuestion(InputStream input , DNSMessage message) throws IOException {
        DNSQuestion dnsQuestion = new DNSQuestion();
        //Name of the question
        String[] domainName = message.readDomainName(input);
//        for (String c : message.readDomainName(input)){
//            System.out.println("qname fromquestion: " + c );
//        }

        dnsQuestion.QName = message.joinDomainName(domainName);
        //Type of the question
        DataInputStream dataInputStream = new DataInputStream(input);
        dnsQuestion.QType = dataInputStream.readShort();
        //Class of the question
        dnsQuestion.QClass = dataInputStream.readShort();

        return dnsQuestion;
    }

    /*void writeBytes(ByteArrayOutputStream, HashMap<String,
    Integer> domainNameLocations). Write the question bytes
    which will be sent to the client. The hash map is used
    for us to compress the message, see the DNSMessage class below.*/
    void writeBytes(ByteArrayOutputStream byteArrayOutputStream, HashMap<String, Integer> domainNameLocations) {
        for (Map.Entry<String, Integer> entry : domainNameLocations.entrySet()) {
            // Write the bytes for the key (domain name)
            writeDomainNameWithDNSFormat(byteArrayOutputStream, entry.getKey().split("\\."));

            // Write the bytes for the value (location)
            byteArrayOutputStream.write((entry.getValue() >> 8) & 0xFF);
            byteArrayOutputStream.write(entry.getValue() & 0xFF);
        }
    }

    void writeDomainNameWithDNSFormat(ByteArrayOutputStream output, String[] domainPieces) {
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


    /* toString(), equals(), and hashCode() -- Let your IDE generate
    these. They're needed to use a question as a HashMap key, and to
    get a human readable string.*/

    @Override
    public String toString() {
        return "DNSQuestion{" +
                "QName='" + QName + '\'' +
                ", QType=" + QType +
                ", QClass=" + QClass +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DNSQuestion that)) return false;
        return QType == that.QType && QClass == that.QClass && Objects.equals(QName, that.QName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(QName, QType, QClass);
    }

    void encodeQuestion(DataOutputStream output) throws IOException {
        // Create a ByteArrayOutputStream to temporarily store the encoded question
        ByteArrayOutputStream questionBytes = new ByteArrayOutputStream();
        DataOutputStream questionOutput = new DataOutputStream(questionBytes);

        // Encode the QNAME
        DNSMessage.writeDomainName(questionBytes, new HashMap<>(), QName.split("\\."));

        // Encode the QTYPE (2 bytes)
        questionOutput.writeShort(QType);

        // Encode the QCLASS (2 bytes)
        questionOutput.writeShort(QClass);

        // Write the encoded question to the main output stream
        output.write(questionBytes.toByteArray());
    }

}
