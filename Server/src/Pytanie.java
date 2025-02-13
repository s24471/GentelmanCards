import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pytanie {
    String txt;
    int gaps;
    public Pytanie(String txt) {
        this.txt = txt;
        gaps = countGaps(txt);
        if(gaps == 0){
            gaps++;
            this.txt += "\n.......";
        }
    }


    public static int countGaps(String txt){
        /*int gaps = 0;
        int n = 0;
        for (int i = 0; i < txt.length(); i++) {
            if(txt.charAt(i) == '.'){
                n++;
            }else{
                if(n>=4){
                    gaps++;
                }
                n=0;
            }
        }
        if(n>=4){
            gaps++;
        }
        return gaps;*/
        return txt.split("\\.{4,}", -1).length - 1;
    }
    public static String replaceGaps(String zdanie, String[] tekstyDoWstawienia) {
        String regex = "\\.{4,}";
        StringBuffer noweZdanie = new StringBuffer();
        int index = 0;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(zdanie);

        while (matcher.find() && index < tekstyDoWstawienia.length) {
            matcher.appendReplacement(noweZdanie, tekstyDoWstawienia[index++]);
        }
        matcher.appendTail(noweZdanie);

        return noweZdanie.toString();
    }


}
