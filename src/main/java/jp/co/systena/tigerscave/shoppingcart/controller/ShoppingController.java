package jp.co.systena.tigerscave.shoppingcart.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import jp.co.systena.tigerscave.shoppingcart.model.Cart;
import jp.co.systena.tigerscave.shoppingcart.model.DeleteForm;
import jp.co.systena.tigerscave.shoppingcart.model.InsertForm;
import jp.co.systena.tigerscave.shoppingcart.model.ListForm;
import jp.co.systena.tigerscave.shoppingcart.model.Order;
import jp.co.systena.tigerscave.shoppingcart.service.ListService;

/**
 * The Class ShoppingController.
 */
@Controller // Viewあり。Viewを返却するアノテーション
public class ShoppingController {

  @Autowired
  HttpSession session; // セッション管理

  @Autowired
  ListService listService; // サービスクラス

  /**
   * 商品一覧画面表示(select)
   *
   * @param mav the mav
   * @return the model and view
   */
  @RequestMapping(value = "/list", method = RequestMethod.GET) // URLとのマッピング
  public ModelAndView list(ModelAndView mav) {

    // 商品一覧取得
    List<Map<String, Object>> itemList = listService.getItemList();

    // テンプレートに渡す内容を設定
    mav.addObject("itemList", itemList);

    // Viewのテンプレート名を設定
    mav.setViewName("ListView");

    return mav;
  }

  /**
   * 注文内容をカートに追加する
   *
   * @param mav the mav
   * @param listForm the list form
   * @param bindingResult the binding result
   * @return the model and view
   */
  @RequestMapping(value = "/list", method = RequestMethod.POST) // URLとのマッピング
  public ModelAndView order(ModelAndView mav, @Validated ListForm listForm,
      BindingResult bindingResult) {

    if (bindingResult.getAllErrors().size() > 0) {
      // リクエストパラメータにエラーがある場合は商品一覧画面を表示

      List<Map<String, Object>> itemListMap = listService.getItemList();
      mav.addObject("itemList", itemListMap);

      // Viewのテンプレート名を設定
      mav.setViewName("ListView");

      return mav;

    }

    // 注文内容をカートに追加
    Cart cart = getCart();
    cart.addOrder(listForm.getItem_id(), listForm.getItem_name());

    // データをセッションへ保存
    session.setAttribute("cart", cart);

    return new ModelAndView("redirect:/cart"); // リダイレクト
  }

  /**
   * カートの内容を表示する(select)
   *
   * @param mav the mav
   * @return the model and view
   */
  @RequestMapping(value = "/cart", method = RequestMethod.GET) // URLとのマッピング
  public ModelAndView cart(ModelAndView mav) {

    // セッションからカートを取得し、テンプレートに渡す
    Cart cart = getCart();
    mav.addObject("orderList", cart.getOrderList());

    // 商品一覧をテンプレートに渡す。※商品名、価格を表示するため
    List<Map<String, Object>> itemListMap = listService.getItemList();
    mav.addObject("itemList", itemListMap);

    // 合計金額計算
    int totalPrice = 0;
    int iPrice = 0;
    for (Order order : cart.getOrderList()) {
      System.out.println(order);
      iPrice = 0;
      iPrice = Integer.parseInt(itemListMap.get(order.getItemId()).get("price").toString());
      totalPrice += iPrice  * order.getNum();
    }
    mav.addObject("totalPrice", totalPrice);

    // Viewのテンプレート名設定
    mav.setViewName("CartView");

    return mav;
  }

  /**
   * 新規追加する(insert)
   *
   * @param mav the mav
   * @return the model and view
   */
  @Autowired
  JdbcTemplate jdbcTemplate;
  @RequestMapping(value = "/insert", method = RequestMethod.POST) // URLとのマッピング
  public ModelAndView insert(ModelAndView mav, InsertForm InsertForm,BindingResult bindingResult) {
    if (bindingResult.getAllErrors().size() > 0) {
      //初期表示時
      List<Map<String, Object>> itemListMap = listService.getItemList();
      mav.addObject("itemList", itemListMap);

      // Viewのテンプレート名を設定
      mav.setViewName("ListView");
    }

    String sSql = "";
    sSql  = " INSERT INTO items (";
    sSql += " item_id, ";
    sSql += " item_name, ";
    sSql += " price ";
    sSql += " ) SELECT ";
    sSql += " max(item_id+1), ";
    sSql += " '" + InsertForm.getInput_name() + "' ,";
    sSql += " " + InsertForm.getInput_price() + " ";
    sSql += " FROM items ";

    jdbcTemplate.update(sSql);

    // 商品一覧画面を表示
    System.out.println("登録したので更新します");

    List<Map<String, Object>> itemListMap = listService.getItemList();
    mav.addObject("itemList", itemListMap);

    // Viewのテンプレート名を設定
    mav.setViewName("ListView");

    return mav;

  }

  /**
   * 注文内容削除する(delete)
   *
   * @param mav the mav
   * @param deleteForm the delete form
   * @param bindingResult the binding result
   * @return the model and view
   */
  @RequestMapping(value = "/cart", method = RequestMethod.POST) // URLとのマッピング
  public ModelAndView deleteOrder(ModelAndView mav, @Validated DeleteForm deleteForm,
      BindingResult bindingResult) {

    if (bindingResult.getAllErrors().size() == 0) {
      //リクエストパラメータにエラーがなければ、削除処理行う

      // カートから商品を削除
      Cart cart = getCart();
      cart.deleteOrder(deleteForm.getItemId());

      // データをセッションへ保存
      session.setAttribute("cart", cart);
    }

    return new ModelAndView("redirect:/cart"); // リダイレクト
  }

  /**
   * セッションからカートを取得する
   *
   * @return the cart
   */
  private Cart getCart() {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null) {
      cart = new Cart();
      session.setAttribute("cart", cart);
    }

    return cart;
  }

}
