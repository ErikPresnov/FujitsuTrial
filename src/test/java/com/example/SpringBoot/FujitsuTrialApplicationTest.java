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


    //tallinn	26038	6.5 2.3	light rain	1679332081
    @Test
    public void calculateFeeOnGivenTimestamp() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/%s/%s/%d".formatted("tallinn", "car", 1679332081));
        MvcResult result = mvc.perform(requestBuilder).andReturn();
        // Tallinn / Car -> BaseFee = 4.0
        // no extra fees for airTemp, windSpeed or phenomenon since vehicleType = car
        assertEquals(4.0,Double.parseDouble(result.getResponse().getContentAsString()));

        requestBuilder = MockMvcRequestBuilders.get("/%s/%s/%d".formatted("tallinn", "scooter", 1679332081));
        result = mvc.perform(requestBuilder).andReturn();
        // Tallinn / scooter -> BaseFee = 3.5
        // rain -> phenomenonFee = 0.5
        // temp = 6.5 so no extra fee
        // wind = 2.3 so no extra fee
        assertEquals(4.0,Double.parseDouble(result.getResponse().getContentAsString()));

        requestBuilder = MockMvcRequestBuilders.get("/%s/%s/%d".formatted("tallinn", "bike", 1679332081));
        result = mvc.perform(requestBuilder).andReturn();
        // Tallinn / bike -> BaseFee = 3.0
        // rain -> phenomenonFee = 0.5
        // temp = 6.5 so no extra fee
        // wind = 2.3 so no extra fee
        assertEquals(3.5,Double.parseDouble(result.getResponse().getContentAsString()));
    }
}