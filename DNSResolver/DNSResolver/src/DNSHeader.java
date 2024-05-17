import java.awt.*;
import java.io.*;
import java.io.DataOutputStream;
import java.io.IOException;


public class DNSHeader {
public int ID;
//public int Flags;
public boolean QR;
public int OpCode;
public boolean AA;

public boolean TC;
public boolean RD;
public boolean RA;
public int Z;
public boolean AD;
public boolean CD;
public int RCODE;
public int QDCOUNT;
public int ANCOUNT;
public int NSCOUNT;
public int ARCOUNT;
//


/* static DNSHeader decodeHeader(InputStream) --read the header from an input stream
(we'll use a ByteArrayInputStream but we will only use the basic read methods of
input stream to read 1 byte, or to fill in a byte array, so we'll be generic).*/
static DNSHeader decodeHeader(InputStream input) throws IOException {
    try {
        DNSHeader header = new DNSHeader();
        DataInputStream dataInputStream = new DataInputStream(input);

        // Read individual fields
        header.ID = dataInputStream.readShort();

        // Read the Flags field
        int flags = dataInputStream.readShort();
        header.QR = (flags & 0x8000) != 0;
        header.OpCode = (flags >> 11) & 0x0F;
        header.AA = (flags & 0x0400) != 0;
        header.TC = (flags & 0x0200) != 0;
        header.RD = (flags & 0x0100) != 0;
        header.RA = (flags & 0x0080) != 0;
        header.Z = (flags >> 4) & 0x07;
        header.RCODE = flags & 0x0F;

        header.QDCOUNT = dataInputStream.readShort();
        header.ANCOUNT = dataInputStream.readShort();
        header.NSCOUNT = dataInputStream.readShort();
        header.ARCOUNT = dataInputStream.readShort();

        // Print for debugging
        System.out.println("header.ID : " + header.ID);
        System.out.println("header.QR : " + header.QR);
        System.out.println("header.OPCODE : " + header.OpCode);
        System.out.println("header.AA : " + header.AA);
        System.out.println("header.TC : " + header.TC);
        System.out.println("header.RD : " + header.RD);
        System.out.println("header.RA : " + header.RA);
        System.out.println("header.Z : " + header.Z);
        System.out.println("header.RCODE : " + header.RCODE);
        System.out.println("header.QDCOUNT : " + header.QDCOUNT);
        System.out.println("header.ANCOUNT : " + header.ANCOUNT);
        System.out.println("header.NSCOUNT : " + header.NSCOUNT);
        System.out.println("header.ARCOUNT : " + header.ARCOUNT);

        return header;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

/* static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response)
 -- This will create the header for the response. It will copy some fields from the
  request*/

    static DNSHeader buildHeaderForResponse(DNSMessage request, DNSMessage response) {
        DNSHeader responseHeader = new DNSHeader();
        responseHeader.ID = request.messageHeader.ID;
        responseHeader.QR = true;
        responseHeader.OpCode = request.messageHeader.OpCode; // Copy OPCODE from the request
        responseHeader.AA = true; // Set AA to true if the server is authoritative
        responseHeader.TC = false; // Set TC to false
        responseHeader.RD = request.messageHeader.RD; // Copy RD from the request
        responseHeader.RA = true; // Set RA to true indicating recursive resolution is available
        responseHeader.AD = false; // Set AD to false
        responseHeader.CD = false; // Set CD to false
        // Set other fields as needed
        responseHeader.QDCOUNT = request.messageHeader.QDCOUNT;
        responseHeader.ANCOUNT = request.messageHeader.ANCOUNT;
        responseHeader.NSCOUNT = request.messageHeader.NSCOUNT;
        responseHeader.ARCOUNT = request.messageHeader.ARCOUNT;

        return responseHeader;
    }


    /* void writeBytes(OutputStream) --encode the header to bytes to be sent back to the
     client. The OutputStream interface has methods to write a single byte or an array of
     bytes. */
//    void writeBytes(OutputStream output) throws IOException {
//        // Write ID as two bytes (big-endian)
//        output.write((ID >> 8) & 0xFF);
//        output.write(ID & 0xFF);
//
//        // Write Flags as two bytes (big-endian)
//        output.write((Flags >> 8) & 0xFF);
//        output.write(Flags & 0xFF);
//
//        // Write QDCOUNT as two bytes (big-endian)
//        output.write((QDCOUNT >> 8) & 0xFF);
//        output.write(QDCOUNT & 0xFF);
//
//        // Write ANCOUNT as two bytes (big-endian)
//        output.write((ANCOUNT >> 8) & 0xFF);
//        output.write(ANCOUNT & 0xFF);
//
//        // Write NSCOUNT as two bytes (big-endian)
//        output.write((NSCOUNT >> 8) & 0xFF);
//        output.write(NSCOUNT & 0xFF);
//
//        // Write ARCOUNT as two bytes (big-endian)
//        output.write((ARCOUNT >> 8) & 0xFF);
//        output.write(ARCOUNT & 0xFF);
//
//
//    }


    /* String toString() -- Return a human readable string version of a header object.
    A reasonable implementation can be autogenerated by your IDE.*/

    @Override
    public String toString() {
        return "DNSHeader{" +
                "ID=" + ID +
                ", QR=" + QR +
                ", OpCode=" + OpCode +
                ", AA=" + AA +
                ", TC=" + TC +
                ", RD=" + RD +
                ", RA=" + RA +
                ", Z=" + Z +
                ", AD=" + AD +
                ", CD=" + CD +
                ", RCODE=" + RCODE +
                ", QDCOUNT=" + QDCOUNT +
                ", ANCOUNT=" + ANCOUNT +
                ", NSCOUNT=" + NSCOUNT +
                ", ARCOUNT=" + ARCOUNT +
                '}';
    }

    void encodeHeader(DataOutputStream output) throws IOException {
        // Encode the DNS header fields
        int flags = 0;
        flags |= (QR ? 1 << 15 : 0);  // Set the QR bit if QR is true
        flags |= (OpCode & 0x0F) << 11;  // Set the OpCode bits
        flags |= (AA ? 1 << 10 : 0);  // Set the AA bit if AA is true
        flags |= (TC ? 1 << 9 : 0);  // Set the TC bit if TC is true
        flags |= (RD ? 1 << 8 : 0);  // Set the RD bit if RD is true
        flags |= (RA ? 1 << 7 : 0);  // Set the RA bit if RA is true
        flags |= (Z & 0x07) << 4;  // Set the Z bits
        flags |= RCODE & 0x0F;  // Set the RCODE bits

        // Write the header to the output stream
        output.writeShort(ID);
        output.writeShort(flags);
    }

}
