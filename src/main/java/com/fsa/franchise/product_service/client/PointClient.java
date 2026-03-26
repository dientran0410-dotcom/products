package com.fsa.franchise.product_service.client;

import com.fsa.franchise.product_service.dto.request.AddPointRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "engagment-service", url = "https://microservice-i7nc.onrender.com")
public interface PointClient {

    @PostMapping("/api/points/add")
    void addPoints(@RequestBody AddPointRequest request);
}
