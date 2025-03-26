import java.io.*;
import java.net.*;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class Receiver {

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(12345); // port number
        byte[] buffer = new byte[4096];
        File tempFile = new File("streamed.ts"); // file name for downloading
        FileOutputStream fos = new FileOutputStream(tempFile); // output stream to write incoming video to

        System.out.println("Receiving video into: " + tempFile.getAbsolutePath());

        // Thread to display file size, // could delete
        new Thread(() -> {
            while (true) {
                long size = tempFile.length();
                System.out.print("\rFile size: " + size + " bytes");
                try {
                    Thread.sleep(500); // update every 0.5s
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();

        // Thread to start playback once the file is big enough
        new Thread(() -> {

            // flag to ensure video only plays once
            boolean started = false;

            // min 100kb
            int minFileSize = 100000;

            while (!started) {
                if (tempFile.length() >= minFileSize) {
                    try {
                        MediaPlayerFactory factory = new MediaPlayerFactory();
                        MediaPlayer player = factory.mediaPlayers().newMediaPlayer();
                        player.media().play(tempFile.getAbsolutePath());
                        System.out.println("\n[INFO] Video playback started.");
                        started = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(200); // check every 200ms
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();


        // UDP receiving loop
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            fos.write(packet.getData(), 0, packet.getLength());
            fos.flush();
        }
    }
}
