package com.fsa.franchise.product_service.client;

import com.fsa.franchise.product_service.model.CreateMomoRequest;
import com.fsa.franchise.product_service.model.CreateMomoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "momo", url = "${momo.end-point}")
public interface MomoApi {

    @PostMapping("/create")
    CreateMomoResponse createMomo(@RequestBody CreateMomoRequest request);
}