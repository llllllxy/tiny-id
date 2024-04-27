package org.tinycloud.tinyid.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:58
 */
public class TestUtils {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(10);

        final Set<String> sets = new HashSet<>();

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9999/api/segment/get"))
                .header("userHeader", "myHeader")
                .header("cookie", "JSESSIONID=111")
                .timeout(Duration.ofSeconds(100))
                .GET()
                .build();

        for (int i = 0; i < 5; i++) {
            executor.execute(() -> {
                try {
                    System.out.println("同步请求2开始");
                    HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());

                    System.out.println("同步请求2:" + response.body());

                    Map<String, Object> responseMap = JsonUtils.readMap(response.body());

                    boolean result = sets.add(responseMap.get("data").toString());
                    if (!result) {
                        System.out.println("Error occurred result: " + result);
                    }
                } catch (Exception e) {
                    System.out.println("Error occurred: " + e.getMessage());
                }
            });
        }

    }
}
