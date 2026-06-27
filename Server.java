import java.io.*;
import java.net.*;

public class Server {
    static final int TCP_PORT = 5000;
    static final int DISCOVERY_PORT = 6000;
    static CursorCapture cursorCapture = new CursorCapture();

    public static void main(String[] args) throws IOException {
        new Thread(Server::runDiscoveryListener).start();
        new Thread(cursorCapture::run).start();

        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        System.out.println("TCP server listening on port " + TCP_PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    static void handleClient(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            while (!clientSocket.isClosed()) {
                if (cursorCapture.hasMoved()) {
                    CursorPos pos = cursorCapture.getPosition();
                    String message = pos.getX() + "," + pos.getY() + "," + pos.getVectorX() + "," + pos.getVectorY();
                    out.println(message);
                    System.out.println("Sent: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }

    static void runDiscoveryListener() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setBroadcast(true);
            byte[] buf = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                if (message.equals("DISCOVER_SERVER_REQUEST")) {
                    String hostname = InetAddress.getLocalHost().getHostName();
                    String response = "DISCOVER_SERVER_RESPONSE:" + hostname + ":" + TCP_PORT;
                    byte[] responseBytes = response.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(
                        responseBytes, responseBytes.length,
                        packet.getAddress(), packet.getPort());
                    socket.send(responsePacket);
                    System.out.println("Replied to discovery request from " + packet.getAddress());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}