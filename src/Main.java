import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress receiverAddress = InetAddress.getByName("localhost");
        int port = 12345;

        FileInputStream videoFile = new FileInputStream("video.mp4"); // file input
        byte[] dataBuffer = new byte[4096]; // or use a multiple of 188 bytes for TS packet alignment
        int bytesRead;

        File file = new File("converted-video.ts");
        if (!file.exists()) {
            System.out.println("Error: converted-video.ts does not exist!");
        } else if (file.length() == 0) {
            System.out.println("Error: converted-video.ts is empty!");
        } else {
            System.out.println("File found: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");
        }
        while ((bytesRead = videoFile.read(dataBuffer)) != -1) {
            //System.out.println("test!");
            DatagramPacket packet = new DatagramPacket(dataBuffer, bytesRead, receiverAddress, port);
            socket.send(packet); // send packet
            Thread.sleep(5); // simulate real-time streaming
        }

        videoFile.close();
        socket.close();
        System.out.println("Streaming finished.");
    }
}