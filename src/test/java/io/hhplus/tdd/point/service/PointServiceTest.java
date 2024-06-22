package io.hhplus.tdd.point.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.hhplus.tdd.custom.CustomException;
import io.hhplus.tdd.repository.PointHistoryRepository;
import io.hhplus.tdd.repository.UserPointRepository;
import io.hhplus.tdd.repository.PointHistoryRepositoryImpl;
import io.hhplus.tdd.repository.UserPointRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointServiceTest {

	private UserPointRepository userPointRepository;
	private PointHistoryRepository pointHistoryRepository;
	private PointService pointService;

	@BeforeEach
	void setup() {
		userPointRepository = new UserPointRepositoryImpl(new UserPointTable());
		pointHistoryRepository = new PointHistoryRepositoryImpl(new PointHistoryTable());
		pointService = new PointService(userPointRepository, pointHistoryRepository);

		// 기본 세팅, User 임의 생성
		userPointRepository.insertOrUpdate(1L, 20L);
		userPointRepository.insertOrUpdate(2L, 100L);
		userPointRepository.insertOrUpdate(3L, 50L);
	}

	/***
	 *  포인트 조회에 대한 fail Test
	 *
	 * 	 user가 없을 때 exception 발생
	 *
	 * 	 여기서 유저가 없다를 판단하는 기준은 아래와 같습니다.
	 * 	 1. UserPointRepository 에 id가 없을 시 무조건 default(userId, point = 0L) 값으로 return 되기 때문에
	 * 	 	point가 0일 경우 유저가 없다고 판단
	 * 	 2. 임시유저 생성 할 때에도 0포인트는 넣어주지 않음
	 *
	 * 	 사용자용 exception message 출력
	 *
	 * 	 @throws CustomException
	 * */

	@Test
	void getUserPointFailTest() throws CustomException {
		// CustomException 발생해야 함
		// io.hhplus.tdd.custom.CustomException: 아이디가 [5]에 해당하는 유저는 존재하지 않습니다.
//		UserPoint result = pointService.getUserPoint(5L);
//		assertEquals(0L, result.point());

		assertThrows(CustomException.class, () -> pointService.getUserPoint(5L));
	}

	// 현재 포인트 조회
	// Success Test
	@Test
	void getUserPointSuccessTest() throws CustomException {
		UserPoint result = pointService.getUserPoint(1L);
		assertEquals(20L, result.point());

		result = pointService.getUserPoint(2L);
		assertEquals(100L, result.point());
	}

	/**
	 * 포인트 충전에 대한 fail Test
	 *
	 * 0포인트 이하는 충전 못하게 exception 발생
	 * 사용자용 exception message 출력
	 *
	 * @throws CustomException
	 */
	@Test
	void chargeFailTest() throws CustomException {
		// CustomException 발생해야 함
		// io.hhplus.tdd.custom.CustomException: 0포인트 이하는 충전 할 수 없습니다.
//		UserPoint result = pointService.chargePoints(1L, 0L);
//		assertEquals(20L, result.point());

		assertThrows(CustomException.class, () -> pointService.chargePoints(1L, 0L));
	}

	// 포인트 충전
	// Success Test
	@Test
	void chargeSuccessTest() throws CustomException {
		// 10포인트 충전
		UserPoint result = pointService.chargePoints(1L, 10L);
		assertEquals(30L, result.point());

		// 10포인트 추가 충전
		result = pointService.chargePoints(1L, 10L);
		assertEquals(40L, result.point());
	}

	/**
	 * 포인트 사용에 대한 fail Test
	 * 차감 포인트가 보유 포인트보다 많을 때 exception 발생
	 * 현재포인트와 사용자용 exception message 출력
	 * */
	@Test
	void usePointsFailTest() throws CustomException {
		// CustomException 발생해야 함
		// io.hhplus.tdd.custom.CustomException: 사용자 1의 포인트가 부족합니다. 현재 포인트: 10
//		UserPoint result = pointService.usePoints(1L, 50L);
//		assertEquals(0L, result.point());

		// 에러발생 체크
		assertThrows(CustomException.class, () -> pointService.usePoints(1L, 50L));
	}

	/**
	 * 포인트 사용에 대한 fail Test
	 * 0 이하의 포인트를 사용하려 할 때 exception 발생
	 * 현재포인트와 사용자용 exception message 출력
	 * */
	@Test
	void userPointsFailTest2() throws CustomException {
		// CustomException 발생해야 함
		// io.hhplus.tdd.custom.CustomException: 0포인트 이상만 사용할 수 있습니다.
//		UserPoint result = pointService.usePoints(1L, 0);

		// 에러발생 체크
		assertThrows(CustomException.class, () -> pointService.usePoints(1L, 0));
	}

	// 포인트 사용
	// Success Test
	@Test
	void usePointsSuccessTest() throws CustomException {
		UserPoint result = pointService.usePoints(1L, 5L);
		assertEquals(15L, result.point());
	}

	/**
	 * 히스토리 조회에 대한 fail test
	 * 조회할 내역이 없을 때 exception 발생
	 * 사용자용 exception message 출력
	 * */
	@Test
	void getPointHistoriesFailTest() throws CustomException {
		// CustomException 발생해야 함
		// io.hhplus.tdd.custom.CustomException: 조회 결과가 없습니다.
//		List<PointHistory> result = pointService.getPointHistories(1L);

//		// histry size 및 최종 유저의 current point 체크
//		assertEquals(0, result.size());
//
//		UserPoint currentPoint = pointService.getUserPoint(1L);
//		assertEquals(0L, currentPoint.point());

		assertThrows(CustomException.class, () -> pointService.getPointHistories(1L));
	}

	// 히스토리 조회
	// Success Test
	@Test
	void getPointHistoriesSuccessTest() throws CustomException {
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

		// --------------------------------------- 잔여 35 point

		// history 확인
		List<PointHistory> result = pointService.getPointHistories(1L);
		result.forEach(System.out::println);

		// history size 및 최종 유저의 current point 체크
		assertEquals(9, result.size());

		UserPoint currentPoint = pointService.getUserPoint(1L);
		assertEquals(35L, currentPoint.point());
	}

	// 포인트 충전 동시성 테스트
	@Test
	void chargeConcurrencyTest() throws CustomException, InterruptedException, ExecutionException {
		int threadCount = 10;
		long chargeAmount = 1L;
		long userId = 1L;

		// 고정크기 멀티쓰레드 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		List<Callable<UserPoint>> tasks = new ArrayList<>();

		// 쓰레드 수 만큼 포인트 충전 시키기 위해 tasks에 담아두기
		for (int i = 0, len = threadCount; i < len; i++) {
			tasks.add(() -> pointService.chargePoints(userId, chargeAmount));
		}

		// tasks에 담긴 만큼 작업
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			// 포인트 쌓이는거 확인
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}

		UserPoint userPoint = pointService.getUserPoint(userId);
		// 변경된 user의 포인트와
		// 임시 생성할 때의 유저 포인트 + (쓰레드 수 (요청 수) * 충전량)
		assertEquals(userPoint.point(), 20L + (threadCount * chargeAmount));

		// 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories = pointService.getPointHistories(userId);
		pointHistories.forEach(System.out::println);
	}

	// 포인트 소모 동시성 테스트
	@Test
	void useConcurrencyTest() throws CustomException, InterruptedException, ExecutionException {
		int threadCount = 10;
		long useAmount = 1L;
		long userId = 2L;

		// 고정크기 쓰레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		List<Callable<UserPoint>> tasks = new ArrayList<>();

		// 쓰레드 수 만큼 포인트 소모 시키기 위해 tasks에 담아두기
		for (int i = 0, len = threadCount; i < len; i++) {
			tasks.add(() -> pointService.usePoints(userId, useAmount));
		}

		// tasks에 담긴 만큼 작업
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			// 포인트 차감되는거 확인
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}

		UserPoint userPoint = pointService.getUserPoint(userId);
		// 변경된 user의 포인트와
		// 임시 생성할 때의 유저 포인트 - (쓰레드 수 (요청 수) * 충전량)
		assertEquals(userPoint.point(), 100L - (threadCount * useAmount));

		// 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories = pointService.getPointHistories(userId);
		pointHistories.forEach(System.out::println);
	}

	// 포인트 충전 동시성 테스트(유저 여러명)
	@Test
	void multiUserChargeTest() throws CustomException, InterruptedException, ExecutionException {
		int threadCount = 10;
		long chargeAmount = 1L;

		// 고정 크기 스레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		List<Callable<UserPoint>> tasks = new ArrayList<>();
		// 유저 3명 반복
		for (int i = 1; i <= 3; i++) {
			// 유저 ID
			long userId = i;
			// 쓰레드 수 만큼 포인트 충전 시키기 위해 tasks에 담아두기
			for (int j = 0; j < threadCount; j++) {
				tasks.add(() -> pointService.chargePoints(userId, chargeAmount));
			}
		}

		// tasks에 담긴 만큼 작업
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			// 포인트 쌓이는거 확인
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}

		// 각 유저의 최종 포인트 검증
		// 임시 생성할 때의 유저 포인트 - (쓰레드 수 (요청 수) * 충전량)
		assertEquals(pointService.getUserPoint(1L).point(), 20L + (threadCount * chargeAmount));
		assertEquals(pointService.getUserPoint(2L).point(), 100L + (threadCount * chargeAmount));
		assertEquals(pointService.getUserPoint(3L).point(), 50L + (threadCount * chargeAmount));

		// 유저별로 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories1 = pointService.getPointHistories(1L);
		System.out.println("============================== User 1 ==============================");
		pointHistories1.forEach(System.out::println);

		List<PointHistory> pointHistories2 = pointService.getPointHistories(2L);
		System.out.println("============================== User 2 ==============================");
		pointHistories2.forEach(System.out::println);

		List<PointHistory> pointHistories3 = pointService.getPointHistories(3L);
		System.out.println("============================== User 3 ==============================");
		pointHistories3.forEach(System.out::println);
	}

	// 포인트 소모 동시성 테스트(유저 여러명)
	@Test
	void multiUseConcurrencyTest() throws CustomException, InterruptedException, ExecutionException {
		int threadCount = 10;
		long useAmount = 1L;

		// 고정크기 쓰레드 풀 생성
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		List<Callable<UserPoint>> tasks = new ArrayList<>();

		// 유저 3명 반복
		for (int i = 1; i <= 3; i++) {
			// 유저 ID
			long userId = i;
			// 쓰레드 수 만큼 포인트 소모 시키기 위해 tasks에 담아두기
			for (int j = 0; j < threadCount; j++) {
				tasks.add(() -> pointService.usePoints(userId, useAmount));
			}
		}

		// tasks에 담긴 만큼 작업
		List<Future<UserPoint>> futures = executorService.invokeAll(tasks);
		for (Future<UserPoint> future : futures) {
			// 각 작업결과 반환
			UserPoint userPoint = future.get();
			// 포인트 차감되는거 확인
			System.out.println(String.format("userPoint >> %d", userPoint.point()));
		}

		// 임시 생성할 때의 유저 포인트 - (쓰레드 수 (요청 수) * 충전량)
		// 각 유저의 최종 포인트 검증
		assertEquals(pointService.getUserPoint(1L).point(), 20L - (threadCount * useAmount));
		assertEquals(pointService.getUserPoint(2L).point(), 100L - (threadCount * useAmount));
		assertEquals(pointService.getUserPoint(3L).point(), 50L - (threadCount * useAmount));

		// 유저별로 히스토리에 잘 적재되었는지 확인
		List<PointHistory> pointHistories1 = pointService.getPointHistories(1L);
		System.out.println("============================== User 1 ==============================");
		pointHistories1.forEach(System.out::println);

		List<PointHistory> pointHistories2 = pointService.getPointHistories(2L);
		System.out.println("============================== User 2 ==============================");
		pointHistories2.forEach(System.out::println);

		List<PointHistory> pointHistories3 = pointService.getPointHistories(3L);
		System.out.println("============================== User 3 ==============================");
		pointHistories3.forEach(System.out::println);
	}
}
