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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.ObservableFloat;
import android.databinding.ObservableInt;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;

import com.crashlytics.android.Crashlytics;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSEvent;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.model.SSQuarterlyLanguage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SSQuarterliesViewModel implements SSViewModel {
    private static final String TAG = SSQuarterliesViewModel.class.getSimpleName();
    private static final int ANIMATION_DURATION = 300;

    private Context context;
    private DataListener dataListener;
    private List<SSQuarterly> ssQuarterlies;
    private List<SSQuarterlyLanguage> ssQuarterlyLanguages;
    private SSQuarterlyLanguage ssQuarterlyLanguage;
    private DatabaseReference ssFirebaseDatabase;
    private ValueEventListener ssLanguagesRef;
    private ValueEventListener ssQuarterliesRef;
    private String ssDefaultLanguage;

    public ObservableInt ssQuarterliesLanguageFilterVisibility;
    public ObservableInt ssQuarterliesLoadingVisibility;
    public ObservableInt ssQuarterliesListVisibility;
    public ObservableInt ssQuarterliesErrorMessageVisibility;
    public ObservableInt ssQuarterliesEmptyStateVisibility;
    public ObservableInt ssQuarterliesErrorStateVisibility;
    public ObservableFloat ssQuarterliesListMarginTop;

    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();
    private ViewPropertyAnimatorCompat mTranslationAnimator;

    public SSQuarterliesViewModel(Context context, final DataListener dataListener) {
        this.context = context;
        this.dataListener = dataListener;
        ssQuarterliesLoadingVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesListVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesLanguageFilterVisibility = new ObservableInt(View.GONE);
        ssQuarterliesErrorMessageVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesEmptyStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesErrorStateVisibility = new ObservableInt(View.INVISIBLE);
        ssQuarterliesListMarginTop = new ObservableFloat(0);
        ssFirebaseDatabase = FirebaseDatabase.getInstance().getReference();
        ssFirebaseDatabase.keepSynced(true);

        this.ssQuarterlyLanguages = new ArrayList<>();
        this.ssQuarterlies = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


        String ssLastLanguageSelected = prefs.getString(SSConstants.SS_LAST_LANGUAGE_INDEX, "");
        ssDefaultLanguage = Locale.getDefault().getLanguage();

        if (!ssLastLanguageSelected.isEmpty()){
            ssDefaultLanguage = ssLastLanguageSelected;
        }

        ssQuarterlyLanguage = new SSQuarterlyLanguage(ssDefaultLanguage, getDisplayLanguageByCode(ssDefaultLanguage), 1);

        loadLanguages();
    }

    private SSQuarterlyLanguage getSelectedLanguage(){
        return ssQuarterlyLanguage;
    }

    public void onFilterClick(MenuItem menuItem){
        if (ssQuarterliesLanguageFilterVisibility.get() == View.GONE) {

            View v = ((Activity)context).findViewById(R.id.ss_quarterlies_language_filter_holder);
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            animateOffset(((Activity)context).findViewById(R.id.ss_quarterlies_list), v.getMeasuredHeight());
            ssQuarterliesLanguageFilterVisibility.set(View.VISIBLE);

            menuItem.setIcon(new IconicsDrawable(context)
                    .icon(GoogleMaterial.Icon.gmd_close)
                    .color(Color.WHITE)
                    .sizeDp(14));

            SSEvent.track(SSConstants.SS_EVENT_LANGUAGE_FILTER);
        } else {
            ssQuarterliesListMarginTop.set(0);
            ssQuarterliesLanguageFilterVisibility.set(View.GONE);
            animateOffset(((Activity)context).findViewById(R.id.ss_quarterlies_list), 0);
            menuItem.setIcon(new IconicsDrawable(context)
                    .icon(GoogleMaterial.Icon.gmd_filter_list)
                    .color(Color.WHITE)
                    .sizeDp(18));
        }
    }

    private void animateOffset(final View child, final int offset) {
        ensureOrCancelAnimator(child);
        mTranslationAnimator.translationY(offset).start();
    }

    private void ensureOrCancelAnimator(View child) {
        if (mTranslationAnimator == null) {
            mTranslationAnimator = ViewCompat.animate(child);
            mTranslationAnimator.setDuration(ANIMATION_DURATION);
            mTranslationAnimator.setInterpolator(INTERPOLATOR);
        } else {
            mTranslationAnimator.cancel();
        }
    }

    private String getDisplayLanguageByCode(String languageCode){
        Locale locale = new Locale(languageCode);
        return org.apache.commons.lang3.StringUtils.capitalize(locale.getDisplayLanguage(Locale.getDefault()));
    }

    private void loadLanguages(){
        ssQuarterliesLoadingVisibility.set(View.VISIBLE);
        ssQuarterliesListVisibility.set(View.INVISIBLE);
        ssQuarterliesErrorMessageVisibility.set(View.INVISIBLE);
        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
        ssQuarterliesErrorStateVisibility.set(View.INVISIBLE);

        ssLanguagesRef = ssFirebaseDatabase.child(SSConstants.SS_FIREBASE_LANGUAGES_DATABASE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            Iterable<DataSnapshot> data = dataSnapshot.getChildren();

                            try {
                                if (ssQuarterlyLanguages == null) {
                                    ssQuarterlyLanguages = new ArrayList<>();
                                } else {
                                    ssQuarterlyLanguages.clear();
                                }

                                for (DataSnapshot d : data) {
                                    SSQuarterlyLanguage _ssQuarterlyLanguage = d.getValue(SSQuarterlyLanguage.class);
                                    _ssQuarterlyLanguage.name = getDisplayLanguageByCode(_ssQuarterlyLanguage.code);

                                    if (_ssQuarterlyLanguage.code.equalsIgnoreCase(ssDefaultLanguage)) {
                                        _ssQuarterlyLanguage.selected = 1;
                                        ssQuarterlyLanguage = _ssQuarterlyLanguage;
                                        ssQuarterlyLanguages.add(0, _ssQuarterlyLanguage);
                                    } else {
                                        ssQuarterlyLanguages.add(_ssQuarterlyLanguage);
                                    }
                                }

                                if (ssQuarterlyLanguages.size() > 0) {
                                    ssQuarterlyLanguage = ssQuarterlyLanguages.get(0);
                                    ssQuarterlyLanguage.selected = 1;
                                }

                                if (dataListener != null) dataListener.onQuarterliesLanguagesChanged(ssQuarterlyLanguages);
                                loadQuarterlies(getSelectedLanguage());
                            } catch (Exception e){
                                Crashlytics.log(Log.INFO, TAG, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ssQuarterliesErrorMessageVisibility.set(View.VISIBLE);
                        ssQuarterliesListVisibility.set(View.INVISIBLE);
                        ssQuarterliesLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterliesErrorStateVisibility.set(View.VISIBLE);
                    }
                });
    }

    private void loadQuarterlies(SSQuarterlyLanguage ssQuarterlyLanguage){
        ssQuarterliesRef = ssFirebaseDatabase.child(SSConstants.SS_FIREBASE_QUARTERLIES_DATABASE)
                .child(ssQuarterlyLanguage.code)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            try {
                                Iterable<DataSnapshot> data = dataSnapshot.getChildren();

                                if (ssQuarterlies == null) {
                                    ssQuarterlies = new ArrayList<>();
                                } else {
                                    ssQuarterlies.clear();
                                }

                                for(DataSnapshot d: data){
                                    ssQuarterlies.add(d.getValue(SSQuarterly.class));
                                }

                                if (dataListener != null) dataListener.onQuarterliesChanged(ssQuarterlies);

                                ssQuarterliesListVisibility.set(View.VISIBLE);
                                ssQuarterliesLoadingVisibility.set(View.INVISIBLE);
                                ssQuarterliesErrorMessageVisibility.set(View.INVISIBLE);
                                ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
                                ssQuarterliesErrorStateVisibility.set(View.INVISIBLE);

                                if (ssQuarterlies.size() == 0) {
                                    ssQuarterliesEmptyStateVisibility.set(View.VISIBLE);
                                }
                            } catch (Exception e){
                                Crashlytics.log(Log.INFO, TAG, e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        ssQuarterliesErrorMessageVisibility.set(View.VISIBLE);
                        ssQuarterliesListVisibility.set(View.INVISIBLE);
                        ssQuarterliesLoadingVisibility.set(View.INVISIBLE);
                        ssQuarterliesEmptyStateVisibility.set(View.INVISIBLE);
                        ssQuarterliesErrorStateVisibility.set(View.VISIBLE);
                    }
                });
    }

    @Override
    public void destroy() {
        context = null;
        dataListener = null;
        ssQuarterlies = null;
        ssQuarterlyLanguage = null;
        ssQuarterlyLanguages = null;

        if (ssLanguagesRef != null){
            ssFirebaseDatabase.removeEventListener(ssLanguagesRef);
        }

        if (ssQuarterliesRef != null){
            ssFirebaseDatabase.removeEventListener(ssQuarterliesRef);
        }

        ssFirebaseDatabase = null;
    }

    public void onChangeLanguageEvent(SSQuarterlyLanguage ssQuarterlyLanguage){
        this.ssQuarterlyLanguage = ssQuarterlyLanguage;

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(SSConstants.SS_LAST_LANGUAGE_INDEX, ssQuarterlyLanguage.code);
        editor.apply();

        this.loadQuarterlies(getSelectedLanguage());
    }

    public interface DataListener {
        void onQuarterliesChanged(List<SSQuarterly> ssQuarterlies);
        void onQuarterliesLanguagesChanged(List<SSQuarterlyLanguage> quarterlyLanguages);
    }
}
