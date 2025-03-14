import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static java.lang.Thread.sleep;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;

public class MessagePasserTest {
    private TestMessagePasser1 client;
    private TestMessagePasser2 server;

    @BeforeEach
    void setUp() {
        client = new TestMessagePasser1(5000);
        server = new TestMessagePasser2(5001);
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
            Object receivedMsg = server.receive();
            System.out.println(receivedMsg);
            assertEquals(msg, receivedMsg);
        });

        receiverThread.start();
        Thread.sleep(100);
        client.send(msg, InetAddress.getLocalHost().getHostAddress(), server.getPort());
    }

    public class TestMessagePasser1 extends MessagePasser {
        private int port;

        public TestMessagePasser1(int port) {
            super(port);
            this.port = port;
        }

        public int getPort() {
            return port;
        }
    }

    public class TestMessagePasser2 extends MessagePasser {
        private int port;

        public TestMessagePasser2(int port) {
            super(port);
            this.port = port;
        }

        public int getPort() {
            return port;
        }
    }

}
