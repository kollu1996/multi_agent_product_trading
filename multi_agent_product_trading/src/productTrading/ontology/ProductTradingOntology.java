package productTrading.ontology;
import jade.content.onto.*;
import jade.content.schema.*;
public class ProductTradingOntology  extends Ontology implements ProductTradingVocabulary {
	 
	  public static final String ONTOLOGY_NAME = "product-trading-ontology";

	 

	 
	  private static Ontology theInstance = new ProductTradingOntology();

	  
	  public static Ontology getInstance() {
	    return theInstance;
	  }

	  
	  private ProductTradingOntology() {
	    
	    super(ONTOLOGY_NAME, BasicOntology.getInstance());
	    try {
	      add(new ConceptSchema(product), Product.class);
	      add(new PredicateSchema(COSTS), Costs.class);
	      add(new AgentActionSchema(SELL), Sell.class);

	     
	      ConceptSchema cs = (ConceptSchema) getSchema(product);
	      cs.add(PRODUCT_TITLE, (PrimitiveSchema) getSchema(BasicOntology.STRING));
	      cs.add(PRODUCT_AUTHORS, (PrimitiveSchema) getSchema(BasicOntology.STRING), 0,
	             ObjectSchema.UNLIMITED);
	      cs.add(PRODUCT_EDITOR, (PrimitiveSchema) getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);

	     
	      PredicateSchema ps = (PredicateSchema) getSchema(COSTS);
	      ps.add(COSTS_ITEM, (ConceptSchema) cs);
	      ps.add(COSTS_PRICE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

	     
	      AgentActionSchema as = (AgentActionSchema) getSchema(SELL);
	      as.add(SELL_ITEM, (ConceptSchema) getSchema(product));
	    }
	    catch (OntologyException oe) {
	      oe.printStackTrace();
	    }
	  }
	}