import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static java.lang.Thread.sleep;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;

public class MessagePasserTest {
    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;

    @BeforeEach
    void setUp() {
        try {
            sendSocket = new DatagramSocket();
            receiveSocket = new DatagramSocket();
            MessagePasser.socket = sendSocket;
        }
        catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Tests send() and receive() methods of MessagePasser class.
     * Uses a zone object as the message.
     * Creates a thread to receive packets.
     *
     * @throws InterruptedException
     * @throws UnknownHostException
     */
    @Test
    void testSendAndReceive() throws InterruptedException, UnknownHostException {
        Zone msg = new Zone(2, 90.00f, 4, 45, 0, 67);
        Thread receiverThread = new Thread(() -> {
            Object receivedMsg = MessagePasser.receive();
            assertEquals(msg, receivedMsg);

        });

        receiverThread.start();
        Thread.sleep(100);
        MessagePasser.send(msg, InetAddress.getLocalHost().getHostAddress(), receiveSocket.getLocalPort());
    }


    @Test
    void testSerialize() {
        Zone testZone = new Zone(1, 100.0f, 0, 50, 0, 50);
        byte[] serializedZone = MessagePasser.serialize(testZone);
        System.out.println(serializedZone);
        assertEquals(serializedZone.getClass(), byte[].class);
    }

    @Test
    void testDeserialize() {
        Zone testZone = new Zone(1, 100.0f, 0, 50, 0, 50);
        byte[] serializedZone = MessagePasser.serialize(testZone);

        Object deserializedZone = MessagePasser.deserialize(serializedZone, serializedZone.length);
        System.out.println(deserializedZone);
        assertEquals(deserializedZone.getClass(), Zone.class);
        assertEquals(deserializedZone, testZone);
    }
}
