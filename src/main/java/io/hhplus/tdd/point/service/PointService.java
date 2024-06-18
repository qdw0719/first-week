package io.hhplus.tdd.point.service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;

@Service
public class PointService {

	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	// 동시성 제어를 위한 ReentrantLock
	private final Lock lock = new ReentrantLock();

	public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
		this.userPointTable = userPointTable;
		this.pointHistoryTable = pointHistoryTable;
	}

	// 유저의 현재 포인트 조회
	public UserPoint getUserPoint(long userId) {
		return userPointTable.selectById(userId);
	}

	// 조회 할 유저의 포인트 히스토리를 조회
	public List<PointHistory> getPointHistories(long userId) {
		return pointHistoryTable.selectAllByUserId(userId);
	}

	// 포인트 충전
	public UserPoint chargePoints(long userId, long amount) throws Exception {
		// 동시성 제어 시작, lock 획득
		lock.lock();

		try {
			if (amount <= 0) {
				throw new Exception("0원 이하는 충전 할 수 없습니다.");
			}

			// 파라미터로 받은 유저의 point를 조회하여 추가 충전될 포인트 양을 더해준다.
			UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, getUserPoint(userId).point() + amount);

			// history테이블에 저장
			pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());
			return updatedUserPoint;
		} finally {
			lock.unlock(); // exception 발생 시에도 lock 해제 보장
		}
	}

	// 포인트 사용
	public UserPoint usePoints(long userId, long amount) throws Exception {
		// 동시성 제어 시작, lock 획득
		lock.lock();

		try {
			if (amount <= 0) {
				throw new Exception("0원 이상만 사용할 수 있습니다.");
			}

			UserPoint currentUserPoint = getUserPoint(userId);

			// 파라미터로 받은 유저의 현재 point가 차감될 포인트보다 적다면 exception
			if (currentUserPoint.point() < amount) {
				throw new Exception(String.format("사용자 %s의 포인트가 부족합니다. 현재 포인트: %d", String.valueOf(userId), currentUserPoint.point()));
			}

			// 파라미터로 받은 유저의 현재 point가 차감될 포인트보다 많으면 차감
			UserPoint updatedUserPoint = userPointTable.insertOrUpdate(userId, currentUserPoint.point() - amount);

			// history테이블에 저장
			pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());
			return updatedUserPoint;
		} finally {
			lock.unlock(); // exception 발생 시에도 lock 해제 보장
		}
	}
}
