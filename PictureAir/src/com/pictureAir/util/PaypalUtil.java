package com.pictureAir.util;

import java.math.BigDecimal;

import com.paypal.android.sdk.payments.PayPalItem;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalPaymentDetails;
import com.paypal.android.sdk.payments.ShippingAddress;

public class PaypalUtil {
	//当商品只有一件的时候，调用这个函数
	public static PayPalPayment getThingToBuy(String paymentIntent, String nameString) {
		return new PayPalPayment(new BigDecimal("0.00"), "USD", nameString, paymentIntent);
	}
	
	/* 
	 * 当商品有多件的时候，调用这个函数
	 */
	public static PayPalPayment getStuffToBuy(String paymentIntent, String nameString) {
		//--- include an item list, payment amount details
		PayPalItem[] items =
			{
				new PayPalItem("old jeans with holes", 1, new BigDecimal("0.01"), "USD",
						"sku-12345678"),
						new PayPalItem("free rainbow patch", 1, new BigDecimal("0.00"),
								"USD", "sku-zero-price"),
								new PayPalItem("long sleeve plaid shirt (no mustache included)", 1, new BigDecimal("0.00"),
										"USD", "sku-33333") 
			};
		BigDecimal subtotal = PayPalItem.getItemTotal(items);
		BigDecimal shipping = new BigDecimal("0.00");
		BigDecimal tax = new BigDecimal("0.00");
		PayPalPaymentDetails paymentDetails = new PayPalPaymentDetails(shipping, subtotal, tax);
		BigDecimal amount = subtotal.add(shipping).add(tax);
		PayPalPayment payment = new PayPalPayment(amount, "USD", nameString, paymentIntent);
		payment.items(items).paymentDetails(paymentDetails);

		//--- set other optional fields like invoice_number, custom field, and soft_descriptor
		payment.custom("This is text that will be associated with the payment that the app can use.");

		return payment;
	}
	
	
}
