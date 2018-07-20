package productTrading.buyer;

public interface ProductBuyerGui {

	void setAgent(ProductBuyerAgent a);
	  void show();
	  void hide();
	  void notifyUser(String message);
	  void dispose();
}