package com.example.redisstudydemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.RedissonMultiLock;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisStudyDemoApplicationTests2 {

    private AtomicInteger atomicInteger = new AtomicInteger(0);
    @Autowired
    private Map<Integer , RedissonClient>  redissonClientMap;

    @Test
    public void contextLoads() {
        //获取客户端实例，也可以配置aop切换，或直接通过注解形式注入
        RMap<String,Integer>[] maps = new RMap[3];
        for (int i =0;i < 3;i++){
            //获取map
            maps[i] = redissonClientMap.get(i).getMap("test-map");
            maps[i].put("test" , 100);
        }
        Long time = System.currentTimeMillis();
        for (int i = 0; i < 100;i++) {
            new Thread(() -> {
                task(maps);
            }).start();
        }
        while(true){
            if(atomicInteger.get() == 100){
                break;
            }
        }
        for (int i = 0; i < maps.length; i++) {
            System.out.print(maps[i].get("test") + "<---->");
        }
        System.out.println(System.currentTimeMillis() - time);
    }

    private void task(RMap<String,Integer> ... maps){
        RReadWriteLock[] locks = new RReadWriteLock[maps.length];
        for (int i = 0 ;i< maps.length ;i++) {
            locks[i] = maps[i].getReadWriteLock("test-lock");
            locks[i].writeLock().lock(10,TimeUnit.SECONDS);
//            locks[i].readLock().lock(3,TimeUnit.SECONDS);
        }
//        RedissonRedLock redLock = new RedissonRedLock (locks);
        //上锁 3秒钟自动解锁
//        redLock.lock(3 , TimeUnit.SECONDS);
        try {
            for (int i = 0 ;i< maps.length ;i++) {
                Integer value =  maps[i].get("test");
                System.out.print( String.format("%s--------------->[value%d:%d];      ",Thread.currentThread().getName() , i ,--value));
                maps[i].put("test" , value);
            }
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //解锁
//            redLock.unlock();
            for (RReadWriteLock  rReadWriteLock: locks){
                rReadWriteLock.writeLock().unlock();
            }
            atomicInteger.addAndGet(1);
        }
    }
    
}
