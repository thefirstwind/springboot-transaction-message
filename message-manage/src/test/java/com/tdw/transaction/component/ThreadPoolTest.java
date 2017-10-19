package com.tdw.transaction.component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



public class ThreadPoolTest {

    public static void main(String[] args) {

		ExecutorService poll = Executors.newFixedThreadPool(3);

        System.out.println("================main a=====================");
        Future<Boolean> future = poll.submit(t1f("1111111"));
        poll.submit(t2f());

        System.out.println("================main b=====================");
        try {
            future.get(5,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(); //get为一个等待过程，异常中止get会抛出异常 
        } catch (ExecutionException e) {
            e.printStackTrace(); //submit计算出现异常
        } catch (TimeoutException e) {
            e.printStackTrace(); //超时异常
            future.cancel(true); //超时后取消任务
        }finally {
            System.out.println("================main done=====================");
            poll.shutdown();
        }

        System.out.println("================main c=====================");
    }
    
    
    private  static Callable<Boolean> t1f(String pstr)
    {
    	return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                System.out.println("================t1f b=====================" + pstr);
                String pstrs = pstr + "ccc";
            	Thread.sleep(5000);
                System.out.println("================t1f e=====================" + pstrs);
                return true; 
            }
        };
    }
    
    
    private  static Callable<Boolean> t2f()
    {
    	return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                System.out.println("==============t2f b=======================");
            	Thread.sleep(3000);
                System.out.println("==============t2f e========================");
                return true; 
            }
        };
    }
    
    
}