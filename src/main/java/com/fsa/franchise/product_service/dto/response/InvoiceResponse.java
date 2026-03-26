package com.fsa.franchise.product_service.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InvoiceResponse {
    private UUID id;
    private List<InvoiceItemResponse> items;
}
