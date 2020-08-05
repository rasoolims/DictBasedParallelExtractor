import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class Extract {
    static HashMap<String, HashMap<String, Float>> readDict(String path) throws Exception {
        HashMap<String, HashMap<String, Float>> dict = new HashMap<>();

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            String word = spl[0];
            dict.put(word, new HashMap<>());
            for (int j = 1; j < spl.length - 1; j += 2) {
                float prob = Float.parseFloat(spl[j + 1]);
                dict.get(word).put(spl[j], prob);
            }
        }

        return dict;
    }

    public static void main(String[] args) throws Exception {
        HashMap dict = readDict(args[0]);


    }
}
