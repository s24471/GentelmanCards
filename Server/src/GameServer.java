import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private int port = 12345;
    private List<Pytanie> pytania = new ArrayList<>();
    private List<String> odpowiedzi = new ArrayList<>();
    private List<ClientHandler> clientHandlers = new ArrayList<>();

    public GameServer() {
        wczytajPytaniaIOdpowiedzi();
    }

    private void wczytajPytaniaIOdpowiedzi() {
        try {
            BufferedReader pytaniaReader = new BufferedReader(new FileReader("kd-pytania.txt"));
            String pytanie;
            while ((pytanie = pytaniaReader.readLine()) != null) {
                pytania.add(new Pytanie(pytanie));
            }
            pytaniaReader.close();


            BufferedReader odpowiedziReader = new BufferedReader(new FileReader("kd-odpowiedzi.txt"));
            String odp;
            while ((odp = odpowiedziReader.readLine()) != null) {
                odpowiedzi.add(odp);
            }
            odpowiedziReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serwer uruchomiony na porcie: " + port + " ip: " + InetAddress.getLocalHost().getHostAddress());

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("nowy telefon");
                ClientHandler ch = new ClientHandler(clientSocket);
                clientHandlers.add(ch);
                ch.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        GameServer gs = new GameServer();

        JFrame jFrame = new JFrame();
        JButton jb = new JButton("start");
        jb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gs.startGame();
            }
        });
        jFrame.add(jb);
        jFrame.setVisible(true);

        gs.start();
    }
    public void startGame(){
        GameManager gm = new GameManager(clientHandlers, pytania, odpowiedzi);
        gm.start();
    }
}
