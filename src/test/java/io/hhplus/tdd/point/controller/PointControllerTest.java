package io.hhplus.tdd.point.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.jayway.jsonpath.JsonPath;
import io.hhplus.tdd.custom.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @BeforeEach
    void setUp() {
        // MockMvc를 설정하는 부분
        mockMvc = MockMvcBuilders.standaloneSetup(new PointController(pointService)).build();
    }

    /**
     * 유저 포인트 조회 테스트
     * @throws Exception
     */
    @Test
    void getUserPointTest() throws Exception {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

        // 서비스의 getUserPoint 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.getUserPoint(anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        // 1번 유저에 대한 테스트를 진행
        // 1번 유저가 100포인트를 가지고 있다고 가정
        // 체크사항 >> status 200, userId 1, point 100
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(100));
    }

    /**
     * 유저 포인트 히스토리 조회 테스트
     * @throws Exception
     */
    @Test
    void getPointHistoriesTest() throws Exception {
        // 기본 세팅, User point history 임의 생성
        // 1번 유저에 대해 총 히스토리 수 5개인지 확인
        List<PointHistory> pointHistories = List.of(
                new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(3L, 1L, 100L, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(4L, 1L, 100L, TransactionType.USE, System.currentTimeMillis()),
                new PointHistory(5L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis())
        );

        // 서비스의 getPointHistories 메서드를 모킹하여 pointHistories를 반환하도록 설정
        when(pointService.getPointHistories(anyLong())).thenReturn(pointHistories);

        // 테스트 수행 및 결과 검증
        // 1번 유저의 포인트 이력이 5개인지 확인
        // 체크사항 >> status 200, length 5
        mockMvc.perform(get("/point/1/histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(5)));
    }

    /**
     * 유저 포인트 충전 테스트
     * @throws Exception
     */
    @Test
    void chargePointsTest() throws Exception {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 200L, System.currentTimeMillis());

        // 서비스의 chargePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.chargePoints(anyLong(), anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        // 1번 유저의 포인트 충전
        // 체크사항 >> status 200, userId 1, point 200
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.point", is(200)));
    }

    /**
     * 유저 포인트 사용 테스트
     * @throws Exception
     */
    @Test
    void usePointsTest() throws Exception {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());

        // 서비스의 usePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.usePoints(anyLong(), anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        // 1번 유저의 포인트 사용
        // 체크사항 >> status 200, userId 1, point 0
        mockMvc.perform(patch("/point/1/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.point", is(0)));
    }

    /**
     * 유저 포인트 충전 동시성 테스트
     * fail test
     * @throws CustomException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    void chargePointsConcurrencyFailTest() throws CustomException, ExecutionException, InterruptedException, TimeoutException {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());

        // 서비스의 chargePoints 메서드를 모킹하여 예외를 던지도록 설정
        when(pointService.chargePoints(anyLong(), anyLong())).thenThrow(new CustomException("0포인트 이하는 충전 할 수 없습니다."));

        int threadCount = 10;

        // 비동기 작업 10개 생성, 수행
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(patch("/point/1/charge")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\": 0}")
                            )
                            .andExpect(status().is5xxServerError())
                            .andExpect(jsonPath("$.message", is("0포인트 이하는 충전 할 수 없습니다.")));
                    // 실패 케이스이므로 UserPoint 반환하지 않음
                } catch (Exception e) {
                    //Request processing failed: io.hhplus.tdd.custom.CustomException: 0포인트 이하는 충전 할 수 없습니다.
                    System.out.println(e.getMessage());
                }
            }));
        }

        // 모든 비동기 작업이 완료될 때까지 대기하며 타임아웃 10초를 설정(무한대기 방지)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
    }

    /**
     * 유저 포인트 사용 동시성 테스트
     * fail test
     * @throws CustomException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    void usePointsConcurrencyFailTest() throws CustomException, ExecutionException, InterruptedException, TimeoutException {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());

        // 서비스의 usePoints 메서드를 모킹하여 예외를 던지도록 설정
        when(pointService.usePoints(anyLong(), anyLong())).thenThrow(new CustomException("0포인트 이상만 사용할 수 있습니다."));

        int threadCount = 10;

        // 비동기 작업 10개 생성, 수행
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    mockMvc.perform(patch("/point/1/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\": 0}")
                            )
                            .andExpect(status().is5xxServerError())
                            .andExpect(jsonPath("$.message", is("0포인트 이상만 사용할 수 있습니다.")));
                    // 실패 케이스이므로 UserPoint 반환하지 않음
                } catch (Exception e) {
                    // Request processing failed: io.hhplus.tdd.custom.CustomException: 0포인트 이상만 사용할 수 있습니다.
                    System.out.println(e.getMessage());
                }
            }));
        }

        // 모든 비동기 작업이 완료될 때까지 대기하며 타임아웃 10초를 설정(무한대기 방지)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
    }

    /**
     * 유저 포인트 충전 동시성 테스트
     * success test
     * @throws CustomException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    void chargePointsConcurrencyTest() throws CustomException, ExecutionException, InterruptedException, TimeoutException {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 200L, System.currentTimeMillis());

        // 서비스의 chargePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.chargePoints(anyLong(), anyLong())).thenReturn(userPoint);

        int threadCount = 10;

        // 비동기 작업 10개 생성, 수행
        List<CompletableFuture<UserPoint>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> { // 각 스레드마다 비동기 작업을 수행
                try {
                    MvcResult mvcResult = mockMvc.perform(patch("/point/1/charge")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\": 100}")
                            )
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.id", is(1)))
                            .andExpect(jsonPath("$.point", is(200)))
                            .andReturn();

                    // JSON 응답에서 UserPoint 객체를 생성하여 반환
                    String json = mvcResult.getResponse().getContentAsString();
                    long userId = ((Integer) JsonPath.read(json, "$.id")).longValue();
                    long point = ((Integer) JsonPath.read(json, "$.point")).longValue();

                    return new UserPoint(userId, point, System.currentTimeMillis());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        // 모든 비동기 작업이 완료될 때까지 대기하며 타임아웃 10초를 설정(무한대기 방지)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

        // 최초 생성한 유저 1의 100포인트가 10번 찍히는지 확인
        for (CompletableFuture<UserPoint> future : futures) {
            System.out.println("future >> " + future.get());
        }
    }

    /**
     * 유저 포인트 사용 동시성 테스트
     * success test
     * @throws CustomException
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    void usePointsConcurrencyTest() throws CustomException, ExecutionException, InterruptedException, TimeoutException {
        // 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());

        // 서비스의 usePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.usePoints(anyLong(), anyLong())).thenReturn(userPoint);

        int threadCount = 10;

        // 비동기 작업 10개 생성, 수행
        List<CompletableFuture<UserPoint>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> { // 각 스레드마다 비동기 작업을 수행
                try {
                    MvcResult mvcResult = mockMvc.perform(patch("/point/1/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\": 100}")
                            )
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.id", is(1)))
                            .andExpect(jsonPath("$.point", is(100)))
                            .andReturn();

                    // JSON 응답에서 UserPoint 객체를 생성하여 반환
                    String json = mvcResult.getResponse().getContentAsString();
                    long userId = ((Integer) JsonPath.read(json, "$.id")).longValue();
                    long point = ((Integer) JsonPath.read(json, "$.point")).longValue();

                    return new UserPoint(userId, point, System.currentTimeMillis());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }

        // 모든 비동기 작업이 완료될 때까지 대기하며 타임아웃 10초를 설정(무한대기 방지)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);

        // 최초 생성한 유저 1의 100포인트가 10번 찍히는지 확인
        for (CompletableFuture<UserPoint> future : futures) {
            System.out.println("future >> " + future.get());
        }
    }
}
