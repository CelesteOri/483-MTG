import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluation {

    public class Data {

        public boolean hit;
        public double precision;

        public Data(boolean hit, double precision) {
            this.hit = hit;
            this.precision = precision;
        }
    }

    public Data testDeck(String deckName, int subsetSize, int k) throws Exception {
        Commander recommender = new Commander();
        String[] h = recommender.getDeck("Decklists/" + deckName);
        String[] samp = recommender.commanderSample(subsetSize, h);

        Index index = new Index(true, "mtgIndex");

        List<String> keys = recommender.keywords();
        String color = "";

        ArrayList<String> deck = new ArrayList<>();
        List<String> usableKeys = new ArrayList<>();
        for (String s : samp) {
            Query query = new QueryParser("name", index.analyzer).parse(s);
            List<Document> results = null;
            results = index.search(query, 1);

            for (Document card : results) {
                String text = card.get("text");
                List<String> usable = recommender.parseKeywords(keys, text);
                usableKeys.addAll(usable);
                deck.add(results.get(0).get("name"));

                if (s.equals(samp[0])) {
                    color = card.get("color_identity");
                }
            }
        }

        String finalQuery = "";

        for (String key : usableKeys) {
            finalQuery = finalQuery.concat(key + " ");
        }
        Query query = new QueryParser("text", index.analyzer).parse(finalQuery);
        List<Document> results = null;
        results = index.search(query, 1000);
        results = index.filter(results, deck, color);

        if (results.size() > k) {
            results = results.subList(0, k);
        }

        ArrayList<String> originalDeck = new ArrayList<>(Arrays.asList(h));
        //ArrayList<String> sample = new ArrayList<>(Arrays.asList(samp));
        boolean hit = false;
        double count = 0;
        for (Document card : results) {
            if (originalDeck.contains(card.get("name"))) {
                hit = true;
                count++;
            }
//            if (sample.contains(card.get("name"))) {
//                System.out.println("ERROR CARD IN SAMPLE");
//            }

        }

        return new Data(hit, count / k);
    }

    public static void main(String[] args) throws Exception {
        Evaluation eval = new Evaluation();

        int[] subsetSizes = {25, 50, 75};

        for (int subsetSize : subsetSizes) {
            System.out.println("Current Subset Size: " + subsetSize);
            for (int k = 1; k <= 10; k++) {
                double hits = 0;
                double precision = 0;
                for (int i = 0; i < 100; i++) {
                    Data data = eval.testDeck("Atraxa, Praetors' Voice.txt", subsetSize, k);
                    if (data.hit) {
                        hits++;
                    }
                    precision += data.precision;
                }

                double hitRate = hits / 100;
                double meanPrecision = precision / 100;

                System.out.println("    k = " + k + ":");
                System.out.println("        hitRate: " + hitRate);
                System.out.println("        meanPrecision: " + meanPrecision);
            }
        }
    }
}

