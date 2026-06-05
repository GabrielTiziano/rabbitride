package com.gghiaroni.rabbitride.carservice.car;

import com.gghiaroni.rabbitride.carservice.car.dto.CarroResponse;
import com.gghiaroni.rabbitride.carservice.car.dto.PagedResult;
import com.gghiaroni.rabbitride.carservice.car.exception.CarroNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CarroService {
    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    @Cacheable(value = "cars-list", key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PagedResult<CarroResponse> listar(StatusCarro status, Pageable pageable){
        Page<Carro> page = (status != null)
            ? carroRepository.findByStatus(status, pageable)
            : carroRepository.findAll(pageable);

        return PagedResult.from(page.map(CarroResponse::from));
    }


    @Cacheable(value = "cars-by-id", key = "#id")
    @Transactional(readOnly = true)
    public CarroResponse buscarPorId(UUID id){
        Carro carro = carroRepository.findById(id)
            .orElseThrow(() -> new CarroNotFoundException(id));
        return CarroResponse.from(carro);
    }
}
