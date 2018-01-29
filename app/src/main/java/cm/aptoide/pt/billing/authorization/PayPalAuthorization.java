/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 10/08/2016.
 */

package cm.aptoide.pt.billing.authorization;

import cm.aptoide.pt.billing.Price;

public class PayPalAuthorization extends Authorization {

  private final String payKey;
  private final Price price;
  private final String productDescription;
  private final long transactionId;

  public PayPalAuthorization(long id, String customerId, Status status, String payKey, Price price,
      String description, String icon, String name, boolean defaultAuthorization, String type,
      String productDescription, long paymentMethodId, long transactionId) {
    super(id, customerId, status, icon, name, type, description, defaultAuthorization,
        paymentMethodId);
    this.payKey = payKey;
    this.price = price;
    this.productDescription = productDescription;
    this.transactionId = transactionId;
  }

  public Price getPrice() {
    return price;
  }

  public String getPayKey() {
    return payKey;
  }

  public String getProductDescription() {
    return productDescription;
  }

  public long getTransactionId() {
    return transactionId;
  }
}
