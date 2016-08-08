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
import android.app.Activity;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.ObservableFloat;
import android.databinding.ObservableInt;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.api.SSApiService;
import com.cryart.sabbathschool.bus.SSBusProvider;
import com.cryart.sabbathschool.event.SSLanguageFilterChangeEvent;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.model.SSQuarterlyLanguage;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SSQuarterliesViewModel implements SSViewModel {
    private static final String TAG = SSQuarterliesViewModel.class.getSimpleName();
    private static final int ANIMATION_DURATION = 300;

    private Context context;
    private Subscription subscription;
    private List<SSQuarterly> quarterlies;
    private List<SSQuarterlyLanguage> quarterlyLanguages;
    private DataListener dataListener;

    public ObservableInt ssQuarterliesLanguageFilterVisibility;
    public ObservableInt ssQuarterliesLoadingVisibility;
    public ObservableInt ssQuarterliesListVisibility;
    public ObservableInt ssQuarterliesErrorMessageVisibility;
    public ObservableInt ssQuarterliesEmptyStateVisibility;
    public ObservableInt ssQuarterliesErrorStateVisibility;

    public ObservableFloat ssQuarterliesListMarginTop;

    public SSQuarterliesViewModel(Context context, DataListener dataListener) {
        this.context = context;
        this.dataListener = dataListener;
        ssQuarterliesLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesListVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesLanguageFilterVisibility = new ObservableInt(View.GONE);
        ssQuarterliesErrorMessageVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesListMarginTop = new ObservableFloat(0);

        this.quarterlyLanguages = new ArrayList<>();
        this.quarterlies = new ArrayList<>();

        String[] language_codes = context.getResources().getStringArray(R.array.ss_quarterlies_language_codes);
        String[] language_names = context.getResources().getStringArray(R.array.ss_quarterlies_language_names);
        int[] language_selects = context.getResources().getIntArray(R.array.ss_quarterlies_language_selects);

        for (int i = 0; i < language_codes.length; i++){
            this.quarterlyLanguages.add(new SSQuarterlyLanguage(language_codes[i], language_names[i], language_selects[i]));
        }

        dataListener.onQuarterliesLanguagesChanged(quarterlyLanguages);

        loadQuarterlies();
        SSBusProvider.getInstance().register(this);

    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayoutTopMargin(final View v, float topMargin) {
        int start = (topMargin > 0) ? 0: ((ViewGroup.MarginLayoutParams)v.getLayoutParams()).topMargin;
        int end = (topMargin > 0) ? (int) topMargin : 0;

        ValueAnimator slideAnimator = ValueAnimator.ofInt(start, end).setDuration(ANIMATION_DURATION);
        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)v.getLayoutParams();
                layoutParams.topMargin = value;
                v.setLayoutParams(layoutParams);
                v.requestLayout();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(slideAnimator);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

//    @BindingAdapter("android:layout_height")
//    public static void setLayoutHeight(final View v, float height) {
//        final int start = (height > 0) ? 0: v.getLayoutParams().height;
//        final int end = (height > 0) ? (int) height : 0;
//
//        ValueAnimator slideAnimator = ValueAnimator.ofInt(start, end).setDuration(300);
//        slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                Integer value = (Integer) animation.getAnimatedValue();
//                v.getLayoutParams().height = value;
//                v.requestLayout();
//            }
//        });
//
//        slideAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                if (start == 0) v.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                if (end == 0) v.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//
//        AnimatorSet set = new AnimatorSet();
//        set.play(slideAnimator);
//        set.setInterpolator(new AccelerateDecelerateInterpolator());
//        set.start();
//    }

    public void onFilterClick(MenuItem menuItem){
        if (ssQuarterliesLanguageFilterVisibility.get() == View.GONE) {
            View v = ((Activity)context).findViewById(R.id.ss_quarterlies_language_filter_holder);
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            ssQuarterliesListMarginTop.set(v.getMeasuredHeight());
            ssQuarterliesLanguageFilterVisibility.set(View.VISIBLE);

            menuItem.setIcon(R.drawable.ic_close_24dp);
        } else {
            ssQuarterliesListMarginTop.set(0);
            ssQuarterliesLanguageFilterVisibility.set(View.GONE);
            menuItem.setIcon(R.drawable.ic_filter_list_24dp);
        }
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    private void loadQuarterlies(){
        ssQuarterliesLoadingVisibility.set(View.VISIBLE);
        ssQuarterliesListVisibility.set(View.INVISIBLE);
        ssQuarterliesErrorMessageVisibility.set(View.INVISIBLE);
        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
        ssQuarterliesErrorStateVisibility.set(View.INVISIBLE);

        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        SSApplication ssApplication = SSApplication.get(context);
        final SSApiService ssApiService = ssApplication.getGithubService();

        SSQuarterlyLanguage ssQuarterlyLanguage = null;

        for(SSQuarterlyLanguage lang : quarterlyLanguages){
            if (lang.selected == 1){
                ssQuarterlyLanguage = lang;
                break;
            }
        }

        if (ssQuarterlyLanguage == null) ssQuarterlyLanguage = new SSQuarterlyLanguage("en", "English", 1);

        final String quarterliesCacheKey = ssQuarterlyLanguage.code;

        final Observable<List<SSQuarterly>> cache = Observable.create(
                new Observable.OnSubscribe<List<SSQuarterly>>() {
                    @Override
                    public void call(Subscriber<? super List<SSQuarterly>> sub) {
                        try {
                            DB snappydb = DBFactory.open(context);
                            SSQuarterly[] categoryList = null;

                            categoryList = snappydb.getObjectArray(quarterliesCacheKey, SSQuarterly.class);
                            if (categoryList != null) {

                                List<SSQuarterly> s = Arrays.asList(categoryList);
                                if (s.size() > 0) {
                                    Log.d(TAG, "CACHEEE!");
                                    sub.onNext(s);
                                }
                            }
                            snappydb.close();

                        } catch (SnappydbException e) {}

                        sub.onCompleted();
                    }
                }
        );

//        Observable<Observable<List<SSQuarterly>>> o =
//                Observable.create(new Observable.OnSubscribe<Observable<List<SSQuarterly>>>(){
//            @Override
//            public void call(Subscriber<? super Observable<List<SSQuarterly>>> sub){
//                sub.onNext(cache);
//
//                for(SSQuarterlyLanguage lang : quarterlyLanguages){
//                    if (lang.selected == 1){
//                        sub.onNext(ssApiService.getQuarterlies(lang.code));
//                    }
//                }
//
//                sub.onCompleted();
//            }
//        });



        quarterlies.clear();

        subscription = Observable
                .concat(cache, ssApiService.getQuarterlies(ssQuarterlyLanguage.code))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(ssApplication.defaultSubscribeScheduler())
                .subscribe(new Subscriber<List<SSQuarterly>>() {
                    @Override
                    public void onCompleted() {
                        if (quarterlies.size() > 0) {
                            dataListener.onQuarterliesChanged(quarterlies);

                            try {
                                DB snappydb = DBFactory.open(context);

                                snappydb.put(quarterliesCacheKey, quarterlies.toArray());
                                snappydb.close();

                            } catch (SnappydbException e) {}
                        }

                        ssQuarterliesListVisibility.set(View.VISIBLE);
                        ssQuarterliesLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterliesErrorMessageVisibility.set(View.INVISIBLE);
                        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterliesErrorStateVisibility.set(View.INVISIBLE);

                        if (quarterlies.size() == 0){
                            ssQuarterliesEmptyStateVisibility.set(View.VISIBLE);
                        }

                        if (dataListener != null){
                            dataListener.onQuarterliesChanged(quarterlies);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ssQuarterliesErrorMessageVisibility.set(View.VISIBLE);
                        ssQuarterliesListVisibility.set(View.INVISIBLE);
                        ssQuarterliesLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterliesErrorStateVisibility.set(View.VISIBLE);
                    }

                    @Override
                    public void onNext(List<SSQuarterly> ssQuarterlies) {
                        quarterlies.addAll(ssQuarterlies);
                    }
                });
    }

    @Override
    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        subscription = null;
        context = null;
        dataListener = null;
        SSBusProvider.getInstance().unregister(this);
    }

    public interface DataListener {
        void onQuarterliesChanged(List<SSQuarterly> quarterlies);
        void onQuarterliesLanguagesChanged(List<SSQuarterlyLanguage> quarterlyLanguages);
    }

    @Subscribe
    public void onChangeLanguageEvent(SSLanguageFilterChangeEvent event){
        this.loadQuarterlies();
    }
}
