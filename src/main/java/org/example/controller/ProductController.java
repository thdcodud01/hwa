package org.example.controller;


import org.example.Container;
import org.example.dto.Product;
import org.example.service.ProductService;
import org.example.util.DBUtil;
import org.example.util.SecSql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.Container.rq;
import static org.example.Container.scanner;

public class ProductController {

  private ProductService productService;

  public ProductController() {
    productService = Container.productService;
  }

  public void write() {
    if(Container.session.isLogined() == false) {
      System.out.println("로그인 후 이용해주세요.");
      return;
    }
// care_id, type_id, product_name, product_brand, product_capacity, product_price, product_explanation
    System.out.println("== 상품 등록 ==");
    System.out.printf("상품명  : ");
    String product_name = Container.scanner.nextLine();
    System.out.printf("상품브랜드  : ");
    String product_brand = Container.scanner.nextLine();
    System.out.printf("상품용량  : ");
    String product_capacity = Container.scanner.nextLine();
    System.out.printf("상품가격  : ");
    String product_price = Container.scanner.nextLine();
    System.out.printf("상품설명  : ");
    String product_explanation = Container.scanner.nextLine();

    int member_email = Container.session.loginedMemberId;
    int id = productService.write(product_name, product_brand, product_capacity, product_price, product_explanation);

    System.out.printf("%d번 상품이 등록되었습니다.\n", id);
  }
  public void search() {
    System.out.println("== 상품 검색 ==");

    // 케어 그룹 검색
    System.out.println("[케어그룹 리스트]");
    SecSql sql = new SecSql();
    sql.append("SELECT A.care");
    sql.append("FROM care AS A");

    List<Map<String, Object>> careList = DBUtil.selectRows(Container.conn, sql);

    for (int i = 0; i < careList.size(); i++) {
      System.out.printf("%s", careList.get(i).get("care"));
      if (i < careList.size() - 1) System.out.printf(", ");
      if (i == careList.size() - 1) System.out.printf("\n");
    }

    String careName;

    while (true) {
      System.out.printf("케어그룹을 입력해주세요: ");
      careName = Container.scanner.nextLine().trim();

      boolean isInCareList = false;

      for (Map<String, Object> careMap : careList) {
        if (careName.equals(careMap.get("care"))) isInCareList = true;
      }

      if (isInCareList) break;

      System.out.println("제대로된 케어그룹을 입력해주세요");
    }

    // 피부타입 검색
    System.out.println("[피부타입 리스트]");
    sql = new SecSql();
    sql.append("SELECT A.`type` as 'skin_type'");
    sql.append("FROM `type` AS A");

    List<Map<String, Object>> skinTypeList = DBUtil.selectRows(Container.conn, sql);

    for (int i = 0; i < skinTypeList.size(); i++) {
      System.out.printf("%s", skinTypeList.get(i).get("skin_type"));
      if (i < skinTypeList.size() - 1) System.out.printf(", ");
      if (i == skinTypeList.size() - 1) System.out.printf("\n");
    }

    String skinTypeName;

    while (true) {
      System.out.printf("피부타입을 입력해주세요: ");
      skinTypeName = Container.scanner.nextLine().trim();

      boolean isInSkinTypeList = false;

      for (Map<String, Object> skinTypeMap : skinTypeList) {
        if (skinTypeName.equals(skinTypeMap.get("skin_type"))) isInSkinTypeList = true;
      }

      if (isInSkinTypeList) break;

      System.out.println("제대로된 피부타입을 입력해주세요");
    }

    sql = new SecSql();
    sql.append("SELECT product.id, `care`.`care`, `type`.`type` AS 'skin_type', product.product_name, product.product_brand, product.product_capacity, product.product_price, product.product_explanation");
    sql.append("FROM product");
    sql.append("INNER JOIN `care` on product.care_id = `care`.id");
    sql.append("INNER JOIN `type` on product.type_id = `type`.id");
    sql.append("WHERE `care`.`care` = ? && `type`.`type` = ?", careName, skinTypeName);

    List<Map<String, Object>> searchedProductMapList = DBUtil.selectRows(Container.conn, sql);
    List<Product> searchedProductList = new ArrayList<>();

    for (Map<String, Object> searchedProductMap : searchedProductMapList) {
      searchedProductList.add(new Product(searchedProductMap));
    }

    System.out.printf("케어그룹: %s, 피부타입: %s 에 대한 검색 결과...\n\n", careName, skinTypeName);
    System.out.println("번호 / 이름 / 브랜드 / 용량 / 가격 / 설명");
    System.out.println("-".repeat(60));
    for (Product product : searchedProductList) {
      System.out.printf("%d / %s / %s / %s / %s / %s\n", product.getId(), product.getProduct_name(), product.getProduct_brand(), product.getProduct_capacity(), product.getProduct_price(), product.getProduct_explanation());
    }

    System.out.printf("세부정보를 확인 할 상품번호를 입력해주세요: ");
    int product_id = scanner.nextInt();
    Product searchedProduct = null;
    for (Product product : searchedProductList) {
      if (product.getId() == product_id) {
        searchedProduct = product;
      }
    }

    if (searchedProduct == null) {
      System.out.println("검색되지 않은 상품번호입니다.");
    }

    Container.session.setSessionProduct(searchedProduct);
  }
//  public void showList() {
//    System.out.println("== 상품 리스트 ==");
//    int page = rq.getIntParam("page", 1);
//    String searchKeyword = rq.getParam("searchKeyword", "");
//    int pageItemCount = 10;
//
//
//
//    // 임시
//    pageItemCount = 5;
//
////    List<Product> articles = productService.getForPrintArticleById(page, pageItemCount, searchKeyword);
//
//    if (articles.isEmpty()) {
//      System.out.println("상품이 존재하지 않습니다.");
//      return;
//    }
//
//    System.out.println("등록일 / 작성자 / 상품명 / 상품브랜드 / 상품용량 / 상품가격 / 상품설명");
//
//    for (Product article : articles) {
//      System.out.printf("%s / %s / %s / %s / %s / %s / %s / %s\n", article.id, article.regDate, article.extra__writerName, article.product_name, article.product_brand, article.product_capacity, article.product_price, article.product_explanation);
//    }
//  }

