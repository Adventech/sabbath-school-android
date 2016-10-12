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
import android.databinding.ObservableInt;
import android.util.Log;
import android.view.View;

import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.api.SSApiService;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class SSQuarterlyViewModel implements SSViewModel {
    private static final String TAG = SSQuarterlyViewModel.class.getSimpleName();
    private Context context;
    private Subscription subscription;
    private SSQuarterly ssQuarterly;
    private String ssQuarterlyId;
    private DataListener dataListener;
    
    public ObservableInt ssQuarterlyLoadingVisibility;
    public ObservableInt ssQuarterlyErrorMessageVisibility;
    public ObservableInt ssQuarterlyEmptyStateVisibility;
    public ObservableInt ssQuarterlyErrorStateVisibility;
    public ObservableInt ssQuarterlyCoordinatorVisibility;

    public SSQuarterlyViewModel(Context context, DataListener dataListener, String ssQuarterlyId) {
        this.context = context;
        this.dataListener = dataListener;
        this.ssQuarterlyId = ssQuarterlyId;

        ssQuarterlyLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterlyErrorMessageVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterlyEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterlyErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterlyCoordinatorVisibility = new ObservableInt(View.INVISIBLE);

        loadQuarterly();
    }

    private void loadQuarterly() {
        ssQuarterlyLoadingVisibility.set(View.VISIBLE);
        ssQuarterlyErrorMessageVisibility.set(View.INVISIBLE);
        ssQuarterlyEmptyStateVisibility.set(View.INVISIBLE);
        ssQuarterlyErrorStateVisibility.set(View.INVISIBLE);

        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        SSApplication ssApplication = SSApplication.get(context);
        final SSApiService ssApiService = ssApplication.getGithubService();

        final Observable<SSQuarterly> cache = Observable.create(
                new Observable.OnSubscribe<SSQuarterly>() {
                    @Override
                    public void call(Subscriber<? super SSQuarterly> sub) {
                        try {
                            DB snappydb = DBFactory.open(context);
                            SSQuarterly ssQuarterlyCache = null;

                            ssQuarterlyCache = snappydb.getObject(ssQuarterlyId, SSQuarterly.class);
                            if (ssQuarterlyCache != null) {

                                sub.onNext(ssQuarterlyCache);
                            }
                            snappydb.close();

                        } catch (SnappydbException e) {}

                        sub.onCompleted();
                    }
                }
        );

        subscription = Observable
                .concat(cache, ssApiService.getQuarterly(ssQuarterlyId))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(ssApplication.defaultSubscribeScheduler())
                .subscribe(new Subscriber<SSQuarterly>() {
                    @Override
                    public void onCompleted() {

                        dataListener.onQuarterlyChanged(ssQuarterly);
                        Log.d(TAG, ssQuarterly.toString());

//                            try {
//                                DB snappydb = DBFactory.open(context);
//
//                                snappydb.put(ssQuarterlyId, ssQuarterly);
//                                snappydb.close();
//
//                            } catch (SnappydbException e) {}

                        ssQuarterlyLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterlyErrorMessageVisibility.set(View.INVISIBLE);
                        ssQuarterlyEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterlyErrorStateVisibility.set(View.INVISIBLE);
                        ssQuarterlyCoordinatorVisibility.set(View.VISIBLE);

                        if (dataListener != null){
                            dataListener.onQuarterlyChanged(ssQuarterly);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ssQuarterlyErrorMessageVisibility.set(View.VISIBLE);
                        ssQuarterlyLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterlyEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterlyCoordinatorVisibility.set(View.INVISIBLE);

                        ssQuarterlyErrorStateVisibility.set(View.VISIBLE);
                    }

                    @Override
                    public void onNext(SSQuarterly _ssQuarterly) {
                        ssQuarterly = _ssQuarterly;
                    }
                });
    }

    @Override
    public void destroy() {
        if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
        subscription = null;
        context = null;
        dataListener = null;
    }

    public void setDataListener(DataListener dataListener) {
        this.dataListener = dataListener;
    }

    public interface DataListener {
        void onQuarterlyChanged(SSQuarterly ssQuarterly);
    }
}