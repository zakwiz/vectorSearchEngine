import java.io.Serializable;

public class Document implements Serializable {
    private int id;
    private String title;
    private String abstractText;
    private String publicationDate;
    private String[] authorList;

    public Document(int id) {
        this.id = id;
    }

    public Document(int id, String title, String publicationDate, String[] authorList, String abstractText) {
        this(id);
        this.title = title;
        this.abstractText = abstractText;
        this.publicationDate = publicationDate;
        System.arraycopy(authorList, 0, this.authorList, 0, authorList.length);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String[] getAuthorList() {
        return authorList.clone();
    }

    public void setAuthorList(String[] authorList) {
        this.authorList = authorList;
    }
}
