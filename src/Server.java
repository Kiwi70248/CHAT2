import java.io.*;
import java.net.*;
import java.util.LinkedList;

class Server extends Thread {

    private Socket socket;
    private BufferedReader in; // поток чтения из сокета
    private BufferedWriter out; // поток чтения в сокет
    public static LinkedList<Server> serverList = new LinkedList<>();

    public Server(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }
    @Override
    public void run() {
        String word;
        try {
            word = in.readLine(); // чтение ника с консоли
            try {
                out.write(word + "\n");
                out.flush();
            } catch (IOException ignored) {}
            try {
                while (true) {
                    word = in.readLine();
                    if(word.equals("stop")) {
                        this.closeAll();
                        break;
                    }
                    System.out.println("Echoing: " + word);
                    for (Server var : Server.serverList) {
                        var.send(word); //отправка сообщения всем пользователям
                    }
                }
            } catch (NullPointerException ignored) {}

        } catch (IOException e) {
            this.closeAll();
        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}

    }

    private void closeAll() {
        try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (Server var : Server.serverList) {
                    if(var.equals(this)) var.interrupt();
                    Server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }
}

class MainServer {

    public static final int PORT = 7777;
    public static LinkedList<Server> serverList = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");
        try {
            while (true) {
                Socket socket = server.accept();
                try {
                    Server.serverList.add(new Server(socket)); // добавление соединения в список
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}