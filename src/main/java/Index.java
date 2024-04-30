/******************************************************************************
 * AUTHOR: Adrian Moore & Honor Jang
 * FILE: Index.java
 * COURSE: CSc 483, Spring 2024
 *
 * PURPOSE:
 * 	Sets up a directory and analyzer for the database of cards.
 * 	Searches for cards based on name or card text, according to user input.
 * 	Has filter and subset functions for other programs to use. It looks for
 * 	exact matches where possible and uses the default scoring.
 *
 *****************************************************************************/
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.*;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Index {
    StandardAnalyzer analyzer;
    Directory index;

    /*
     * Loads up the index and analyzer, or builds them if they aren't
     * present.
     *
     * Parameters:
     * loadFromDisk: a boolean indicating whether to load from disk or not
     * inputFilePath: the file path to the index
     *
     * Return:
     * Nothing, sets up the index and analyzer
     */
    public Index(boolean loadFromDisk, String inputFilePath) {
        analyzer = new StandardAnalyzer();
        if (loadFromDisk) {
            loadIndex(inputFilePath);
        } else {
            createIndex(inputFilePath);
        }
    }

    /*
     * Loads up the index
     *
     * Parameters:
     * inputFilePath: the file path to the index
     *
     * Return:
     * Nothing, sets up the index or prints an error if not possible
     */
    private void loadIndex(String inputFilePath) {
        try {
            String filePath = new File(inputFilePath).getAbsolutePath();
            index = FSDirectory.open(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Creates the index and saves documents to file.
     *
     * Parameters:
     * inputFilePath: the file path to the index
     *
     * Return:
     * Nothing, sets up the index or prints an error if not possible
     */
    private void createIndex(String inputFilePath) {
        try {
            index = FSDirectory.open(Paths.get("mtgIndex"));
            //index = new ByteBuffersDirectory();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(index, config);

            ObjectMapper objectMapper = new ObjectMapper();
            String filePath = new File(inputFilePath).getAbsolutePath();
            List<Map<String, Object>> data = objectMapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> card : data) {
                //System.out.println(card.get("name"));
                //System.out.println(card.get("legalities"));
                if (((LinkedHashMap)card.get("legalities")).get("commander").equals("legal")) {
                    addDoc(writer, card);
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Adds a document to the index
     *
     * Parameters:
     * writer: an IndexWriter for index
     * card: a representation of a card with a Map<String, Object>
     *
     * Return:
     * Nothing, adds a card to the index
     */
    private void addDoc(IndexWriter writer, Map<String, Object> card) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name", (String) card.get("name"), Field.Store.YES));

        ArrayList<LinkedHashMap<String, Object>> faces = new ArrayList<>();
        try {
            faces = (ArrayList<LinkedHashMap<String, Object>>) card.get("card_faces");
        } catch (ClassCastException e) {
        }

        String oracleText = "";
        if (faces != null) {
            oracleText = oracleText.concat((String) faces.get(0).get("oracle_text"));
            oracleText = oracleText.concat("\n");
            oracleText = oracleText.concat((String) faces.get(1).get("oracle_text"));
        } else {
            oracleText = oracleText.concat((String) card.get("oracle_text"));
        }
        doc.add(new TextField("text", oracleText, Field.Store.YES));
        doc.add(new StringField("color_identity", String.join("", (ArrayList<String>) card.get("color_identity")), Field.Store.YES));
        writer.addDocument(doc);
    }

    /*
     * Searches for cards that best match the query. Uses the default scoring.
     *
     * Parameters:
     * query: the Query used for searching
     * how_many: an int representing the number of cards to return
     *
     * Return:
     * a list of Documents (cards) that match the query.
     */
    public List<Document> search(Query query, int how_many) throws IOException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, how_many);
        ScoreDoc[] hits = docs.scoreDocs;

        Arrays.sort(hits, (doc1, doc2) -> Float.compare(doc2.score, doc1.score));

        List<Document> results = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            int docID = hit.doc;
            float score = hit.score;
            Document doc = searcher.doc(docID);
            results.add(doc);
        }

        return results;
    }

    /*
     * Filters out cards that don't match a color identity or is a repeat card
     *
     * Parameters:
     * results: a list of Documents representing card data
     * cardNames: an ArrayList of String representing the names of cards
     *      already present
     * colorIdentity: a String representing the selection of colors valid
     *      results can be
     *
     * Return:
     * a list of Documents from results that match the criteria.
     */
    public List<Document> filter(List<Document> results, ArrayList<String> cardNames, String colorIdentity) {
        List<Document> filteredResults = new ArrayList<>();
        for (Document card : results) {
            if (cardNames.contains(card.get("name"))) {
                continue;
            }
            if (!isSubset(card.get("color_identity"), colorIdentity)){
                continue;
            }
            filteredResults.add(card);
        }
        return filteredResults;
    }

    /*
     * Checks if a given string's characters is a subset of another string's
     *
     * Parameters:
     * s1: the String that is being tested
     * s2: the String s1 is checked against to see if s1 is a subset of s2
     *
     * Return:
     * True, if s1 is a subset of s2; false otherwise
     */
    private boolean isSubset(String s1, String s2) {
        for (char c : s1.toCharArray()) {
            if (s2.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        // Comment this if mtgIndex is for some reason missing
        Index index = new Index(true, "mtgIndex");

        // Uncomment the below line if mtgIndex is for some reason missing
        // Index index = new Index(false, "oracleCards.json");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Magic card search!");

        boolean running = true;
        while (running) {

            System.out.println("Would you like to search card names or card text? (type n/t)");
            String searchType = "";
            while (!searchType.equals("n") && !searchType.equals("t")) {
                searchType = scanner.nextLine();
            }
            if (searchType.equals("n")) {
                searchType = "name";
            } else {
                searchType = "text";
            }

            System.out.println("Please enter a search query:");
            String userQuery = scanner.nextLine();

            Query query = null;
            try {
                query = new QueryParser(searchType, index.analyzer).parse(userQuery);
            } catch (ParseException e) {
                System.out.println("Unrecognized query :(");
                continue;
            }

            System.out.println("Your search results are:\n");
            List<Document> results = null;
            try {
                results = index.search(query, 100);
            } catch (IOException e) {
                System.out.println("Error while searching :(");
                continue;
                //e.printStackTrace();
            }

            ArrayList<String> dummy = new ArrayList<>();
            results = index.filter(results, dummy, "WUBRG");

            for (Document card : results) {
                System.out.println(card.get("name") + " " + card.get("color_identity"));
                System.out.println(card.get("text"));
                System.out.println();
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