package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import com.gghiaroni.rabbitride.carservice.car.dto.PagedResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/carros")
public class CarroController {
    private final CarroService carroService;

    public CarroController(CarroService carroService) {
        this.carroService = carroService;
    }

    @GetMapping
    public ResponseEntity<PagedResult<CarroResponse>> listar(
        @RequestParam(required = false) StatusCarro status,
        @PageableDefault(size = 20) Pageable pageable
    ){
        return ResponseEntity.ok(carroService.listar(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarroResponse> buscarPorId(@PathVariable UUID id){
        return ResponseEntity.ok(carroService.buscarPorId(id));
    }
}
