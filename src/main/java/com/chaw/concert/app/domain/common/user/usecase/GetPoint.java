package com.chaw.concert.app.domain.common.user.usecase;

import com.chaw.concert.app.domain.common.user.entity.Point;
import com.chaw.concert.app.domain.common.user.repository.PointRepository;
import org.springframework.stereotype.Service;

@Service
public class GetPoint {

    private final PointRepository pointRepository;

    public GetPoint(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public Output execute(Input input) {
        Point point = pointRepository.findByUserId(input.userId());
        if (point == null) {
            point = Point.builder()
                    .userId(input.userId())
                    .balance(0)
                    .build();
            pointRepository.save(point);
        }
        return new Output(point.getBalance());
    }

    public record Input(
            Long userId
    ) {}

    public record Output(
            Integer point
    ) {}
}
