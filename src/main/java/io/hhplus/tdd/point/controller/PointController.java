package io.hhplus.tdd.point.controller;

import java.util.List;

import io.hhplus.tdd.custom.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointService;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{id}")
    public UserPoint point(@PathVariable("id") long id) throws CustomException {
        return pointService.getUserPoint(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("/{id}/histories")
    public List<PointHistory> history(@PathVariable("id") long id) throws CustomException {
        return pointService.getPointHistories(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("/{id}/charge")
    public UserPoint charge(@PathVariable("id") long id, @RequestBody PointHistory pointHistory) throws CustomException {
        return pointService.chargePoints(id, pointHistory.amount());
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("/{id}/use")
    public UserPoint use(@PathVariable("id") long id, @RequestBody PointHistory pointHistory) throws CustomException {
        return pointService.usePoints(id, pointHistory.amount());
    }
}
