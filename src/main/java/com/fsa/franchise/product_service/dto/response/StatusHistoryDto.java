package com.fsa.franchise.product_service.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryDto {
    private String status;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;
}
