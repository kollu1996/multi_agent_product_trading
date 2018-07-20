package productTrading.ontology;
import jade.content.AgentAction;
import jade.core.AID;
public class Sell implements AgentAction {
	  private Product item;

	  public Product getItem() {
	    return item;
	  }

	  public void setItem(Product item) {
	    this.item = item;
	  }

	}