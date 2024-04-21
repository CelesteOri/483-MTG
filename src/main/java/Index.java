import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
                System.out.println(card.get("name"));
                addDoc(writer, card);
            }

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

    public static void main(String[] args) {
        Index index = new Index();

    }
}
