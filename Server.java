import java.io.*;
import java.net.*;

public class Server {
    static final int TCP_PORT = 5000;
    static final int DISCOVERY_PORT = 6000;
    static CursorCapture cursorCapture = new CursorCapture();

    public static void main(String[] args) throws IOException {
        // start the discovery responder in its own thread
        new Thread(Server::runDiscoveryListener).start();
        new Thread(cursorCapture::run).start();

        // start the normal TCP server
        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        System.out.println("TCP server listening on port " + TCP_PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received: " + line);
                out.println("Echo: " + line);
                if (line.equalsIgnoreCase("bye")) break;


                // while there is a connection we also want to send the cursor position to the client
                CursorPos pos = cursorCapture.getPosition();
                System.out.println("Cursor Position Server: (" + pos.getX() + ", " + pos.getY() + ")");
            }
            clientSocket.close();
            System.out.println("Client disconnected");
        }
    }

    static void runDiscoveryListener() {
        try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
            socket.setBroadcast(true);
            byte[] buf = new byte[256];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // waits for a discovery request

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