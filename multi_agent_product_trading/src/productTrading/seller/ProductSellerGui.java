package productTrading.seller;

public interface ProductSellerGui {
	void setAgent(ProductSellerAgent a);
	  void show();
	  void hide();
	  void notifyUser(String message);
	  void dispose();
}