package cn.reve.framework.utils;

import java.util.concurrent.*;

/**
 * 简要描述
 *
 * @author : Rêve
 * @version : 1.0
 * @date : 2023/5/4 16:15
 */
public class ThreadPoolsUtils {
    // 核心线程池大小
    static final int corePoolSize = 4;
    // 最大线程池大小
    static final int maximumPoolSize =16;
    // 线程最大空闲时间
    static final long keepAliveTime = 2000L;
    // 时间单位
    static final TimeUnit unit = TimeUnit.MILLISECONDS;

    public static ExecutorService exec = new ThreadPoolExecutor(
            // 核心线程池大小
            corePoolSize,
            // 最大线程池大小
            maximumPoolSize,
            // 线程最大空闲时间
            keepAliveTime,
            // 时间单位
            unit,
            // 线程等待队列
            new LinkedBlockingQueue<Runnable>(64),
            // 线程创建工厂
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable);
                }
            },
            // 拒绝策略,这里直接抛出异常
            new ThreadPoolExecutor.AbortPolicy()
    );


}