  public void showDetail() {
    Product product = Container.session.getSessionProduct();
    System.out.printf("번호 : %d\n", product.getId());
   // System.out.printf("등록일 : %s\n", article.regDate);
//    System.out.printf("작성자 : %s\n", article.extra__writerName);
    System.out.printf("상품명 : %s\n", product.getProduct_name());
    System.out.printf("상품브랜드 : %s\n", product.getProduct_brand());
    System.out.printf("상품용량 : %s\n", product.getProduct_capacity());
    System.out.printf("상품가격 : %s\n", product.getProduct_price());
    System.out.printf("상품설명 : %s\n", product.getProduct_explanation());
  }

  public void modify() {
    if(Container.session.isLogined() == false) {
      System.out.println("로그인 후 이용해주세요.");
      return;
    }

//    int id = rq.getIntParam("id", 0);
//
//    if (id == 0) {
//      System.out.println("id를 올바르게 입력해주세요.");
//      return;
//    }

    List<Map<String, Object>> modifiedProductMapList = DBUtil.selectRows(Container.conn, sql);
    List<Product> modifiedProductList = new ArrayList<>();

    for (Map<String, Object> modifiedProductMap : modifiedProductMapList) {
      modifiedProductList.add(new Product(modifiedProductMap));
    }

    System.out.println("수정할 상품 번호를 입력해주세요.");
    int product_id = scanner.nextInt();
    for (Product product : modifiedProductList) {
      if (product.getId() == product_id) {
        searchedProduct = product;
      }

    Product product = productService.getProductById(id);

    boolean productExists = productService.productExists(id);

    if (productExists == false) {
      System.out.printf("%d번 상품은 존재하지 않습니다.\n", id);
      return;
    }

//    if(article.member_id != Container.session.loginedMemberId) {
//      System.out.println("권한이 없습니다");
//      return;
//    }

    System.out.printf("상품명  : ");
    String product_name = Container.scanner.nextLine();
    System.out.printf("상품브랜드  : ");
    String product_brand = Container.scanner.nextLine();
    System.out.printf("상품용량  : ");
    String product_capacity = Container.scanner.nextLine();
    System.out.printf("상품가격  : ");
    String product_price = Container.scanner.nextLine();
    System.out.printf("상품설명  : ");
    String product_explanation = Container.scanner.nextLine();
    productService.update(product_name, product_brand, product_capacity, product_price, product_explanation);

    System.out.printf("%d번 상품이 수정되었습니다.\n", id);
  }

  public void delete() {
    if(Container.session.isLogined() == false) {
      System.out.println("로그인 후 이용해주세요.");
      return;
    }

    int id = rq.getIntParam("id", 0);

    if (id == 0) {
      System.out.println("id를 올바르게 입력해주세요.");
      return;
    }

    System.out.printf("== %d번 상품 삭제 ==\n", id);

    Product article = productService.getProductById(id);

    boolean articleExists = productService.articleExists(id);

    if (articleExists == false) {
      System.out.printf("%d번 상품은 존재하지 않습니다.\n", id);
      return;
    }

//    if(article.member_id != Container.session.loginedMemberId) {
//      System.out.println("권한이 없습니다");
//      return;
//    }

    productService.delete(id);

    System.out.printf("%d번 상품이 삭제되었습니다.\n", id);
  }

}
