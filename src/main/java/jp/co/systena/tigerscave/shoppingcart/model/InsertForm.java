package jp.co.systena.tigerscave.shoppingcart.model;

/**
 * The Class InsertForm.
 */
public class InsertForm {

  /** 商品名 */
  private String input_name;

  /** 価格 */
  private int input_price;

  public String getInput_name() {
    return input_name;
  }

  public void setInput_name(String input_name) {
    this.input_name = input_name;
  }

  public int getInput_price() {
    return input_price;
  }

  public void setInput_price(int input_price) {
    this.input_price = input_price;
  }
}
