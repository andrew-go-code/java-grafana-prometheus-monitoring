package com.demo.javagrafanaprometheusmonitoring;

import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

@SpringBootApplication
@RestController
@RequestMapping("/server")
public class JavaGrafanaPrometheusMonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaGrafanaPrometheusMonitoringApplication.class, args);
    }

    private final LongAdder globalCounter = new LongAdder();
    private final List<Info> infoList = new CopyOnWriteArrayList<>();

    @Value
    private static class Info {
        long objectCount;
        String threadName;
    }

    @Value
    private static class Result {
        long objectCount;
        Map<String, Long> threadWork;
    }

    @GetMapping
    @SneakyThrows
    public Result endpoint(
            @RequestParam(value = "delayMs") Long delayMs,
            @RequestParam(value = "creatingTimeMs") Long creatingTimeMs,
            @RequestParam(value = "threadCount") Integer threadCount,
            @RequestParam(value = "forgetToClearObjects", required = false) Boolean forgetToClearObjects) {
        globalCounter.reset();
        long stopTime = System.currentTimeMillis() + creatingTimeMs;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Callable<Info>> tasks = new ArrayList<>();

        for (int i = 0; i <= threadCount; i++) {
            tasks.add(() -> {
                long localCounter = 0;
                while (stopTime > System.currentTimeMillis()) {
                    Thread.sleep(delayMs);
                    globalCounter.increment();
                    localCounter++;
                    infoList.add(new Info(1, Thread.currentThread().getName()));
                }
                return new Info(localCounter, Thread.currentThread().getName());
            });
        }
        List<Future<Info>> futures = executorService.invokeAll(tasks);

        Map<String, Long> threadsWork = new HashMap<>();
        for (Future<Info> future : futures) {
            Info info = future.get();
            threadsWork.merge(info.getThreadName(), info.getObjectCount(), Long::sum);
        }
        System.out.println("---");
        System.out.println(threadsWork);
        threadsWork.clear();

        for (Info info : infoList) {
            threadsWork.merge(info.getThreadName(), info.getObjectCount(), Long::sum);
        }

        if (Objects.requireNonNullElse(forgetToClearObjects, true)) {
            infoList.clear();
        }
        executorService.shutdownNow();
        return new Result(globalCounter.sumThenReset(), threadsWork);
    }
}
