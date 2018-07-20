package productTrading.seller;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.proto.ContractNetResponder;
import jade.content.*;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import java.util.*;

import productTrading.ontology.*;

public class ProductSellerAgent extends Agent{

	
	  private Map catalogue = new HashMap();

	 
	  private ProductSellerGui myGui;

	  
	  private Codec codec = new SLCodec();
	  private Ontology ontology = ProductTradingOntology.getInstance();

	 
	  protected void setup() {
	    
	    System.out.println("Seller-agent "+getAID().getName()+" is ready.");

	    getContentManager().registerLanguage(codec);
	    getContentManager().registerOntology(ontology);

	   
	    myGui = new ProductSellerGuiImpl();
	    myGui.setAgent(this);
	    myGui.show();

	   
	    addBehaviour(new CallForOfferServer());

	   
	    DFAgentDescription dfd = new DFAgentDescription();
	    dfd.setName(getAID());
	    ServiceDescription sd = new ServiceDescription();
	    sd.setType("Product-selling");
	    sd.setName(getLocalName()+"-Product-selling");
	    dfd.addServices(sd);
	    try {
	      DFService.register(this, dfd);
	    }
	    catch (FIPAException fe) {
	      fe.printStackTrace();
	    }
	  }

	  
	  protected void takeDown() {
	   
	    if (myGui != null) {
	      myGui.dispose();
	    }

	   
	    System.out.println("Seller-agent "+getAID().getName()+"terminating.");

	    try {
	      DFService.deregister(this);
	    }
	    catch (FIPAException fe) {
	      fe.printStackTrace();
	    }
	  }
	  public void method2(String title,int desiredPrice, int minPrice)
	  {
		   final String title1 = title;
		   final int dp1 = desiredPrice;
		   final int mp = minPrice;
		  addBehaviour(new OneShotBehaviour(this)
		   {
	         public void action(){
	         ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
	         aclmsg.addReceiver(new AID("observer", AID.ISLOCALNAME));
	         aclmsg.setContent(getName()+" "+" wants to sell"+" "+title1+" "+"at desired price of"+" "+dp1+" "+"and minimum price of"+" "+mp);
	         send(aclmsg);
	         }
		  });
	   
	}
	  
	  public void putForSale(String title, int initPrice, int minPrice, Date deadline) {
	    addBehaviour(new PriceManager(this, title, initPrice, minPrice, deadline));
	  }

	  private class PriceManager extends TickerBehaviour {
	    private String title;
	    private int minPrice, currentPrice, initPrice, deltaP;
	    private long initTime, deadline, deltaT;

	    private PriceManager(Agent a, String t, int ip, int mp, Date d) {
	      super(a, 500); 
	      title = t;
	      initPrice = ip;
	      currentPrice = initPrice;
	      deltaP = initPrice - mp;
	      deadline = d.getTime();
	      initTime = System.currentTimeMillis();
	      deltaT = ((deadline - initTime) > 0 ? (deadline - initTime) : 500);
	    }

	    public void onStart() {
	     
	      catalogue.put(title, this);
	      super.onStart();
	    }

	    public void onTick() {
	      long currentTime = System.currentTimeMillis();
	      if (currentTime > deadline) {
	        
	        myGui.notifyUser("Cannot sell product "+title);
	        catalogue.remove(title);
	        stop();
	      }
	      else {
	        
	        long elapsedTime = currentTime - initTime;
	       
	        currentPrice = (int)Math.round(initPrice - 1.0 * deltaP * (1.0 * elapsedTime / deltaT));
	      }
	    }

	    public int getCurrentPrice() {
	      return currentPrice;
	    }
	  }

	  private class CallForOfferServer extends ContractNetResponder {

	    int price;

	    CallForOfferServer() {
	      super(ProductSellerAgent.this, MessageTemplate.and(MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.MatchPerformative(ACLMessage.CFP)));
	    }

	    protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException, FailureException, NotUnderstoodException {
	      
	      ACLMessage reply = cfp.createReply();
	       {
	      try {
	        ContentManager cm = myAgent.getContentManager();
	        Action act = (Action) cm.extractContent(cfp);
	        Sell sellAction = (Sell) act.getAction();
	        Product product = sellAction.getItem();
	        myGui.notifyUser("Received Proposal to buy "+product.getTitle());
	        PriceManager pm = (PriceManager)catalogue.get(product.getTitle());
	        if (pm != null) {
	          
	          reply.setPerformative(ACLMessage.PROPOSE);
	          ContentElementList cel = new ContentElementList();
	          cel.add(act);
	          Costs costs = new Costs();
	          costs.setItem(product);
	          price = pm.getCurrentPrice();
	          costs.setPrice(price);
	          cel.add(costs);
	          cm.fillContent(reply, cel);
	        }
	        else {
	          
	          reply.setPerformative(ACLMessage.REFUSE);
	        }
	      }
	      catch (OntologyException oe) {
	        oe.printStackTrace();
	        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	      }
	      catch (CodecException ce) {
	        ce.printStackTrace();
	        reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	      }
	      catch (Exception e) {
	          e.printStackTrace();
	          reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	      }
	      }
	     
	      myGui.notifyUser(reply.getPerformative() == ACLMessage.PROPOSE ? "Sent Proposal to sell at "+ price : "Refused Proposal as the product is not for sale");
	      return reply;
	    }

	    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
	      ACLMessage inform = accept.createReply();
	      inform.setPerformative(ACLMessage.INFORM);
	      inform.setContent(Integer.toString(price));
	      myGui.notifyUser("Sent Inform at price "+price);
	      myGui.notifyUser(getAID().getName()+"Sold the product");
	      return inform;
	    }
	  }
}