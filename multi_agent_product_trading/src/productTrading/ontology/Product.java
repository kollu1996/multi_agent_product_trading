package productTrading.ontology;
import jade.content.Concept;
import jade.util.leap.List;
public class Product implements Concept {
	  private String title;
	  private List authors;
	  private String editor;
	  private String agent;

	  public String getTitle() {
	    return title;
	  }

	  public void setTitle(String title) {
	    this.title = title;
	  }

	  public List getAuthors() {
	    return authors;
	  }

	  public void setAuthors(List authors) {
	    this.authors = authors;
	  }

	  public String getEditor() {
	    return editor;
	  }

	  public void setEditor(String editor) {
	    this.editor = editor;
	  }
	}