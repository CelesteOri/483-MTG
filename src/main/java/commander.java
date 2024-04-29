import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    public static void main(String[] args) throws Exception {
        commander temp = new commander();
        String[] h = temp.getDeck("C:\\Users\\honor\\IdeaProjects\\483-MTG\\Atraxa, Praetors' Voice.txt");

        Index index = new Index(true, "mtgIndex");
        Query query = new QueryParser("name", index.analyzer).parse(h[0]);
        List<Document> results = null;
        results = index.search(query, 1);

        for (Document card : results) {
            System.out.println(card.get("name") + " " + card.get("color_identity"));
            System.out.println(card.get("text"));
            System.out.println();
        }

        String[] samp = temp.commanderSample(25, h);
        for (String s : samp) {
            System.out.println(s);
        }
    }
}
