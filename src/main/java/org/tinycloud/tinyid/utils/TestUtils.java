package org.tinycloud.tinyid.utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 测试脚本程序
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:58
 */
public class TestUtils {

    /**
     * 测试并发
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {


    }


    /**
     * 测试并发HTTP请求获取ID是否重复
     */
    public static void testHttpConcurrency() {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        final Set<String> sets = ConcurrentHashMap.newKeySet();

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


    /**
     * 测试直接调用并发重复问题
     */
    public static void testConcurrency() throws InterruptedException {
        int threadCount = 100;
        int requestsPerThread = 1000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ConcurrentHashMap<String, Boolean> idMap = new ConcurrentHashMap<>();
        AtomicInteger duplicateCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    String id = IdTableUtils.nextId("TEST_CODE");
                    if (idMap.putIfAbsent(id, true) != null) {
                        duplicateCount.incrementAndGet();
                        System.out.println("发现重复ID: " + id);
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("总生成ID数: " + idMap.size());
        System.out.println("重复ID数: " + duplicateCount.get());
    }
}
