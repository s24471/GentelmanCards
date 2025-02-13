import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GameManager {
    List<ClientHandler> gracze = new ArrayList<>();
    List<Pytanie> pytania = new ArrayList<>();
    List<String> odpowiedzi = new ArrayList<>();
    int mainGracz;

    public GameManager(List<ClientHandler> gracze, List<Pytanie> pytania, List<String> odpowiedzi) {
        this.gracze.addAll(gracze);
        this.pytania.addAll(pytania);
        this.odpowiedzi.addAll(odpowiedzi);
    }

    public void start() {
        Collections.shuffle(pytania);
        Collections.shuffle(odpowiedzi);
        Collections.shuffle(gracze);


        mainGracz = 0;
        while (!pytania.isEmpty() && !odpowiedzi.isEmpty()) {
            fillHands();
            Pytanie pytanie = pytania.remove(0);
            gracze.get(mainGracz).waitMainEvent(pytanie);
            pytanieEvent(pytanie);

            waitForAns();

            List<String> odp = getOdp(pytanie);


            displayAns(odp);
            int winner = chooseAnsEvent(odp);
            String winAns = odp.get(winner);
            if(winner >=mainGracz)winner++;
            gracze.get(winner).score++;
            displayWinner(winner, winAns);
            sleep(3000);
            displayRanking();
            sleep(3000);
            mainGracz++;
            mainGracz = mainGracz%gracze.size();

        }


    }

    public void displayRanking() {
        int[] scores = new int[gracze.size()];
        String[] scores2 = new String[gracze.size()];
        for (int i = 0; i < gracze.size(); i++) {
            scores[i] = gracze.get(i).score;
            scores2[i] = gracze.get(i).imie + " ma " + scores[i];
        }
        boolean check = true;
        while (check) {
            check = false;
            for (int i = 0; i < scores.length - 1; i++) {
                if (scores[i] < scores[i + 1]) {
                    int tmp = scores[i];
                    scores[i] = scores[i + 1];
                    scores[i + 1] = tmp;

                    String tmp2 = scores2[i];
                    scores2[i] = scores2[i + 1];
                    scores2[i + 1] = tmp2;
                    check = true;
                }
            }
        }
        for (ClientHandler clientHandler : gracze) {
            clientHandler.displayRanking(scores2);
        }
    }

    public void displayWinner(int winner, String ans) {
        String imie = gracze.get(winner).imie;

        for (ClientHandler clientHandler : gracze) {
            clientHandler.displayWinner(imie, ans);
        }
    }

    public void displayAns(List<String> odp) {
        for (int i = 0; i < gracze.size(); i++) {
            if (i != mainGracz) {
                gracze.get(i).displayAnsEvent(odp);
            }
        }
    }

    public List<String> getOdp(Pytanie pytanie) {
        List<String> odp = new ArrayList<>();
        for (int i = 0; i < gracze.size(); i++) {
            if (i != mainGracz) {
                String tmp = gracze.get(i).recived;
                for(String n: tmp.split(";")){
                    gracze.get(i).odpowiedzi.remove(n);
                }
                odp.add(Pytanie.replaceGaps(pytanie.txt, tmp.split(";")));
                gracze.get(i).recived = null;
            }
        }
        return odp;
    }

    public int chooseAnsEvent(List<String> odp) {
        gracze.get(mainGracz).chooseAnsEvent(odp);
        while (gracze.get(mainGracz).recived == null) {
            sleep(100);
        }
        int n = Integer.parseInt(gracze.get(mainGracz).recived);
        gracze.get(mainGracz).recived = null;
        return n;
    }

    public void waitForAns() {
        boolean check = true;
        while (check) {
            sleep(100);
            check = false;
            for (int i = 0; i < gracze.size(); i++) {
                if (i != mainGracz) {
                    if (gracze.get(i).recived == null) {
                        check = true;
                        break;
                    }

                }
            }
        }
    }

    public void sleep(int mili) {
        try {
            TimeUnit.MILLISECONDS.sleep(mili);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void pytanieEvent(Pytanie pytanie) {
        for (int i = 0; i < gracze.size(); i++) {
            if (i != mainGracz) {
                gracze.get(i).pytanieEvent(pytanie);
            }
        }
    }

    public void fillHands() {
        for (ClientHandler ch : gracze) {
            fillHand(ch);
        }
    }

    public void fillHand(ClientHandler ch) {
        while (ch.odpowiedzi.size() < 5) {
            ch.odpowiedzi.add(odpowiedzi.remove(0));
        }
    }
}
