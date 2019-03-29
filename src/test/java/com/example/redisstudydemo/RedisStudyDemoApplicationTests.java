package com.example.redisstudydemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisStudyDemoApplicationTests {

    @Autowired
    private RedissonClient redissonClient;

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    @Test
    public void contextLoads() {
        RMap<String,Integer> map = redissonClient.getMap("test-map");
        map.put("test" , 100);
        for (int i = 0; i < 100;i++) {
            new Thread(() -> {
                task(map);
            }).start();
        }
        while(true){
            if(atomicInteger.get() == 100){
                break;
            }
        }
        System.out.println(map.get("test"));
    }

    private void task(RMap<String,Integer> map){
        RLock lock = map.getFairLock("test-lock");
//        lock.lock(3, TimeUnit.SECONDS);
        try {
            if(!lock.tryLock(4000,1000, TimeUnit.MILLISECONDS)){
                Thread.currentThread().interrupt();
                Thread.currentThread().sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("锁失败，取消任务");
            atomicInteger.addAndGet(1);
            return;
        }
        try {
            Integer value = map.get("test");
            System.out.println(Thread.currentThread().getName() + "-------------->" + --value);
            map.put("test" , value);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
           /* if("Thread-71".equals(Thread.currentThread().getName())){
                atomicInteger.addAndGet(1);
                throw new RuntimeException("宕机");
            }*/
            lock.unlock();
            atomicInteger.addAndGet(1);
        }
    }

}
