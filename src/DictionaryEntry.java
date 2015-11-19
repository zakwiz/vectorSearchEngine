import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DictionaryEntry implements Serializable {
    private Map<Integer, Posting> postings;

    DictionaryEntry() {
        postings = new HashMap<>();
    }

    public int getDocFrequency() {
        return postings.size();
    }

    public Map<Integer, Posting> getPostings() {
        return postings;
    }

    public void addPosition(int documentId, TermPosition position) {
        Posting posting = postings.get(documentId);

        if (posting == null) {
            posting = new Posting();
            postings.put(documentId, posting);
        }

        posting.addTermPosition(position);
    }
}
