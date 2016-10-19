/*
 * Copyright (c) 2016 Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.viewmodel;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.model.SSQuarterlyInfo;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.concurrent.TimeUnit;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SSLessonsViewModel implements SSViewModel, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = SSLessonsViewModel.class.getSimpleName();
    private static final int SS_LESSONS_UPDATE_DELAY = 5;

    private Context context;
    private Subscription subscription;
    private Subscription subscriptionDelay;
    private SSQuarterlyInfo ssQuarterlyInfo;
    private String ssQuarterlyPath;
    private DataListener dataListener;
    
    public ObservableInt ssLessonsLoadingVisibility;
    public ObservableInt ssLessonsErrorMessageVisibility;
    public ObservableInt ssLessonsEmptyStateVisibility;
    public ObservableInt ssLessonsErrorStateVisibility;
    public ObservableInt ssLessonsCoordinatorVisibility;

    public SSLessonsViewModel(Context context, DataListener dataListener, String ssQuarterlyPath) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssQuarterlyPath = ssQuarterlyPath;

        ssLessonsLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsErrorMessageVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssLessonsCoordinatorVisibility = new ObservableInt(View.INVISIBLE);

        loadQuarterlyInfo();
        loadQuarterlyInfo(SS_LESSONS_UPDATE_DELAY);
    }

    private String getQuarterlyInfoEtag(final Context context, String ssQuarterlyInfoCacheKey){
        String etag = null;
        try {
            DB snappydb = DBFactory.open(context);
            etag = snappydb.get(ssQuarterlyInfoCacheKey + "_etag");
            snappydb.close();
        } catch (SnappydbException e) {}
        return etag;
    }

    private void cacheQuarterlyInfo(final Context context, String ssQuarterlyInfoCacheKey, SSQuarterlyInfo ssQuarterlyInfo, String etag){
        try {
            DB snappydb = DBFactory.open(context);
            snappydb.put(ssQuarterlyInfoCacheKey, ssQuarterlyInfo);
            snappydb.put(ssQuarterlyInfoCacheKey + "_etag", etag);
            snappydb.close();
        } catch (SnappydbException e) {}
    }

    private Observable<Response<SSQuarterlyInfo>> getQuarterlyInfoObservableCache(final Context context, final String ssQuarterlyInfoCacheKey){
        return Observable.create(
            new Observable.OnSubscribe<Response<SSQuarterlyInfo>>() {
                @Override
                public void call(Subscriber<? super Response<SSQuarterlyInfo>> sub) {
                    try {
                        DB snappydb = DBFactory.open(context);
                        SSQuarterlyInfo ssQuarterlyInfoCache;

                        ssQuarterlyInfoCache = snappydb.getObject(ssQuarterlyInfoCacheKey, SSQuarterlyInfo.class);
                        if (ssQuarterlyInfoCache != null) {
                            sub.onNext(Response.success(ssQuarterlyInfoCache));
                        }
                        snappydb.close();

                    } catch (SnappydbException e) {}
                    sub.onCompleted();
                }
            }
        );
    }

    private Observable<Response<SSQuarterlyInfo>> getQuarterlyInfoObservableFresh(final Context context, final String ssQuarterlyId){
        SSApplication ssApplication = SSApplication.get(context);
        return ssApplication.getGithubService().getQuarterlyInfo(ssQuarterlyId);
    }

    private Subscriber<Response<SSQuarterlyInfo>> getQuarterlyInfoSubscriber(final boolean ui){
        return new Subscriber<Response<SSQuarterlyInfo>>() {
            @Override
            public void onStart(){
                super.onStart();

                if (ui) {
                    ssLessonsLoadingVisibility.set(View.VISIBLE);
                    ssLessonsErrorMessageVisibility.set(View.INVISIBLE);
                    ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
                    ssLessonsErrorStateVisibility.set(View.INVISIBLE);
                }
            }

            @Override
            public void onCompleted() {
                dataListener.onRefreshFinished();

                if (ui) {
                    ssLessonsLoadingVisibility.set(View.INVISIBLE);
                    ssLessonsErrorMessageVisibility.set(View.INVISIBLE);
                    ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
                    ssLessonsErrorStateVisibility.set(View.INVISIBLE);
                    ssLessonsCoordinatorVisibility.set(View.VISIBLE);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (ui) {
                    ssLessonsErrorMessageVisibility.set(View.VISIBLE);
                    ssLessonsLoadingVisibility.set(View.INVISIBLE);
                    ssLessonsEmptyStateVisibility.set(View.INVISIBLE);
                    ssLessonsCoordinatorVisibility.set(View.INVISIBLE);
                    ssLessonsErrorStateVisibility.set(View.VISIBLE);
                }
            }

            @Override
            public void onNext(Response<SSQuarterlyInfo> ssQuarterlyInfoResponse) {
                String etag = ssQuarterlyInfoResponse.headers().get("etag");

                if (etag != null && !etag.equals(getQuarterlyInfoEtag(context, ssQuarterlyPath))){
                    cacheQuarterlyInfo(context, ssQuarterlyPath, ssQuarterlyInfoResponse.body(), etag);
                }

                if (ssQuarterlyInfoResponse.body() != null){
                    ssQuarterlyInfo = ssQuarterlyInfoResponse.body();
                    if (dataListener != null){
                        dataListener.onQuarterlyChanged(ssQuarterlyInfo);
                    }
                }
            }
        };
    }

    private void loadQuarterlyInfo() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        SSApplication ssApplication = SSApplication.get(context);

        subscription = Observable
                .concat(
                        getQuarterlyInfoObservableCache(context, ssQuarterlyPath),
                        getQuarterlyInfoObservableFresh(context, ssQuarterlyPath))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(ssApplication.defaultSubscribeScheduler())
                .subscribe(getQuarterlyInfoSubscriber(true));
    }

    private void loadQuarterlyInfo(int delay){
        if (subscriptionDelay != null && !subscriptionDelay.isUnsubscribed()) subscriptionDelay.unsubscribe();
        SSApplication ssApplication = SSApplication.get(context);

        subscriptionDelay = getQuarterlyInfoObservableFresh(context, ssQuarterlyPath)
                .delay(delay, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(ssApplication.defaultSubscribeScheduler())
                .subscribe(getQuarterlyInfoSubscriber(false));
    }

    @Override
    public void onRefresh() {
        loadQuarterlyInfo(0);
    }

    @Override
    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        subscription = null;
        subscriptionDelay = null;
        context = null;
        dataListener = null;
        ssQuarterlyInfo = null;
        ssQuarterlyPath = null;
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }


    public String getDate() {
        if (ssQuarterlyInfo != null){
            return ssQuarterlyInfo.quarterly.date;
        }
        return "";
    }

    public String getDescription() {
        if (ssQuarterlyInfo != null){
            return ssQuarterlyInfo.quarterly.description;
        }
        return "";
    }

    public String getCover() {
        if (ssQuarterlyInfo != null){
            return ssQuarterlyInfo.quarterly.cover;
        }
        return "";
    }

    @BindingAdapter({"coverUrl"})
    public static void loadCover(ImageView view, String coverUrl) {
        ViewCompat.setElevation(view, 15.0f);
        Glide.with(view.getContext())
                .load(coverUrl)
                .into(view);
    }

    public interface DataListener {
        void onQuarterlyChanged(SSQuarterlyInfo ssQuarterlyInfo);
        void onRefreshFinished();
    }
}