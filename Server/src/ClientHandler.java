import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    static String separator = ";";
    static int ID = 0;
    public int id = ID++;
    private Socket clientSocket;
    public List<String> odpowiedzi;
    public String imie;
    public int score;
    PrintWriter out;
    BufferedReader in;
    String recived;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        odpowiedzi = new ArrayList<>();
        recived = null;
        score = 0;
    }


    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            imie = in.readLine();

            while (true) {
                String tmp = in.readLine();
                if (tmp != null) {
                    recived = tmp;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String n) {
        out.println(n);
    }


    public void waitMainEvent(Pytanie pytanie) {
        String event = "WAIT";
        String add = "Czekaj na odpowiedzi wszystkich graczy";
        send(event + separator + pytanie.txt + separator + add);
    }

    public void pytanieEvent(Pytanie pytanie) {
        String event = "PYTANIE";
        String txt = pytanie.txt;
        String gaps = String.valueOf(pytanie.gaps);
        String s = event + separator + odpowiedzi.size() + separator;
        for (String n : odpowiedzi) {
            s += n + separator;
        }
        s += txt + separator + gaps;


        send(s);
    }


    public void displayAnsEvent(List<String> odp) {
        String s = "ODPOWIEDZI";
        for (String o : odp) {
            s += separator + o;
        }
        send(s);
    }

    public void chooseAnsEvent(List<String> odp) {
        String s = "ODPOWIEDZICHOOSE";
        for (String o : odp) {
            s += separator + o;
        }
        send(s);
    }

    public void displayWinner(String name, String ans) {
        String s = "DISPLAYWINNER" + separator + name + separator + ans;
        send(s);
    }

    public void displayRanking(String[] scores) {
        String s = "DISPLATRANKING";
        for (String str : scores) {
            s += separator + str;
        }
        send(s);
    }
}
