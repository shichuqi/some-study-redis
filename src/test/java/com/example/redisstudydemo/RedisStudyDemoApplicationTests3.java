package com.example.redisstudydemo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisStudyDemoApplicationTests3 {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private Map<Integer , RedissonClient>  redissonClientMap;

    private RedissonClient redissonClient0;

    @PostConstruct
    public void init(){
        redissonClient0 = redissonClientMap.get(999);
    }

    @Test
    public void contextLoads() {
        RMap<String,Integer> map = redissonClient.getMap("test-map");
        map.put("test" , 100);
        RReadWriteLock lock = map.getReadWriteLock("lock");
        //1、读：锁；写：解
        System.out.println("1、读：锁；写：解");
        lock.writeLock().lock(3,TimeUnit.SECONDS);
        lock.readLock().lock(3,TimeUnit.SECONDS);
        long time1 = System.currentTimeMillis();
        map.put("test",99);
        System.out.println("写耗时：" + (System.currentTimeMillis()-time1));
        //重置时间
        time1 = System.currentTimeMillis();
        System.out.println(map.get("test"));
        System.out.println("读耗时：" + (System.currentTimeMillis()-time1));
        System.out.println("==============================================");

        new Thread(()->{
            map.put("test",0);
            RMap<String,Integer> map1 = redissonClient0.getMap("test-map");
            System.out.println(Thread.currentThread().getName() + ":" + map1.get("test"));
        }).start();

        lock.readLock().unlock();
        lock.writeLock().unlock();
        System.out.println(map.get("test"));
        //2、读：锁；写：锁
       /* System.out.println("2、读：锁；写：锁");
        lock.writeLock().lock(3,TimeUnit.SECONDS);
        time1 = System.currentTimeMillis();
        map.put("test",99);
        System.out.println("写耗时：" + (System.currentTimeMillis()-time1));

        lock.readLock().lock(3,TimeUnit.SECONDS);
        //重置时间
        time1 = System.currentTimeMillis();
        System.out.println(map.get("test"));
        System.out.println("读耗时：" + (System.currentTimeMillis()-time1));
        System.out.println("==============================================");

        lock.readLock().unlock();
        lock.writeLock().unlock();
        //3、读：解；写：锁
        System.out.println("3、读：解；写：锁");
        lock.writeLock().lock(3,TimeUnit.SECONDS);
        time1 = System.currentTimeMillis();
        System.out.println(map.get("test"));
        System.out.println("读耗时：" + (System.currentTimeMillis()-time1));
        //重置时间
        time1 = System.currentTimeMillis();
        map.put("test",99);
        System.out.println("写耗时：" + (System.currentTimeMillis()-time1));
        System.out.println("==============================================");*/

    }


}
