package com.iamverycute.weather;

import static android.app.PendingIntent.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.alibaba.fastjson.JSON;
import com.iamverycute.weather.model.Forecast;
import com.iamverycute.weather.model.JsonRootBean;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ForegroundService extends Service implements Callback, Consumer<Long>, Handler.Callback {

    private Call call;
    private List<Forecast> list;
    private Notification notify;
    private Disposable obDispose;
    private Method collapsePanels;
    private RemoteViews notifyView;
    private StatusBarManager service;
    public static boolean isRunning = false;
    private final Handler handler = new Handler(this);
    private final OkHttpClient client = new OkHttpClient();
    private final Request request = new Request.Builder().url("http://wthrcdn.etouch.cn/weather_mini?citykey=101280101").build();

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        final NotificationChannel channel = new NotificationChannel("notify_weather", "notify_weather", NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("Notify Weather Service");
        channel.setSound(null, null);
        channel.enableLights(false);
        channel.setShowBadge(false);
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel.getId());
        notifyView = new RemoteViews(getPackageName(), R.layout.notify);
        notifyView.setOnClickPendingIntent(R.id.weather_refresh, getService(getApplicationContext(),
                1,
                new Intent(getApplicationContext(), ForegroundService.class).putExtra("events", "updtWeather"),
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));
        notify = builder.setCustomContentView(notifyView).setWhen(System.currentTimeMillis()).setSmallIcon(R.drawable.icon_line).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true).build();
        startForeground(1, notify);
        service = getSystemService(StatusBarManager.class);
        try {
            collapsePanels = service.getClass().getMethod("collapsePanels");
        } catch (NoSuchMethodException ignored) {
        }
        final Observable<Long> ob = Observable.interval(45, TimeUnit.MINUTES, Schedulers.computation());
        obDispose = ob.subscribe(this);
    }

    @Override
    public void accept(Long aLong) {
        updtWeather();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String data = intent.getStringExtra("events");
            if (data != null) {
                switch (data) {
                    case "showActivity":
                        // startActivity(new Intent(this, MainActivity.class));
                        HideStatusBar();
                        break;
                    case "updtWeather":
                        notifyView.setViewVisibility(R.id.first_tips, View.GONE);
                        notifyView.setViewVisibility(R.id.weather_info, View.GONE);
                        notifyView.setViewVisibility(R.id.loading_tips, View.VISIBLE);
                        startForeground(1, notify);
                        updtWeather();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1000:
                int count = 0;
                for (Forecast item : list) {
                    String low = item.getLow().substring(2).trim();
                    String w1 = item.getDate().substring(item.getDate().indexOf(getString(R.string.day)) + 1);
                    String w2 = low.substring(0, low.length() - 1) + "~" + item.getHigh().substring(2).trim();
                    String w3 = item.getType().trim();
                    switch (count) {
                        case 0:
                            notifyView.setTextViewText(R.id.day1, w1 + "\n" + w2 + "\n" + w3);
                            break;
                        case 1:
                            notifyView.setTextViewText(R.id.day2, w1 + "\n" + w2 + "\n" + w3);
                            break;
                        case 2:
                            notifyView.setTextViewText(R.id.day3, w1 + "\n" + w2 + "\n" + w3);
                            break;
                        case 3:
                            notifyView.setTextViewText(R.id.day4, w1 + "\n" + w2 + "\n" + w3);
                            break;
                        case 4:
                            notifyView.setTextViewText(R.id.day5, w1 + "\n" + w2 + "\n" + w3);
                            break;
                        default:
                            break;
                    }
                    count++;
                }
                break;
            case 2000:
                HideStatusBar();
                if (list == null) {
                    notifyView.setViewVisibility(R.id.first_tips, View.VISIBLE);
                }
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                break;
        }
        notifyView.setViewVisibility(R.id.weather_info, View.VISIBLE);
        notifyView.setViewVisibility(R.id.loading_tips, View.GONE);
        startForeground(1, notify);
        return false;
    }

    private void HideStatusBar() {
        if (service != null && collapsePanels != null) {
            try {
                collapsePanels.invoke(service);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (obDispose != null && !obDispose.isDisposed()) {
            obDispose.dispose();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updtWeather() {
        if (call != null && !call.isExecuted()) {
            return;
        }
        call = client.newCall(request);
        call.enqueue(this);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        Message msg = Message.obtain();
        msg.what = 2000;
        handler.sendMessage(msg);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response resp) {
        try {
            String res = Objects.requireNonNull(resp.body()).string();
            JsonRootBean weatherInfo = JSON.parseObject(res, JsonRootBean.class);
            if (weatherInfo != null && weatherInfo.getStatus() == 1000) {
                weatherInfo.getData().getForecast().get(0).setDate(getString(R.string.today));
                list = weatherInfo.getData().getForecast();
                Message msg = Message.obtain();
                msg.what = 1000;
                handler.sendMessage(msg);
            }
        } catch (IOException ignored) {
        }
    }
}