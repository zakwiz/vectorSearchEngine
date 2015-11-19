import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Posting implements Serializable {
    private List<TermPosition> termPositions;

    Posting() {
        termPositions = new ArrayList<>();
    }

    Posting(TermPosition termPosition) {
        this();
        termPositions.add(termPosition);
    }

    public void addTermPosition(TermPosition position) {
        termPositions.add(position);
    }

    public int getFrequency() {
        return termPositions.size();
    }

    public List<TermPosition> getTermPositions() {
        return (List<TermPosition>)((ArrayList<TermPosition>)termPositions).clone();
    }
}
