import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

class Pair<T1, T2> {
    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
}

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

    static float alignProb(HashMap<String, HashMap<String, Float>> dict, String srcSen, String dstSen) {
        float logProb = 0f;
        String[] srcWords = srcSen.split(" ");
        String[] dstWords = dstSen.split(" ");

        int wordCounts = 0;
        for (String dstWord : dstWords) {
            float highest = 0;
            for (String srcWord : srcWords) {
                if (dict.containsKey(srcWord) && dict.get(srcWord).containsKey(dstWord)) {
                    float wp = dict.get(srcWord).get(dstWord);
                    if (wp > highest)
                        highest = wp;
                }
            }
            if (highest > 0) {
                logProb += Math.log(highest);
                wordCounts++;
            }
        }
        if (wordCounts == 0) // No alignment found!
            return Float.NEGATIVE_INFINITY;
        return logProb / wordCounts;
    }

    public static void main(String[] args) throws Exception {
        HashMap dict = readDict(args[0]);

        BufferedReader srcReader = new BufferedReader(new FileReader(args[1]));
        BufferedReader dstReader = new BufferedReader(new FileReader(args[2]));
        String srcLine, dstLine;

        HashMap<String, Pair<String, Float>> bestAlignment = new HashMap<>();
        int lineNum = 0;
        while ((srcLine = srcReader.readLine()) != null && (dstLine = dstReader.readLine()) != null) {
            String srcSen = srcLine.trim().toLowerCase();
            String dstSen = dstLine.trim().toLowerCase();
            float alignProb = alignProb(dict, srcSen, dstSen);
            if (!Float.isInfinite(alignProb)) {
                if (!bestAlignment.containsKey(srcSen) || bestAlignment.get(srcSen).second < alignProb)
                    bestAlignment.put(srcLine, new Pair<>(dstSen, alignProb));
            }
            lineNum++;
            System.out.print(bestAlignment.size() + "/" + lineNum + "\r");
        }
        System.out.print(bestAlignment.size() + "/" + lineNum + "\n");

    }
}
