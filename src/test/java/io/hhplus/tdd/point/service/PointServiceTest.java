package io.hhplus.tdd.point.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointServiceTest {
	
	private UserPointTable userPointTable; 
	private PointHistoryTable pointHistoryTable;
	private PointService pointService;
    
	
	@BeforeEach
	void setup() {
		userPointTable = new UserPointTable();
		pointHistoryTable = new PointHistoryTable();
		pointService = new PointService(userPointTable, pointHistoryTable);
		
		
		// 기본 세팅, User 임의 생성
		userPointTable.insertOrUpdate(1L, 0L);
		userPointTable.insertOrUpdate(2L, 10L);
	}
	
	// 현재 포인트 조회
	@Test
	void getUserPointTest() {
		UserPoint result = pointService.getUserPoint(1L);
		assertEquals(0L, result.point());
		
		result = pointService.getUserPoint(2L);
		assertEquals(10L, result.point());
	}
	
	// 포인트 충전
	@Test
	void chargeTest() throws Exception {
		// 10포인트 충전
		UserPoint result = pointService.chargePoints(1L, 10L);
		assertEquals(10L, result.point());
		
		// 10포인트 추가 충전
		result = pointService.chargePoints(1L, 10L);
		assertEquals(20L, result.point());
	}
	
	// 포인트 사용
	@Test
	void usePointsTest() throws Exception {
		// 유저 1에대한 포인트 차감에러 확인
//		UserPoint result = pointService.usePoints(1L, 10L);
//		assertEquals(0L, result.point());
		
		// 에러발생 체크
		assertThrows(Exception.class, () -> pointService.usePoints(1L, 10L));
	}
	
	@Test
	void getPointHistoriesTest() throws Exception {
		// 유저 1에 포인트 충전 및 차감 진행
		pointService.chargePoints(1L, 10L);
		pointService.chargePoints(1L, 10L);
		pointService.usePoints(1L, 5L);
		pointService.chargePoints(1L, 10L);
		pointService.chargePoints(1L, 10L);
		pointService.usePoints(1L, 5L);
		pointService.usePoints(1L, 5L);
		pointService.usePoints(1L, 5L);
		pointService.usePoints(1L, 5L);
		
		// --------------------------------------- 잔여 15 point
		
		// history 확인
		List<PointHistory> result = pointService.getPointHistories(1L);
		result.forEach(System.out::println);
		
		// histry size 및 최종 유저의 current point 체크
        assertEquals(9, result.size());
        
        UserPoint currentPoint = pointService.getUserPoint(1L);
        assertEquals(15L, currentPoint.point());
	}
	
	
	// 포인트 충전 동시성 테스트
	@Test
	void multiChargeTest() throws InterruptedException, ExecutionException {
		int threadCount = 10;
		long chargeAmount = 100L;
		long userId = 1L;
		
		// 고정크기 멀티쓰레드 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		
		List<Callable<UserPoint>> tasks = new ArrayList<>();
		
		// 쓰레드 수 만큼 포인트 충전 시키기 위해 tasks에 담아두기
		for (int i = 0, len = threadCount; i < len; i++) {
			tasks.add(() -> {
				return pointService.chargePoints(userId, chargeAmount);
			});
		}
		
		// tasks에 담긴 만큼 작업 후 대기
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}
		
		UserPoint userPoint = pointService.getUserPoint(1L);
		assertEquals(userPoint.point(), (threadCount * chargeAmount));
		
		// 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories = pointService.getPointHistories(userId);
		pointHistories.forEach(System.out::println);
	}
	
	// 포인트 소모 동시성 테스트
	@Test
	void multiUseTest() throws InterruptedException, ExecutionException {
		int threadCount = 10;
		long chargeAmount = 1L;
		long userId = 2L;
		
		// 고정크기 멀티쓰레드 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		
		List<Callable<UserPoint>> tasks = new ArrayList<>();
		
		// 쓰레드 수 만큼 포인트 충전 시키기 위해 tasks에 담아두기
		for (int i = 0, len = threadCount; i < len; i++) {
			tasks.add(() -> {
				return pointService.usePoints(userId, chargeAmount);
			});
		}
		
		// tasks에 담긴 만큼 작업 후 대기
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}
		
		UserPoint userPoint = pointService.getUserPoint(1L);
		assertEquals(userPoint.point(), 10L - (threadCount * chargeAmount));
		
		// 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories = pointService.getPointHistories(userId);
		pointHistories.forEach(System.out::println);
	}
}















