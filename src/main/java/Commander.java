/******************************************************************************
 * AUTHOR: Honor Jang & Adrian Moore
 * FILE: Commander.java
 * COURSE: CSc 483, Spring 2024
 *
 * PURPOSE:
 * 	Offers a basic card suggestion service to the user based on a partially
 * 	completed Commander-format deck. It uses Index.java code to build an
 * 	index and search said index. The query to search is based on the keywords
 * 	that appear in the deck. Keywords that appear more will have higher
 * 	weights than less frequent ones.
 *****************************************************************************/


import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Commander {
    /*
    * Reads in a file named fileName and converts it to a usable array
    * of Strings. Each String represents a card in the deck.
    *
    * Parameters:
    * filename: a String representing the file path of the decklist
    *
    * Return:
    * A String[] with all the card names of the deck
    */
    public String[] getDeck(String fileName) throws Exception {
        // File path is passed as parameter
        File file = new File(fileName);

        // Creating an object of BufferedReader class
        BufferedReader br
                = new BufferedReader(new FileReader(file));

        // Declaring a string variable
        List<String> deck = new ArrayList<String>();
        String cur;

        // Condition holds true until there is no character in a string
        while ((cur = br.readLine()) != null) {
            if (!cur.isEmpty()) { // make sure to skip blank lines
                String[] pair = cur.split(" ", 2);
                int occ = Integer.parseInt(pair[0]);
                for (int j = 0; j < occ; j++) {
                    deck.add(pair[1]);
                }
            }
        }

        return deck.toArray(new String[0]);
    }

    /*
     * Reads in a file named keyword.txt (assumed to be in the root
     * directory) and saves its entries in a list of Strings.
     *
     * Parameters:
     * none, just call
     *
     * Return:
     * keywordL: a List<String> with all the keywords we can search for
     */
    public List<String> keywords() throws Exception {
        File file = new File("keyword.txt");

        // Creating an object of BufferedReader class
        BufferedReader br
                = new BufferedReader(new FileReader(file));
        List<String> keywordL = new ArrayList<String>();

        String cur;
        while ((cur = br.readLine()) != null)
            keywordL.add(cur);

        return keywordL;
    }

    /*
     * Takes the commander and (number - 1) deck cards from a full commander
     * deck list. The deck cards are randomly selected. Used for testing.
     *
     * Parameters:
     * number: an int representing the number of cards to sample (inc. the
     *      commander)
     * deck: String[] that represents the full deck list
     *
     * Return:
     * A String[] with the random subset and the commander
     */
    public String[] commanderSample(int number, String[] deck) {
        String[] strings = new String[number];
        strings[0] = deck[0];
        int i = 1;

        // Converting the array to a list
        List<String> dList = Arrays.asList(Arrays.copyOfRange(deck, 1, 100));

        // Shuffling the list
        Collections.shuffle(dList);

        // Converting the list back to an array
        String[] shuffledD = dList.toArray(new String[0]);

        // Put all entries into the array strings
        int j = 0;
        while (i < number) {
            // Skip the commander
            if (shuffledD[j].equals(strings[0])) { j++; }

            strings[i] = shuffledD[j];
            i++; j++;
        }

        return strings;
    }

    /*
     * Finds the keywords present in a block of text.
     *
     * Parameters:
     * keys: a List<String> of all the keywords
     * text: a String representing the cardtext
     *
     * Return:
     * appears: a List<String> with all the keywords that appear in the text
     */
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
        }

        return appears;
    }

    /*
     * Generates and displays deck addition suggestions
     *
     * Parameters:
     * index: the Index used for the program
     * sample: a String[] representing the partially constructed deck list
     * keys: a List<String> of all the keywords
     * size: an int representing the maximum number of results that can
     *      be displayed
     *
     * Return:
     * nothing
     */
    public void deckSuggestions
            (Index index, String[] sample, List<String> keys, int size)
            throws Exception {

        String color = "";
        ArrayList<String> deck = new ArrayList<>();
        List<String> usableKeys = new ArrayList<>();

        for (String s : sample) {
            // get all card data
            Query query = new QueryParser("name", index.analyzer).parse(s);
            List<Document> results = null;
            results = index.search(query, 1);

            for (Document card : results) {
                String text = card.get("text");
                List<String> usable = this.parseKeywords(keys, text);
                usableKeys.addAll(usable);
                deck.add(results.get(0).get("name"));

                // Save the commander color identity
                if (s.equals(sample[0])) {
                    color = card.get("color_identity");
                }
            }
        }

        String finalQuery = "";

        // Make a giant query
        for (String key : usableKeys) {
            finalQuery = finalQuery.concat(key + " ");
        }

        // Search for the top 1000 results and filter
        Query query = new QueryParser("text", index.analyzer).parse(finalQuery);
        List<Document> results = null;
        results = index.search(query, 1000);

        results = index.filter(results, deck, color);
        if (results.size() > size) {
            results = results.subList(0, size);
        }

        // Print out the results
        System.out.println("Showing top " + results.size() + " results:");
        System.out.println();
        for (Document card : results) {
            System.out.println(card.get("name") + " " + card.get("color_identity"));
            System.out.println(card.get("text"));
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        Commander commanderProg = new Commander();
        List<String> keys = commanderProg.keywords();

        Index index = new Index(true, "mtgIndex");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Magic Commander Deck Helper!");
        System.out.println("---------------------------------------");
        System.out.println("Before using this program, make sure that you"
                + " check the README!");

        boolean running = true;
        while (running) {
            System.out.println("");
            System.out.println("What is the filename of your deck?");
            String filename = scanner.nextLine();

            try {
                System.out.println("---------------------------------------");
                String[] decklist =
                        commanderProg.getDeck("Decklists/" + filename);
                commanderProg.deckSuggestions(index, decklist, keys, 10);
                System.out.println("---------------------------------------");
            } catch (Exception e) {
                System.out.println("Invalid file name or poorly formatted file.");
            }

            System.out.println("\nWould you like to continue searching? (type y/n)");
            String userContinue = "";
            while (!userContinue.equals("n") && !userContinue.equals("y")) {
                userContinue = scanner.nextLine();
            }
            if (userContinue.equals("n")) {
                running = false;
            }
        }
    }
}
