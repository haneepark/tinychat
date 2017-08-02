package com.parkhanee.tinychat;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache; // could be wrong one; if it causes error, use android.support.v4.LruCache instead.

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by parkhanee on 2017. 7. 24..
 * https://developer.android.com/training/volley/requestqueue.html
 * Singleton Class
 */

public final class MyVolley {
    // 처음 인스턴스화 할때 생성되서 그 뒤로 바뀌지 않고 singleton class 내부 에서만 사용되는 변수들!
    private static MyVolley myVolley=null;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Context mCtx;

    // 생성자. 맨처음 한번만 호출
    private MyVolley(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    // 필요할 때 마다 호출
    public static synchronized MyVolley getInstance(Context context) {
        if (myVolley == null) {
            // MyVolley 인스턴스 생성은 맨 처음 getInstance가 호출 되었을 때 딱 한번 만 실행된다.
            myVolley = new MyVolley(context);
        }
        return myVolley;
    }

    // 필요할 때 마다 호출
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.

            // 처음한번만 실행
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }


    // FIXME: 2017. 7. 26. 이게 필요한가 ? 얘는 non-static method라서 액티비티에서 갖다 쓸 수 없는데 ?
    // 현재는 같은 기능을 액티비티에서는 queue.add 해서 쓰고있다.
    // 그러면 얘가 이 클래스 내부에서는 쓰일일이 있나 ?
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    // 필요할 때 마다 호출
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

}
