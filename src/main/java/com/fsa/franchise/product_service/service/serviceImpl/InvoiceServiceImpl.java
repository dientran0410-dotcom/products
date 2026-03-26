package com.fsa.franchise.product_service.service.serviceImpl;

import com.fsa.franchise.product_service.dto.response.InvoiceItemResponse;
import com.fsa.franchise.product_service.dto.response.InvoiceResponse;
import com.fsa.franchise.product_service.entity.Cart;
import com.fsa.franchise.product_service.entity.CartItem;
import com.fsa.franchise.product_service.entity.Invoice;
import com.fsa.franchise.product_service.entity.InvoiceItem;
import com.fsa.franchise.product_service.repository.CartItemRepository;
import com.fsa.franchise.product_service.repository.CartRepository;
import com.fsa.franchise.product_service.repository.InvoiceItemRepository;
import com.fsa.franchise.product_service.repository.InvoiceRepository;
import com.fsa.franchise.product_service.service.CartService;
import com.fsa.franchise.product_service.service.InvoiceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    @Override
    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    @Override
    public Invoice createInvoice(UUID customerId) {

        Cart cart = cartService.getCartByCustomerId(customerId);

        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Invoice invoice = new Invoice();
        invoice.setCustomerId(customerId);
        invoice.setFranchiseId(1L);
        invoice.setStatus(Invoice.InvoiceStatus.PENDING_PAYMENT);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem item : items) {

            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            subtotal = subtotal.add(itemTotal);

            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setProductId(item.getProductId());
            invoiceItem.setPrice(item.getPrice());
            invoiceItem.setQuantity(item.getQuantity());
            invoiceItem.setTotal(itemTotal);

            invoiceItem.setInvoice(invoice);
            invoice.getItems().add(invoiceItem);
        }

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setPointsDiscount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setTotalAmount(subtotal);

        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice applyCouponCode(UUID invoiceId, String couponCode, BigDecimal discountPercent) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        BigDecimal discount = invoice.getSubtotal().multiply(discountPercent);

        invoice.setDiscountAmount(discount);
        invoice.setUpdatedAt(LocalDateTime.now());

        recalcTotal(invoice);

        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice applyPoints(UUID invoiceId, int points) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        BigDecimal discount = BigDecimal.valueOf(points)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        invoice.setPointsDiscount(discount);
        invoice.setUpdatedAt(LocalDateTime.now());

        recalcTotal(invoice);

        return invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse checkout(UUID invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Invoice already paid");
        }

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        invoice.setUpdatedAt(LocalDateTime.now());

        Cart cart = cartService.getCartByCustomerId(invoice.getCustomerId());
        cartItemRepository.deleteByCartId(cart.getId());

        invoiceRepository.save(invoice);

        List<InvoiceItemResponse> items = invoice.getItems().stream().map(item -> {
            InvoiceItemResponse res = new InvoiceItemResponse();
            res.setProductId(item.getProductId());
            res.setQuantity(item.getQuantity());
            res.setPrice(item.getPrice());
            return res;
        }).toList();

        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setItems(items);

        return response;
    }
    // public Invoice checkout(UUID invoiceId) {
    //
    // Invoice invoice = invoiceRepository.findById(invoiceId)
    // .orElseThrow(() -> new RuntimeException("Invoice not found"));
    //
    //
    // invoice.setStatus(Invoice.InvoiceStatus.PAID);
    // invoice.setUpdatedAt(LocalDateTime.now());
    //
    // Cart cart = cartService.getCartByCustomerId(invoice.getCustomerId());
    // cartItemRepository.deleteByCartId(cart.getId());
    //
    // return invoiceRepository.save(invoice);
    // }

    private void recalcTotal(Invoice invoice) {

        BigDecimal subtotal = nullSafe(invoice.getSubtotal());
        BigDecimal discount = nullSafe(invoice.getDiscountAmount());
        BigDecimal pointDiscount = nullSafe(invoice.getPointsDiscount());
        BigDecimal tax = nullSafe(invoice.getTaxAmount());

        BigDecimal total = subtotal
                .subtract(discount)
                .subtract(pointDiscount)
                .add(tax);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        invoice.setTotalAmount(total);
    }

    private BigDecimal nullSafe(BigDecimal value) {

        return value != null ? value : BigDecimal.ZERO;
    }

}
