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

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableInt;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.behavior.SSReadingNavigationSheetBehavior;
import com.cryart.sabbathschool.misc.SSHelper;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.model.SSRead;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

public class SSReadingViewModel implements SSViewModel, SSReadingNavigationSheetBehavior.OnNestedScrollCallback {
    private static final String TAG = SSReadingViewModel.class.getSimpleName();
    private static final int PEEK_HEIGHT = 60;
    private static final int SS_READ_UPDATE_DELAY = 5;

    private Context context;
    private Subscription subscription;
    private Subscription subscriptionDelay;
    private String ssLessonPath;

    private SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior;
    public ObservableInt ssReadingNavigationSheetPeekHeight;


    public SSReadingViewModel(Context context, SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior, String ssLessonPath){
        ssReadingNavigationSheetPeekHeight = new ObservableInt(SSHelper.convertDpToPixels(context, PEEK_HEIGHT));

        this.context = context;
        this.ssReadingNavigationSheetBehavior = ssReadingNavigationSheetBehavior;
        this.ssLessonPath = ssLessonPath;

        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        final SSApplication ssApplication = SSApplication.get(context);

        subscription = ssApplication.getGithubService().getLessonInfo(ssLessonPath)
                .flatMap(new Func1<Response<SSLessonInfo>, Observable<Response<SSRead>>>() {
                    @Override
                    public Observable<Response<SSRead>> call(Response<SSLessonInfo> ssLessonInfoResponse) {
                        return ssApplication.getGithubService().getRead(ssLessonInfoResponse.body().days.get(0).read_path);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(ssApplication.defaultSubscribeScheduler())
                .subscribe(new Subscriber<Response<SSRead>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Response<SSRead> ssReadResponse) {
                        Log.d(TAG, ssReadResponse.body().content);
                    }
                });
    }

    private Subscriber<Response<SSLessonInfo>> getLessonInfoSubscriber(){
        return new Subscriber<Response<SSLessonInfo>>() {
            @Override
            public void onStart(){
                super.onStart();

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Response<SSLessonInfo> ssLessonInfoResponse) {
                String etag = ssLessonInfoResponse.headers().get("etag");

                if (ssLessonInfoResponse.body() != null){
                    Log.d(TAG, ssLessonInfoResponse.body().lesson.full_path);
                }
            }
        };
    }

    @BindingAdapter("app:behavior_peekHeight")
    public static void setBehaviorPeekHeight(final View v, float peekHeight) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)v.getLayoutParams();
        final SSReadingNavigationSheetBehavior ssReadingNavigationSheetBehavior = (SSReadingNavigationSheetBehavior)params.getBehavior();

        if (ssReadingNavigationSheetBehavior == null){
            return;
        }

        int start = (peekHeight > 0) ? 0: ssReadingNavigationSheetBehavior.getPeekHeight();
        int end = (peekHeight > 0) ? (int) peekHeight : 0;
        ValueAnimator slideAnimator = ValueAnimator.ofInt(start, end).setDuration(300);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ssReadingNavigationSheetBehavior.setPeekHeight(value);
                v.requestLayout();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(slideAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        subscription = null;
        context = null;
    }

    public void onNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed){
        if (dyConsumed < 0) {
            ssReadingNavigationSheetPeekHeight.set(SSHelper.convertDpToPixels(context, PEEK_HEIGHT));
        } else if (dyConsumed > 0) {
            ssReadingNavigationSheetPeekHeight.set(0);
        }
    }

    public void onMenuClick(View view) {
        if (ssReadingNavigationSheetBehavior.getState() == SSReadingNavigationSheetBehavior.STATE_EXPANDED) {
            ssReadingNavigationSheetBehavior.setState(SSReadingNavigationSheetBehavior.STATE_COLLAPSED);
        } else {
            ssReadingNavigationSheetBehavior.setState(SSReadingNavigationSheetBehavior.STATE_EXPANDED);
        }
    }

    public interface DataListener {
        void onLessonInfoChanged(SSLessonInfo ssLessonInfo);
        void onReadChanged(SSRead ssRead);
    }
}
