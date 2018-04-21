package com.bearya.robot.household.http.retrofit;

import android.support.annotation.NonNull;

import com.bearya.robot.household.R;
import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.http.retrofit.interceptor.CacheControlInterceptor;
import com.bearya.robot.household.http.retrofit.interceptor.HttpCommonInterceptor;
import com.bearya.robot.household.utils.LogUtils;
import com.bearya.robot.household.utils.SharedPrefUtil;
import com.bearya.robot.household.utils.UserInfoManager;

import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class HttpRetrofitClient<V> implements HttpLoggingInterceptor.Logger {

    private final String TAG = "HttpRetrofitClient";

    protected static boolean debug = true;

    public final static String BASE_URL_DEFAULT = "https://api.bearya.com/";
    public final static String BASE_URL_DEBUG  = "https://dev.api.bearya.com/";

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private V apiService;
    private SSLSocketFactory sslSocketFactory;
    private X509TrustManager trustManager;
    private String baseUrl = BASE_URL_DEBUG;

    public SSLSocketFactory getSslSocketFactory() {
        if (sslSocketFactory == null) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{getTrustManager()}, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
        return sslSocketFactory;
    }

    public X509TrustManager getTrustManager() {
        if (trustManager == null) {
            try {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);
                TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                    throw new IllegalStateException("Unexpected default trust managers:"
                            + Arrays.toString(trustManagers));
                }
                trustManager = (X509TrustManager) trustManagers[0];
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                trustManager = new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                    }
                };
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        }
        return trustManager;
    }

    private CookieJar cookieJar = new CookieJar() {
        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    };

    private HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor();

    public void setCommonInterceptor(HttpCommonInterceptor commonInterceptor) {
        this.commonInterceptor = commonInterceptor;
    }

    private CacheControlInterceptor cacheControlInterceptor = new CacheControlInterceptor();

    public HttpRetrofitClient() {
        getRetrofit(baseUrl);
    }

    protected OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            File httpCacheDirectory = new File(MyApplication.getContext().getCacheDir(), "responses");
            int cacheSize = 10 * 1024 * 1024;
            Cache cache = new Cache(httpCacheDirectory, cacheSize);

//            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(this);
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            HttpCommonInterceptor commonInterceptors = new HttpCommonInterceptor();

            okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(HttpConstant.HTTP_REQUEST_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .readTimeout(HttpConstant.HTTP_REQUEST_READ_TIMEOUT, TimeUnit.MILLISECONDS)
                    .cookieJar(cookieJar)
                    .sslSocketFactory(getSslSocketFactory(), getTrustManager())
                    .addInterceptor(commonInterceptors)
//                    .addNetworkInterceptor(cacheControlInterceptor)
//                    .addInterceptor(cacheControlInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .cache(cache)
                    .build();
        }

        return okHttpClient;
    }

    protected Retrofit getRetrofit(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(getOkHttpClient())
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public V getService(final Class<V> serviceCls) {
        if (apiService == null) {
            apiService = getRetrofit(baseUrl).create(serviceCls);
        }
        return apiService;
    }

    /**
     * 对网络接口返回的Response进行分割操作
     *
     * @param response
     * @param <T>
     * @return
     */
    public <T> Observable<T> flatResponse(final HttpResult<T> response) {
        return Observable.create(new Observable.OnSubscribe<T>() {

            @Override
            public void call(Subscriber<? super T> subscriber) {
                if (response.isSuccess()) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(response.getData());
                    }
                } else if (response.isTokenInvalid()) {
                    UserInfoManager.getInstance().loginOut();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new APIException(response.getStatus(), response.getText(), isShowTips));
                    }
                    return;
                } else {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new APIException(response.getStatus(), response.getText(), isShowTips));
                    }
                    return;
                }

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }

            }
        });
    }

    private boolean isShowTips;

    public void setIsShowTips(boolean isShowTips) {
        this.isShowTips = isShowTips;
    }


    /**
     * 自定义异常，当接口返回的{@link HttpResult#status}不为{@link BusinessStatusCodeEnum#SUCCESS_OK}时，需要抛出此异常
     * eg：登陆时验证码错误；参数为传递等
     */
    public static class APIException extends Exception {
        public int code;
        public String message;

        public APIException(int code, String message, boolean isShowTips) {
            this.code = code;
            this.message = message;
            if (isShowTips) {
//                Utils.showToast(message);
            }
        }

        @Override
        public String getMessage() {
            return message + "(" + code + ")";
        }

        public int getCode() {
            return code;
        }

        public String getErrorMessage() {
            return message;
        }

    }

    @SuppressWarnings("unchecked")
    protected <T> Observable.Transformer<HttpResult<T>, T> applySchedulers() {
        return (Observable.Transformer<HttpResult<T>, T>) transformer;
    }

    @SuppressWarnings("unchecked")
    final Observable.Transformer transformer = new Observable.Transformer() {
        @Override
        public Object call(Object observable) {
            return ((Observable) observable).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnError(new Action1<Throwable>() {
//                        @Override
//                        public void call(Throwable throwable) {
//                            System.out.println("okhttp====doOnError---" + throwable.toString());
//                            throwable.printStackTrace();
//                        }
//                    })
//                    .onExceptionResumeNext(Observable.empty())
                    .onErrorResumeNext(new HttpResponseFunc())
                    .flatMap(new Func1() {
                        @Override
                        public Object call(Object response) {
                            return flatResponse((HttpResult<Object>) response);
                        }
                    });
        }
    };

    private static class HttpResponseFunc<T> implements Func1<Throwable, Observable<T>> {
        @Override public Observable<T> call(Throwable t) {
            return Observable.error(ExceptionHandle.handleException(t));
        }
    }

    @Override
    public void log(@NonNull String message) {
        LogUtils.d(TAG, message);
    }
}
