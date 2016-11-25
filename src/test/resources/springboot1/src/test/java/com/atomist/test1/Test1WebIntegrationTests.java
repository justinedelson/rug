/*
    Generated by Atomist on 12 07 2016 
*/
package com.atomist.test1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Test1Application.class)
@WebIntegrationTest("server.port:8080")
public class Test1WebIntegrationTests {

  private static final int PORT = 8080;

    // Parameterize tests like this
    private static final String AN_APP_PATH = "http://localhost:" + PORT + "/path";
 
    // Use this to run tests
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    public void sampleTest() {
        // Use RestTemplate to hit chosen URLs
    }
 }