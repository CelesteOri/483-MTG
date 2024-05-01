/******************************************************************************
 * AUTHOR: Adrian Moore & Honor Jang
 * FILE: Evaluation.java
 * COURSE: CSc 483, Spring 2024
 *
 * PURPOSE:
 * Test the effectiveness of the card recommendation system in Commander.java.
 * Runs many samples of sizes 25, 50, and 75 on a collection of decklists stored
 * in the Decklists directory. Metrics reported are hit rate and mean precision,
 * where hit rate is how often we recommend a card that is actually in the
 * unsampled part of the decklist.
 *
 *****************************************************************************/

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Evaluation {

    /*
     * Class for transfering the test data to main for reporting
     */
    public class Data {

        public boolean hit;
        public double precision;

        public Data(boolean hit, double precision) {
            this.hit = hit;
            this.precision = precision;
        }
    }

    /*
     * Performs a sinngle recommendation test
     *
     * Parameters:
     * deckName: the name of the decklist we are going to sample
     * subsetSize: the size of the sample to take from the decklist
     * k: how many card we should recommend
     *
     * Return:
     * the hit rate and precision for this test
     *
     */
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
        ArrayList<String> sample = new ArrayList<>(Arrays.asList(samp));
        boolean hit = false;
        double count = 0;
        for (Document card : results) {
            if (originalDeck.contains(card.get("name"))) {
                hit = true;
                count++;
            }
            if (sample.contains(card.get("name"))) {
                System.out.println("ERROR: RECOMMENDED CARD IN SAMPLE");
            }
        }

        return new Data(hit, count / k);
    }

    public static void main(String[] args) throws Exception {
        Evaluation eval = new Evaluation();

        int[] subsetSizes = {10};
        int iteration = 100;

        File decklistDirectory = new File("Decklists");
        File[] files = decklistDirectory.listFiles();
        ArrayList<String> decklists = new ArrayList<>();

        for (File file : files) {
            decklists.add(file.getName());
        }

        for (int i = 0; i < decklists.size(); i++) {
            System.out.println(i + ": " + decklists.get(i));
        }

        long startTime = System.currentTimeMillis();

        for (int subsetSize : subsetSizes) {
            System.out.println("Current Subset Size: " + subsetSize);
            for (int k = 1; k <= 10; k++) {
                double hits = 0;
                double precision = 0;
                for (String deckName : decklists) {
                    for (int i = 0; i < iteration; i++) {
                        Data data = eval.testDeck(deckName, subsetSize, k);
                        if (data.hit) {
                            hits++;
                        }
                        precision += data.precision;
                    }
                }

                double hitRate = hits / (20 * iteration);
                double meanPrecision = precision / (20 * iteration);

                System.out.println("    k = " + k + ":");
                System.out.println("        hitRate: " + hitRate);
                System.out.println("        meanPrecision: " + meanPrecision);
            }
        }

        long endTime = System.currentTimeMillis();

        System.out.println("\nRuntime was " + ((endTime - startTime) / 60000.0) + " minutes");
    }
}