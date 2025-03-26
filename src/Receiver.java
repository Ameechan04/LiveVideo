import java.io.*;
import java.net.*;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Receiver {

    public static void main(String[] args) throws Exception {
        System.setProperty("vlc.lib.path", "C:\\Program Files\\VideoLAN\\VLC"); // Replace with your VLC path

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
                        // Pass VLC options as arguments to MediaPlayerFactory
                        String[] vlcArgs = new String[] {// Disable subtitles
                                "--no-video-title-show",
                                "--sub-filter=none"  // Disable video title display
                        };

                        // Initialize MediaPlayerFactory with options
                        MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory(vlcArgs);

                        // Create the EmbeddedMediaPlayer
                        EmbeddedMediaPlayer player = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
                        player.marquee().enable(false);
                        // Specify path to the video file (make sure the file exists)
                        if (!tempFile.exists()) {
                            throw new Exception("Video file does not exist.");
                        }

                        // Play the video
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

