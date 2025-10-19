import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    Socket client = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    String msg = "";
    int port = 8888;

    void setConnection() {
        Thread thread = null;
        try {
            client = new Socket("127.0.0.1", port);
            System.out.println("Connected to server");
            out = new ObjectOutputStream(client.
                    getOutputStream());
            out.flush();
            in = new ObjectInputStream(client.
                    getInputStream());

            thread = new Thread(new InputThread());
            thread.start();
            do {
                Scanner scanner = new Scanner(System.in);
                msg = scanner.nextLine();
                sendMessage(msg);
            } while (!msg.equals("exit"));
        } catch (IOException ex) {
            if (thread != null)
                thread.interrupt();
            ex.printStackTrace();
        } finally {
            if (thread != null)
                thread.interrupt();
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (client != null)
                    client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    void sendMessage (String msg)
    {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class InputThread implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    msg = (String) in.readObject();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("server> " + msg);
            }
        }
    }
}