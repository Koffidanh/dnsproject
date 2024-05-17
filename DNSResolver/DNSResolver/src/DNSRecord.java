import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DNSRecord {
public String recordName;
public int type;
public int classCode;
public int ttl;
public int RDLength;
public byte [] rData;

//Construction
    public DNSRecord(String name, int type, int classCode, long ttl, int RDLength, byte [] rData){
        this.recordName = name;
        this.type = type;
        this.classCode = classCode;
        this.ttl = (int) ttl;
        this.RDLength = RDLength;
        this.rData = rData;
    }

    public DNSRecord(){
        this.recordName = "" ;
        this.type = 0;
        this.classCode = 0;
        this.ttl = 0;
        this.RDLength = 0;
        this.rData = new byte[this.RDLength];
    }



    static DNSRecord decodeRecord(InputStream input, DNSMessage message) {
        try {
//            DNSRecord dnsRecord = new DNSRecord();
            String recordName = "";
            DataInputStream dataInputStream = new DataInputStream(input);
            dataInputStream.mark(2);
            short first2Bytes = dataInputStream.readShort();
            boolean isCompressed = (first2Bytes & 0xC000) == 0xC000;
            if(isCompressed){
                short offset = (short) (first2Bytes & 0x3fff);
                recordName = String.join(".", message.readDomainName(offset));
            }
            else {
                dataInputStream.reset();
                recordName = String.join(".", message.readDomainName(input) ) ;
//            System.out.println("domanin in recored: " + String.join(".", message.readDomainName(input) ) );

            }
            short type = dataInputStream.readShort();
            short classCode = dataInputStream.readShort();
            int ttl = dataInputStream.readInt();
            short RDLength = dataInputStream.readShort();
            byte[] rData = dataInputStream.readNBytes(RDLength);

//            for (int i = 0; i < RDLength; i++) {
//                rData[i] = (byte) input.read();
//            }
            return new DNSRecord(recordName, type, classCode, ttl, RDLength, rData);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


    void writeBytes(ByteArrayOutputStream output, HashMap<String, Integer> domainNameLocations) {
        for (Map.Entry<String, Integer> entry : domainNameLocations.entrySet()) {
            // Write the bytes for the key (domain name)
            writeStringAsBytes(output, entry.getKey());

            // Write the bytes for the value (location)
            output.write((entry.getValue() >> 8) & 0xFF);
            output.write(entry.getValue() & 0xFF);
        }
    }

    // Helper method to convert a String to bytes
    void writeStringAsBytes(ByteArrayOutputStream byteArrayOutputStream, String value) {
        byte[] stringBytes = value.getBytes(StandardCharsets.UTF_8);

        // Write the length of the string as a 16-bit unsigned integer
        byteArrayOutputStream.write((stringBytes.length >> 8) & 0xFF);
        byteArrayOutputStream.write(stringBytes.length & 0xFF);

        // Write the actual string bytes
        byteArrayOutputStream.write(stringBytes, 0, stringBytes.length);
    }


    @Override
    public String toString() {

        return ("DNSRECORD: " + "\n" + "RECORD NAME= " + recordName +"\n" +"DNSTYPE= " + type + "\n" + "DNSCLASSRECORD= " + classCode + "\n" + "DNSTTL: " + ttl + "\n" + "DNSRDLENGHT= " + RDLength + "\n" + "RDATA= " + rData + "\n");
    }

    boolean isExpired() {
        Date date = new Date();

        return ttl - (long)date.getTime() < 0;
    }

    public void encodeRecord(DataOutputStream dataOutputStream, Map<String, Integer> domainOffsets) throws IOException {

        // Write the domain name using compression if it has occurred before
        writeDomainName(dataOutputStream, recordName, domainOffsets);

        // Write other fields of the record
        dataOutputStream.writeShort(type);
        dataOutputStream.writeShort(classCode);
        dataOutputStream.writeInt(ttl);
        dataOutputStream.writeShort(RDLength);
        dataOutputStream.write(rData);
    }

    private void writeDomainName(DataOutputStream dataOutputStream, String domainName, Map<String, Integer> domainOffsets) throws IOException {
        Integer offset = domainOffsets.get(domainName);
        if (offset != null) {
            // Write compression pointer
            dataOutputStream.writeShort(0xC000 | offset);
        } else {
            // Write the domain name normally
            // Write each label separately, followed by a terminating zero byte
            String[] labels = domainName.split("\\.");
            for (String label : labels) {
                byte[] labelBytes = label.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeByte(labelBytes.length);
                dataOutputStream.write(labelBytes);
            }
            // Terminate the domain name with a zero byte
            dataOutputStream.writeByte(0);
            // Store the offset for future reference
            domainOffsets.put(domainName, dataOutputStream.size());
        }
    }
}
