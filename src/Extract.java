import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

class Pair<T1, T2> {
    public T1 first;
    public T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
}

public class Extract {
    static HashSet<String> ignoreWords = new HashSet<>();

    static HashMap<String, HashMap<String, Float>> readDict(String path) throws Exception {
        HashMap<String, HashMap<String, Float>> dict = new HashMap<>();
        ignoreWords.add("&apos;");
        ignoreWords.add("&");
        ignoreWords.add(";");
        ignoreWords.add("!");
        ignoreWords.add("?");
        ignoreWords.add("(");
        ignoreWords.add(")");
        ignoreWords.add("[");
        ignoreWords.add("]");
        ignoreWords.add("{");
        ignoreWords.add("}");
        ignoreWords.add("؟");
        ignoreWords.add("!");
        ignoreWords.add(".");

        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            String word = spl[0];
            if (ignoreWords.contains(word) || word.startsWith("&"))
                continue;
            dict.put(word, new HashMap<>());
            for (int j = 1; j < spl.length - 1; j += 2) {
                if (ignoreWords.contains(spl[j]) || spl[j].startsWith("&"))
                    continue;
                float prob = Float.parseFloat(spl[j + 1]);
                dict.get(word).put(spl[j], prob);
            }
        }

        return dict;
    }

    static float alignProb(HashMap<String, HashMap<String, Float>> dict, String srcSen, String dstSen) {
        float logProb = 0f;
        if (srcSen.equals(dstSen))
            return Float.NEGATIVE_INFINITY;
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
        if (wordCounts < 3) // No alignment found!
            return Float.NEGATIVE_INFINITY;
        return logProb / wordCounts;
    }

    public static void main(String[] args) throws Exception {
        HashMap dict = readDict(args[0]);

        BufferedReader srcReader = new BufferedReader(new FileReader(args[1]));
        BufferedReader dstReader = new BufferedReader(new FileReader(args[2]));
        String srcLine, dstLine;

        HashMap<String, Pair<String, Float>> bestAlignment = new HashMap<>();
        HashMap<String, Pair<String, Float>> bestRevAlignment = new HashMap<>();
        int lineNum = 0;
        while ((srcLine = srcReader.readLine()) != null && (dstLine = dstReader.readLine()) != null) {
            srcLine = srcLine.trim();
            dstLine = dstLine.trim();
            String srcSen = srcLine.toLowerCase();
            String dstSen = dstLine.toLowerCase();
            float alignProb = alignProb(dict, srcSen, dstSen);
            if (!Float.isInfinite(alignProb)) {
                if (!bestAlignment.containsKey(srcLine) || bestAlignment.get(srcLine).second < alignProb)
                    bestAlignment.put(srcLine, new Pair<>(dstSen, alignProb));
                if (!bestRevAlignment.containsKey(dstLine) || bestRevAlignment.get(dstLine).second < alignProb)
                    bestRevAlignment.put(dstLine, new Pair<>(srcLine, alignProb));
            }
            lineNum++;
            if (lineNum % 10000 == 0)
                System.out.print(bestAlignment.size() + "/" + bestRevAlignment.size() + "/" + lineNum + "\r");
        }
        System.out.print(bestAlignment.size() + "/" + lineNum + "\n");

        System.out.println("Writing alignments");
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
        int written = 0;
        for (String srcSen : bestAlignment.keySet()) {
            String dstSen = bestAlignment.get(srcSen).first;
            if (bestRevAlignment.containsKey(dstSen) && bestRevAlignment.get(dstSen).first.equals(srcSen)) {
                double prob = Math.exp(bestAlignment.get(srcSen).second);
                double probRev = Math.exp(bestRevAlignment.get(dstSen).second);
                double allProb = prob * probRev;
                writer.write(srcSen + "\t" + dstSen + "\t" + prob + "\t" + probRev + "\t" + allProb + "\n");
                written++;
            }
        }
        System.out.println("Writing alignments " + written + " done!");


    }
}
