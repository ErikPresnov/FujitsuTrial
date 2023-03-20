package com.example.SpringBoot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(FujitsuTrialApplication.class)
@ExtendWith(SpringExtension.class)
class FujitsuTrialApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void calculateFeeOnGivenTimestamp() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/%s/%s/%d".formatted("tartu", "car", 1679320713));
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        assertEquals(0,Integer.parseInt(result.getResponse().getContentAsString()));
    }
}