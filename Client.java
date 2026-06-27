import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    static final int DISCOVERY_PORT = 6000;
    static final int TIMEOUT_MS = 3000;
    static String clientName = "Client-" + UUID.randomUUID().toString().substring(0, 8);
    static CursorCapture cursorCapture = new CursorCapture();

    public static void main(String[] args) throws IOException {
        List<String[]> servers = discoverServers();
        new Thread(cursorCapture::run).start();

        if (servers.isEmpty()) {
            System.out.println("No servers found on the network.");
            return;
        }

        System.out.println("Found " + servers.size() + " server(s):");
        for (int i = 0; i < servers.size(); i++) {
            String[] s = servers.get(i);
            System.out.println(i + ": " + s[1] + " (" + s[0] + ":" + s[2] + ")");
        }

        String[] chosen = servers.get(0);
        System.out.println("Auto-connecting to " + chosen[0] + ":" + chosen[2]);


        Socket socket = new Socket(chosen[0], Integer.parseInt(chosen[2]));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //keep reading whatever the server sends- its cursor stream
        new Thread(() -> {
            try {
                String serverLine;
                while ((serverLine = in.readLine()) != null) {
                    System.out.println("From server: " + serverLine);
                }
            } catch (IOException e) {
                System.out.println("Server connection closed: " + e.getMessage());
            }
        }).start();

        // continuously send this clients own cursor position
        while (true) {
            if (cursorCapture.hasMoved()) {
                CursorPos pos = cursorCapture.getPosition();
                String message = pos.getX() + "," + pos.getY() + "," + pos.getVectorX() + "," + pos.getVectorY();
                out.println(message);
                System.out.println("Sent: " + message);
            }
        }
    }

    static List<String[]> discoverServers() throws IOException {
        List<String[]> found = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_MS);

            byte[] requestData = "DISCOVER_SERVER_REQUEST".getBytes();
            DatagramPacket requestPacket = new DatagramPacket(
                    requestData, requestData.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(requestPacket);

            byte[] buf = new byte[256];
            long endTime = System.currentTimeMillis() + TIMEOUT_MS;

            while (System.currentTimeMillis() < endTime) {
                try {
                    DatagramPacket responsePacket = new DatagramPacket(buf, buf.length);
                    socket.receive(responsePacket);
                    String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                    if (response.startsWith("DISCOVER_SERVER_RESPONSE:")) {
                        String[] parts = response.split(":");
                        String hostname = parts[1];
                        String port = parts[2];
                        String ip = responsePacket.getAddress().getHostAddress();
                        String key = ip + ":" + port;
                        if (!seen.contains(key)) {
                            seen.add(key);
                            found.add(new String[]{ip, hostname, port});
                        }
                    }
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        }
        return found;
    }
}