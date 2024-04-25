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

    public Index(boolean loadFromDisk, String inputFilePath) {
        analyzer = new StandardAnalyzer();
        if (loadFromDisk) {
            loadIndex(inputFilePath);
        } else {
            createIndex(inputFilePath);
        }
    }

    private void loadIndex(String inputFilePath) {
        try {
            String filePath = new File(inputFilePath).getAbsolutePath();
            index = FSDirectory.open(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
//            System.out.println(doc.get("name") + " " + doc.get("color_identity") + " (" + score + "):");
//            System.out.println(doc.get("text") + "\n");
        }

        return results;
    }

    public List<Document> filter(List<Document> results, List<Document> query, String color_identity) {
        List<Document> filteredResults = new ArrayList<>();
        for (Document card : results) {
            if (query.contains(card)) {
                continue;
            }
            if (!isSubset(card.get("color_identity"), color_identity)){
                continue;
            }
            filteredResults.add(card);
        }
        return filteredResults;
    }

    private boolean isSubset(String s1, String s2) {
        for (char c : s1.toCharArray()) {
            if (s2.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Index index = new Index(true, "mtgIndex");
//        Index index = new Index(false, "oracleCards.json");
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

            ArrayList<Document> dummy = new ArrayList<>();
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