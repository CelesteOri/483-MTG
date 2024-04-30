import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commander {
    public String[] getDeck(String fileName) throws Exception{
        // File path is passed as parameter
        File file = new File(fileName);

        // Creating an object of BufferedReader class
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        String[] st = new String[100];
        String cur; int i = 0;
        // Condition holds true till
        // there is character in a string
        while ((cur = br.readLine()) != null) {
            String[] pair = cur.split(" ", 2);
            int occ = Integer.parseInt(pair[0]);
            for (int j = 0; j < occ; j++) {
                st[i] = pair[1];
                i++;
            }
        }

        return st;
    }

    public List<String> keywords() throws Exception {
        // I don't know how to make this generic yet
        //File file = new File("C:\\Users\\honor\\IdeaProjects\\483-MTG\\keyword.txt");
        File file = new File("keyword.txt");

        // Creating an object of BufferedReader class
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        List<String> keywordL = new ArrayList<String>();

        String cur;
        while ((cur = br.readLine()) != null) {
            keywordL.add(cur);
        }

        return keywordL;
    }

    public String[] commanderSample(int number, String[] deck) {
        String[] strings = new String[number];
        strings[0] = deck[0]; int i = 1;

        // Converting the array to a list
        List<String> dList = Arrays.asList(Arrays.copyOfRange(deck, 1, 100));

        // Shuffling the list
        Collections.shuffle(dList);

        // Converting the list back to an array
        String[] shuffledD = dList.toArray(new String[0]); int j = 0;
        while (i < number) {
            if (shuffledD[j].equals(strings[0])) { j++; }
            strings[i] = shuffledD[j];
            i++; j++;
        }

        return strings;
    }

    public List<String> parseKeywords(List<String> keys, String text) {
        List<String> appears = new ArrayList<String>();

        text = text.toLowerCase();

        Pattern pattern;
        Matcher matcher;
        for (String key : keys) {
            String word = Pattern.quote(key.toLowerCase());
            pattern = Pattern.compile("\\b" + word + "\\b");
            matcher = pattern.matcher(text);
            if (matcher.find()) {
                appears.add(key);
            }
//            if (text.contains(key.toLowerCase())) {
//                appears.add(key);
//            }
        }

        return appears;
    }

    public static void main(String[] args) throws Exception {
        Commander temp = new Commander();
        //String[] h = temp.getDeck("C:\\Users\\honor\\IdeaProjects\\483-MTG\\Atraxa, Praetors' Voice.txt");
        String[] h = temp.getDeck("Decklists/Atraxa, Praetors' Voice.txt");
        String[] samp = temp.commanderSample(50, h);

        Index index = new Index(true, "mtgIndex");

        List<String> keys = temp.keywords();
        String color = "";

        ArrayList<String> deck = new ArrayList<>();
        List<String> usableKeys = new ArrayList<>();
        for (String s : samp) {
            Query query = new QueryParser("name", index.analyzer).parse(s);
            List<Document> results = null;
            results = index.search(query, 1);

            for (Document card : results) {
                String text = card.get("text");
                List<String> usable = temp.parseKeywords(keys, text);
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

        if (results.size() > 10) {
            results = results.subList(0, 10);
        }

        for (Document card : results) {
            System.out.println(card.get("name") + " " + card.get("color_identity"));
            System.out.println(card.get("text"));
            System.out.println();
        }
    }
}
