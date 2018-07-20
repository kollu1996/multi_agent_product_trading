package productTrading.buyer;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.ContractNetInitiator;
import jade.content.*;
import java.util.*;

import productTrading.ontology.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;

public class ProductBuyerAgent extends Agent {
     public int k=10;
	 public  String chotu;
	 public int motu;
	

	  private Vector sellerAgents = new Vector();
	


	  private ProductBuyerGui myGui;

	

	  private Codec codec = new SLCodec();

	  private Ontology ontology = ProductTradingOntology.getInstance();

	 

	  protected void setup() {

		 


	  

	    setEnabledO2ACommunication(true, 0);

	  

	    addBehaviour(new CyclicBehaviour(this) {

	      public void action() {

	        ProductInfo info = (ProductInfo) myAgent.getO2AObject();

	        if (info != null) {

	          purchase(info.getTitle(), info.getMaxPrice(), info.getDeadline());
	        }

	        else {

	          block();

	        }

	      }

	    } );

	   

	    System.out.println("Buyer-agent "+getAID().getName()+" is ready.");

	     

	    getContentManager().registerLanguage(codec);

	    getContentManager().registerOntology(ontology);

	 

	    

	    Object[] args = getArguments();

	    if (args != null && args.length > 0) {

	      for (int i = 0; i < args.length; ++i) {

	        AID seller = new AID((String) args[i], AID.ISLOCALNAME);

	        sellerAgents.addElement(seller);

	      }

	    }

	 

	    myGui = new ProductBuyerGuiImpl();

	    myGui.setAgent(this);

	    myGui.show();

	   

	    addBehaviour(new TickerBehaviour(this,500) {

	      protected void onTick() {

	       
	        DFAgentDescription template = new DFAgentDescription();
	        ServiceDescription sd = new ServiceDescription();

	        sd.setType("product-selling");

	        template.addServices(sd);

	        try {

	          DFAgentDescription[] result = DFService.search(myAgent, template);

	          sellerAgents.clear();

	          for (int i = 0; i < result.length; ++i) {

	            sellerAgents.addElement(result[i].getName());

	          }

	        }

	        catch (FIPAException fe) {

	          fe.printStackTrace();

	        }

	      }

	    } );
	  }
	 

	
	 

	  protected void takeDown() {

	    

	    if (myGui != null) {

	      myGui.dispose();

	    }

	    

	    System.out.println("Buyer-agent "+getAID().getName()+"terminated.");

	  }
	  public void method(String title, int maxCost)
	  {
		   final String title1 = title;
		   final int max1 = maxCost;
		  addBehaviour(new OneShotBehaviour(this)
		   {
	         public void action(){
	         ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
	         aclmsg.addReceiver(new AID("observer", AID.ISLOCALNAME));
	         aclmsg.setContent(getName()+" "+ "wants to buy product"+" "+title1+" "+"at max cost of"+" "+ max1);
	         send(aclmsg);
	         }
		  });
	   
	}
	  

	
	  public void purchase(String title, int maxPrice, Date deadline) {

	   

	    addBehaviour(new PurchaseManager(this, title, maxPrice, deadline));
	  }
	  
	  
	 

	 

	  public void setCreditCard(String creditCarNumber) {

	  }

	
	 

	  private class PurchaseManager extends TickerBehaviour {

	    public String title;

	    private int maxPrice;

	    private long deadline, initTime, deltaT;

	    private PurchaseManager(Agent a, String t, int mp, Date d) {

	      super(a, 500); 
	      title = t;

	      maxPrice = mp;

	      deadline = d.getTime();

	      initTime = System.currentTimeMillis();

	      deltaT = deadline - initTime;

	    }

	    public void onTick() {

	      long currentTime = System.currentTimeMillis();

	      if (currentTime > deadline) {

	      
	        myGui.notifyUser("Cannot buy product "+title);

	        stop();

	      }

	      else {

	        

	        long elapsedTime = currentTime - initTime;
              
             
           
	        int acceptablePrice = (int)Math.round(1.0 * maxPrice * (1.0 * elapsedTime / deltaT));
	         

	        myAgent.addBehaviour(new ProductNegotiator(title, acceptablePrice, this));

	      }

	    }

	  }

	 

		 
	 
	 

	 
	 


	  
	  public ACLMessage cfp = new ACLMessage(ACLMessage.CFP); 


	  public class ProductNegotiator extends ContractNetInitiator {

	    private String title;

	    private int maxPrice;

	    private PurchaseManager manager;

	   
	   public ProductNegotiator(String t, int p, PurchaseManager m) {

	      super(ProductBuyerAgent.this, cfp);

	      title = t;

	      maxPrice = p;

	      manager = m;

	      Product product = new Product();

	      product.setTitle(title);
	    

	      Sell sellAction = new Sell();

	      sellAction.setItem(product);

	      Action act = new Action(ProductBuyerAgent.this.getAID(), sellAction);

	      try {


	        cfp.setLanguage(codec.getName());

	        cfp.setOntology(ontology.getName());

	        ProductBuyerAgent.this.getContentManager().fillContent(cfp, act);

	      } catch (Exception e) {

	        e.printStackTrace();

	      }

	    }

	 

	                   protected Vector prepareCfps(ACLMessage cfp) {

	               
	                cfp.clearAllReceiver();

	                    for (int i = 0; i < sellerAgents.size(); ++i) {

	                        cfp.addReceiver((AID) sellerAgents.get(i));

	                }

	                   Vector v = new Vector();

	                       v.add(cfp);

	                       if (sellerAgents.size() > 0)

	                   myGui.notifyUser("Sent Call for Proposal to "+sellerAgents.size()+" sellers.");
	                  return v;

	                   }

	 

	    protected void handleAllResponses(Vector responses, Vector acceptances) {

	      ACLMessage bestOffer = null;

	      int bestPrice = -1;

	      for (int i = 0; i < responses.size(); i++) {

	        ACLMessage rsp = (ACLMessage) responses.get(i);

	        if (rsp.getPerformative() == ACLMessage.PROPOSE) {

	          try {

	            ContentElementList cel = (ContentElementList)myAgent.getContentManager().extractContent(rsp);

	            int price = ((Costs)cel.get(1)).getPrice();

	            myGui.notifyUser("Received Proposal at "+price+" when maximum acceptable price was "+maxPrice);

	            if (bestOffer == null || price < bestPrice) {

	              bestOffer = rsp;

	              bestPrice = price;

	            }

	          } catch (Exception e) {

	            e.printStackTrace();

	          }

	        }

	      }

	 

	      for (int i = 0; i < responses.size(); i++) {

	        ACLMessage rsp = (ACLMessage) responses.get(i);

	        ACLMessage accept = rsp.createReply();

	        if (rsp == bestOffer) {

	          boolean acceptedProposal = (bestPrice <= maxPrice);
	          accept.setPerformative(acceptedProposal ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL);

	          accept.setContent(title);

	          myGui.notifyUser(acceptedProposal ? "sent Accept Proposal" : "sent Reject Proposal");
	        } else {

	          accept.setPerformative(ACLMessage.REJECT_PROPOSAL);  

	        }

	       

	        acceptances.add(accept);

	      }

	    }
	    protected void handleInform(ACLMessage inform) {
	    	
		     
	      int price = Integer.parseInt(inform.getContent());
	      k=price;
	      
	      myGui.notifyUser("product "+title+" successfully purchased. Price =" + price);
	      
	      k=price;
	      
	      manager.stop();
	    }
	  } 
	  
	 

}