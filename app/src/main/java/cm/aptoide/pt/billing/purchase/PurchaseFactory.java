package cm.aptoide.pt.billing.purchase;

public class PurchaseFactory {

  public Purchase create(long productId, String signature, String signatureData,
      Purchase.Status status, String sku) {
    return new Purchase(status, productId, sku, signature, signatureData);
  }
}