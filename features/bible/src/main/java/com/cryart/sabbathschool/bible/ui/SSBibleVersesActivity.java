/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
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

package com.cryart.sabbathschool.bible.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cryart.sabbathschool.bible.R;
import com.cryart.sabbathschool.bible.databinding.SsBibleVersesActivityBinding;
import com.cryart.sabbathschool.core.misc.SSColorTheme;
import com.cryart.sabbathschool.core.misc.SSConstants;
import com.cryart.sabbathschool.core.misc.SSEvent;
import com.cryart.sabbathschool.core.model.SSBibleVerses;
import com.cryart.sabbathschool.core.model.SSRead;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class SSBibleVersesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SsBibleVersesActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.ss_bible_verses_activity);
        final String verse = getIntent().getExtras().getString(SSConstants.SS_READ_VERSE_EXTRA);
        String readIndex = getIntent().getExtras().getString(SSConstants.SS_READ_INDEX_EXTRA);

        if (readIndex == null) finish();

        binding.setViewModel(new SSBibleVersesViewModel(this));
        binding.ssBibleVersesHeader.setBackgroundColor(Color.parseColor(SSColorTheme.getInstance(this).getColorPrimary()));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        SSEvent.track(this, SSConstants.SS_EVENT_BIBLE_OPEN);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mDatabase.child(SSConstants.SS_FIREBASE_READS_DATABASE)
            .child(readIndex)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    final SSRead ssRead = dataSnapshot.getValue(SSRead.class);

                    SSBibleVersionsAdapter ssBibleVersionsAdapter = new SSBibleVersionsAdapter(ssRead.bible);
                    binding.ssReadingBibleVersionList.setAdapter(ssBibleVersionsAdapter);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SSBibleVersesActivity.this);
                    String lastBibleVersionUsed = prefs.getString(SSConstants.SS_LAST_BIBLE_VERSION_USED, null);

                    if (lastBibleVersionUsed != null) {
                        int i = 0;
                        for (SSBibleVerses _ssBibleVerses : ssRead.bible) {
                            if (_ssBibleVerses.name.equalsIgnoreCase(lastBibleVersionUsed)) {
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
                            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                            SharedPreferences.Editor editor = shared.edit();
                            editor.putString(SSConstants.SS_LAST_BIBLE_VERSION_USED, ssBibleVerses.name);
                            editor.apply();
                            binding.ssBibleVersesView.loadVerse(ssBibleVerses, verse);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                        }
                    });
                }

                @Override
                public void onCancelled(@NotNull DatabaseError databaseError) {
                    Timber.e(databaseError.toException());
                }
            });
    }
}
