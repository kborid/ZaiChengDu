package com.z012.chengdu.sc.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.GlideModule;

/**
 * @author kborid
 * @date 2017/9/25.
 */

public class GlideModuleConfig implements GlideModule {
    private static final int MEMORY_CACHE_SIZE = (int) Math.min(Runtime.getRuntime().maxMemory() / 4, 16 * 1024 * 1024);
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
        int defaultMemoryCacheSize = calculator.getMemoryCacheSize();
        int defaultBitmapPoolSize = calculator.getBitmapPoolSize();

        int customBitmapPoolSize = (int) (1.2 * defaultBitmapPoolSize);
        int customMemoryCacheSize = (int) (1.2 * defaultMemoryCacheSize);
//        System.out.println("custom MCS = " + customMemoryCacheSize);
//        System.out.println("custom BPS = " + customBitmapPoolSize);
//        System.out.println("MEMORY_CACHE_SIZE = " + MEMORY_CACHE_SIZE);

        builder.setBitmapPool(new LruBitmapPool(customBitmapPoolSize));
        builder.setMemoryCache(new LruResourceCache(MEMORY_CACHE_SIZE));
        builder.setDiskCache(new ExternalCacheDiskCacheFactory(context, DISK_CACHE_SIZE));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
//        glide.register(CustomReqURLFormatModel.class, InputStream.class, new CustomReqURLFormatModelFactory());
    }
}
