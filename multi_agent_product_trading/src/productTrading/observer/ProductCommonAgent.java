package productTrading.observer;
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
public class ProductCommonAgent extends Agent {
	protected void setup()
	{
		
		 addBehaviour(new CyclicBehaviour(this) {
	           
	            public void action(){
	             ACLMessage msg = receive();
	             if(msg!=null){
	               String kollu = msg.getContent();
	               System.out.println(kollu);
	             }
	            }
	            });
		
		
		
	}
	
  public static void main(String []args)
  {
	   ProductCommonAgent p1 = new ProductCommonAgent();
	   p1.setup();
  }
}
