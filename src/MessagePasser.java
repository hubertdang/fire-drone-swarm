import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;

/**
 * This class is meant to be extended by any classes that intend to use UDP communication. It
 * should not be instantiated on its own (should always be inherited).
 */
public abstract class MessagePasser {
    private static final int MESSAGE_MAX_SIZE = 1024;
    private DatagramSocket socket;

    /**
     * Creates a MessagePasser object with a specific port to bind its socket to.
     *
     * @param port The port to bind the socket to.
     */
    public MessagePasser(int port) {
        try {
            socket = new DatagramSocket(port);
        }
        catch (SocketException e) {
            System.exit(1);
        }
    }

    /**
     * Sends a message object to a specified address and port.
     *
     * @param msg        The message object to send.
     * @param addressStr The address to send the message object to.
     * @param port       The port to send the message object to.
     */
    public void send(Object msg, String addressStr, int port) {
        InetAddress address;

        byte[] serializedMsg = serialize(msg);

        try {
            address = InetAddress.getByName(addressStr);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        DatagramPacket packet = new DatagramPacket(
                serializedMsg,
                serializedMsg.length,
                address,
                port);

        try {
            socket.send(packet);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object receive() {
        byte[] buf = new byte[MESSAGE_MAX_SIZE];

        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            socket.receive(packet);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return deserialize(packet.getData(), packet.getLength());
    }

    /**
     * Serializes a message.
     *
     * @param msg The message to serialize.
     * @return The serialize message in bytes.
     */
    public byte[] serialize(Object msg) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(msg);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        return baos.toByteArray();
    }

    /**
     * Deserializes a serialized message.
     *
     * @param msg    The serialized message to deserialize.
     * @param length The length of the serialized message in bytes.
     * @return The deserialized message.
     */
    private Object deserialize(byte[] msg, int length) {
        Object deserializedMsg;

        ByteArrayInputStream bais = new ByteArrayInputStream(msg, 0, length);

        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserializedMsg = ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return deserializedMsg;
    }

}
