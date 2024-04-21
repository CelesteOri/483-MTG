import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Index {

    String inputFilePath ="";

    StandardAnalyzer analyzer;
    Directory index;
    IndexWriterConfig config;
    IndexWriter writer;

    public Index() {
        createIndex();
    }

    private void createIndex() {

        try {
            analyzer = new StandardAnalyzer();
            index    = new ByteBuffersDirectory();
            config   = new IndexWriterConfig(analyzer);
            writer   = new IndexWriter(index, config);

            ObjectMapper objectMapper = new ObjectMapper();
            String filePath = new File("oracleCards.json").getAbsolutePath();
            List<Map<String, Object>> data = objectMapper.readValue(new File(filePath), new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> card : data) {
                //System.out.println(card.get("name"));
                addDoc(writer, card);
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void addDoc(IndexWriter writer, Map<String, Object> card) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("name", (String) card.get("name"), Field.Store.YES));
        String oracleText = (String) card.get("oracle_text");
        if (oracleText == null) {
            oracleText = "";
        }
        doc.add(new TextField("text", oracleText, Field.Store.YES));
        writer.addDocument(doc);
    }

    public List<String> search(Query query) throws IOException {
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, reader.maxDoc());
        ScoreDoc[] hits = docs.scoreDocs;

        Arrays.sort(hits, (doc1, doc2) -> Float.compare(doc2.score, doc1.score));

        List<String> results = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            int docID = hit.doc;
            float score = hit.score;
            Document doc = searcher.doc(docID);
            results.add(doc.get("name"));
        }

        return results;
    }

    public static void main(String[] args) {
        Index index = new Index();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Magic card search!");

        boolean running = true;
        while (running) {

            System.out.println("Please enter a search query:");
            String userQuery = scanner.nextLine();

            Query query = null;
            try {
                query = new QueryParser("name", index.analyzer).parse(userQuery);
            } catch (ParseException e) {
                System.out.println("Unrecognized query :(");
                continue;
            }

            List<String> results = null;
            try {
                results = index.search(query);
            } catch (IOException e) {
                //System.out.println("Error while searching :(");
                //continue;
                e.printStackTrace();
            }
            System.out.println("Your search results are:\n");
            for (String card : results) {
                System.out.println(card);
            }

            System.out.println("\nWould you like to continue searching? (y/n)");
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