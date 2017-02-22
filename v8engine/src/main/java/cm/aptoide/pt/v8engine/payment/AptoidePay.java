/*
 * Copyright (c) 2016.
 * Modified by Marcelo Benites on 12/08/2016.
 */

package cm.aptoide.pt.v8engine.payment;

import cm.aptoide.pt.v8engine.repository.PaymentAuthorizationFactory;
import cm.aptoide.pt.v8engine.repository.PaymentAuthorizationRepository;
import cm.aptoide.pt.v8engine.repository.PaymentConfirmationRepository;
import cm.aptoide.pt.v8engine.repository.PaymentRepository;
import cm.aptoide.pt.v8engine.repository.ProductRepository;
import java.util.List;
import rx.Completable;
import rx.Observable;
import rx.Single;

public class AptoidePay {

  private final PaymentConfirmationRepository confirmationRepository;
  private final PaymentAuthorizationRepository authorizationRepository;
  private final PaymentAuthorizationFactory authorizationFactory;
  private final PaymentRepository paymentRepository;
  private final ProductRepository productRepository;
  private final Payer payer;

  public AptoidePay(PaymentConfirmationRepository confirmationRepository,
      PaymentAuthorizationRepository authorizationRepository,
      PaymentAuthorizationFactory authorizationFactory, PaymentRepository paymentRepository,
      ProductRepository productRepository, Payer payer) {
    this.confirmationRepository = confirmationRepository;
    this.authorizationRepository = authorizationRepository;
    this.authorizationFactory = authorizationFactory;
    this.paymentRepository = paymentRepository;
    this.productRepository = productRepository;
    this.payer = payer;
  }

  public Observable<List<Payment>> payments() {
    return paymentRepository.getPayments();
  }

  public Observable<Payment> payment(int paymentId) {
    return paymentRepository.getPayment(paymentId);
  }

  public Completable initiate(Payment payment) {
    if (payment.getAuthorization().isAuthorized()
        || payment.getAuthorization().isInitiated()) {
      return Completable.complete();
    }
    return authorizationRepository.createPaymentAuthorization(payment.getId());
  }

  public Completable authorize(int paymentId) {
    return authorizationRepository.saveAuthorization(authorizationFactory.create(paymentId,
        Authorization.Status.PENDING, payer.getId()));
  }

  public Completable process(Payment payment, Product product) {
    return payment.process(product);
  }

  public Observable<PaymentConfirmation> confirmation(Product product) {
    return paymentRepository.getConfirmation(product);
  }

  public Single<Purchase> purchase(Product product) {
    return productRepository.getPurchase(product);
  }
}
