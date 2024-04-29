import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class commander {
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

    private List<String> keywords() throws Exception {
        // I don't know how to make this generic yet
        File file = new File("C:\\Users\\honor\\IdeaProjects\\483-MTG\\keyword.txt");

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

    private String[] commanderSample(int number, String[] deck) {
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

    private List<String> parseKeywords(List<String> keys, String text) {
        List<String> appears = new ArrayList<String>();

        text = text.toLowerCase();

        for (String key : keys) {
            if (text.contains(key.toLowerCase())) {
                appears.add(key);
            }
        }

        return appears;
    }

    public static void main(String[] args) throws Exception {
        commander temp = new commander();
        String[] h = temp.getDeck("C:\\Users\\honor\\IdeaProjects\\483-MTG\\Atraxa, Praetors' Voice.txt");
        String[] samp = temp.commanderSample(25, h);

        Index index = new Index(true, "mtgIndex");

        List<String> keys = temp.keywords();

        List<String> usableKeys = new ArrayList<>();
        for (String s : samp) {
            Query query = new QueryParser("name", index.analyzer).parse(s);
            List<Document> results = null;
            results = index.search(query, 1);

            for (Document card : results) {
                String text = card.get("text");
                System.out.println(text);
                List<String> usable = temp.parseKeywords(keys, text);
                usableKeys.addAll(usable);
            }
        }

        for (String key : usableKeys) { System.out.println(key); }
    }
}
