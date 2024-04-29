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
 * 测试脚本程序
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:58
 */
public class TestUtils {

    public static void main(String[] args) {

        ExecutorService executor = Executors.newFixedThreadPool(20);
        final Set<String> sets = new HashSet<>();

        HttpClient client = HttpClient.newBuilder().build();

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9999/api/segment/get/XXX"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        for (int i = 0; i < 5006; i++) {
            executor.execute(() -> {
                try {
                    HttpResponse<String> response = client.send(request1, HttpResponse.BodyHandlers.ofString());
                    Map<String, Object> responseMap = JsonUtils.readMap(response.body());
                    String id = responseMap.get("data").toString();
                    System.out.println(id);
                    boolean result = sets.add(id);
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
