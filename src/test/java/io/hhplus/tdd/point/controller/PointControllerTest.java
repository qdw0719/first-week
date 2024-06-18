package io.hhplus.tdd.point.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
//        MockitoAnnotations.openMocks(this); 
    	mockMvc = MockMvcBuilders.standaloneSetup(new PointController(pointService)).build();
    }

    @Test
    void getUserPointTest() throws Exception {
    	// 기본 세팅, User 임의 생성
        UserPoint userPoint = new UserPoint(1L, 100L, System.currentTimeMillis());
        
        // 서비스의 getUserPoint 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.getUserPoint(anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.point").value(100));
    }

    @Test
    void getPointHistoriesTest() throws Exception {
    	// 기본 세팅, User point history 임의 생성
    	List<PointHistory> pointHistories = List.of(
            new PointHistory(1L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(2L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(3L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(4L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(5L, 1L, 100L, TransactionType.CHARGE, System.currentTimeMillis())
        );
    	
        // 서비스의 getPointHistories 메서드를 모킹하여 pointHistories를 반환하도록 설정
        when(pointService.getPointHistories(anyLong())).thenReturn(pointHistories);

        // 테스트 수행 및 결과 검증
        mockMvc.perform(get("/point/1/histories"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()", is(5)));
    }

    @Test
    void chargePointsTest() throws Exception {
    	// 100포인트 충전 후 새로 생성
    	UserPoint userPoint = new UserPoint(1L, 200L, System.currentTimeMillis());
    	
    	// 서비스의 chargePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.chargePoints(anyLong(), anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        mockMvc.perform(patch("/point/1/charge")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"amount\": 100}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.point", is(200)));
    }

    @Test
    void usePointsTest() throws Exception {
    	// 100포인트 차감 후 새로 생성
    	UserPoint userPoint = new UserPoint(1L, 0L, System.currentTimeMillis());
    	
    	// 서비스의 usePoints 메서드를 모킹하여 userPoint를 반환하도록 설정
        when(pointService.usePoints(anyLong(), anyLong())).thenReturn(userPoint);

        // 테스트 수행 및 결과 검증
        mockMvc.perform(patch("/point/1/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.point", is(0)));
    }
}
