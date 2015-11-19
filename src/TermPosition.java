import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TermPosition implements Serializable {
    public enum Section {TITLE, ABSTRACT}

    private Section section;
    private int index;

    TermPosition(Section section, int index) {
        this.section = section;
        this.index = index;
    }

    public Section getSection() {
        return section;
    }

    public int getIndex() {
        return index;
    }
}