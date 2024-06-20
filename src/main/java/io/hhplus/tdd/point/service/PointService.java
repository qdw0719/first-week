package io.hhplus.tdd.point.service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.hhplus.tdd.custom.CustomException;
import org.springframework.stereotype.Service;

import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

@Service
public class PointService {

	private final UserPointRepository userPointRepository;
	private final PointHistoryRepository pointHistoryRepository;

	// 동시성 제어를 위한 ReentrantLock
	private final Lock lock = new ReentrantLock();

	public PointService(UserPointRepository userPointRepository, PointHistoryRepository pointHistoryRepository) {
		this.userPointRepository = userPointRepository;
		this.pointHistoryRepository = pointHistoryRepository;
	}

	// 유저의 현재 포인트 조회
	public UserPoint getUserPoint(long userId) throws CustomException {
		UserPoint userPoint = userPointRepository.selectById(userId);
		if (userPoint.point() == 0L) {
			throw new CustomException(String.format("아이디가 [%d]에 해당하는 유저는 존재하지 않습니다.", userId));
		}
		return userPoint;
	}

	// 조회 할 유저의 포인트 히스토리를 조회
	public List<PointHistory> getPointHistories(long userId) throws CustomException {
		List<PointHistory> pointHistories = pointHistoryRepository.selectAllByUserId(userId);

		if (pointHistories.isEmpty()) {
			throw new CustomException("조회 결과가 없습니다.");
		}

		return pointHistories;
	}

	// 포인트 충전
	public UserPoint chargePoints(long userId, long amount) throws CustomException {
		// 동시성 제어 시작, lock 획득
		lock.lock();

		try {
			if (amount <= 0) {
				throw new CustomException("0포인트 이하는 충전 할 수 없습니다.");
			}

			// 파라미터로 받은 유저의 point를 조회하여 추가 충전될 포인트 양을 더해준다.
			UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, getUserPoint(userId).point() + amount);

			// history테이블에 저장
			pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
			return updatedUserPoint;
		} finally {
			lock.unlock(); // exception 발생 시에도 lock 해제 보장
		}
	}

	// 포인트 사용
	public UserPoint usePoints(long userId, long amount) throws CustomException {
		// 동시성 제어 시작, lock 획득
		lock.lock();

		try {
			if (amount <= 0) {
				throw new CustomException("0포인트 이상만 사용할 수 있습니다.");
			}

			UserPoint currentUserPoint = getUserPoint(userId);

			// 파라미터로 받은 유저의 현재 point가 차감될 포인트보다 적다면 exception
			if (currentUserPoint.point() < amount) {
				throw new CustomException(String.format("사용자 %d의 포인트가 부족합니다. 현재 포인트: %d", userId, currentUserPoint.point()));
			}

			// 파라미터로 받은 유저의 현재 point가 차감될 포인트보다 많으면 차감
			UserPoint updatedUserPoint = userPointRepository.insertOrUpdate(userId, currentUserPoint.point() - amount);

			// history테이블에 저장
			pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
			return updatedUserPoint;
		} finally {
			lock.unlock(); // exception 발생 시에도 lock 해제 보장
		}
	}
}
