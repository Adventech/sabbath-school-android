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

package com.cryart.sabbathschool.view;


import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.adapter.SSBibleVersionsAdapter;
import com.cryart.sabbathschool.databinding.SsBibleVersesActivityBinding;
import com.cryart.sabbathschool.misc.SSColorTheme;
import com.cryart.sabbathschool.misc.SSConstants;
import com.cryart.sabbathschool.misc.SSEvent;
import com.cryart.sabbathschool.model.SSBibleVerses;
import com.cryart.sabbathschool.model.SSRead;
import com.cryart.sabbathschool.viewmodel.SSBibleVersesViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SSBibleVersesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SsBibleVersesActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.ss_bible_verses_activity);
        final String verse = getIntent().getExtras().getString(SSConstants.SS_READ_VERSE_EXTRA);
        String readIndex = getIntent().getExtras().getString(SSConstants.SS_READ_INDEX_EXTRA);

        if (readIndex == null) finish();

        binding.setViewModel(new SSBibleVersesViewModel(this));
        binding.ssBibleVersesHeader.setBackgroundColor(Color.parseColor(SSColorTheme.getInstance().getColorPrimary()));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        SSEvent.track(SSConstants.SS_EVENT_BIBLE_OPEN);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
                .child(readIndex)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {

                            final SSRead ssRead = dataSnapshot.getValue(SSRead.class);

                            SSBibleVersionsAdapter ssBibleVersionsAdapter = new SSBibleVersionsAdapter(ssRead.bible);
                            binding.ssReadingBibleVersionList.setAdapter(ssBibleVersionsAdapter);

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SSApplication.get());
                            String lastBibleVersionUsed = prefs.getString(SSConstants.SS_LAST_BIBLE_VERSION_USED, null);

                            if (lastBibleVersionUsed != null){
                                int i = 0;
                                for (SSBibleVerses _ssBibleVerses : ssRead.bible){
                                    if (_ssBibleVerses.name.equalsIgnoreCase(lastBibleVersionUsed)){
                                        binding.ssReadingBibleVersionList.setSelection(i);
                                        break;
                                    }
                                    i++;
                                }
                            } else {
                                binding.ssReadingBibleVersionList.setSelection(0);
                            }

                            binding.ssReadingBibleVersionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    SSBibleVerses ssBibleVerses = (SSBibleVerses) adapterView.getItemAtPosition(i);
                                    SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(SSApplication.get());
                                    SharedPreferences.Editor editor = shared.edit();
                                    editor.putString(SSConstants.SS_LAST_BIBLE_VERSION_USED, ssBibleVerses.name);
                                    editor.apply();
                                    binding.ssBibleVersesView.loadVerse(ssBibleVerses, verse);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> arg0) {}
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
