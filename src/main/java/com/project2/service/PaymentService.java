package com.project2.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@SuppressWarnings("null")
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public String createRazorpayOrder(BigDecimal amount, String orderId) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue()); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "order_rcptid_" + orderId);

        Order order = razorpay.orders.create(orderRequest);
        return order.get("id");
    }
}
