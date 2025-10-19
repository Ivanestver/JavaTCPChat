import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.stream.DoubleStream;

public class ServerChat {
    ServerSocket listener = null;
    Socket client = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    int port = 50000;
    String msg = "";
    boolean isConnected = false;

    public void listen() {
        Thread thread = null;
        try {
            listener = new ServerSocket(port);
            System.out.println("Waiting for connection");
            client = listener.accept();
            System.out.println("Client connected " + client.getInetAddress().getHostName());
            out = new ObjectOutputStream(client.getOutputStream());
            out.flush();

            in = new ObjectInputStream(client.getInputStream());
            isConnected = true;
            thread = new Thread(new InputThread());
            thread.start();
            do {
                try {
                    msg = (String) in.readObject();
                    System.out.println("client> " + msg);
                } catch (ClassNotFoundException e) {
                    isConnected = false;
                    thread.interrupt();
                    throw new RuntimeException(e);
                }
            } while (!msg.equals("exit"));
        } catch (IOException e) {
            if (thread != null)
                thread.interrupt();
            isConnected = false;
            throw new RuntimeException(e);
        }
        finally {
            isConnected = false;
            if (thread != null)
                thread.interrupt();
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                if (listener != null)
                    listener.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class InputThread implements Runnable {
        @Override
        public void run() {
            while (isConnected && !Thread.currentThread().isInterrupted()) {
                Scanner scanner = new Scanner(System.in);
                System.out.print("server> ");
                msg = scanner.nextLine();
                sendMessage(msg);
            }
        }
    }
}