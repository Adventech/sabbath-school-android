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

package com.cryart.sabbathschool.bible.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.cryart.sabbathschool.bible.R;
import com.cryart.sabbathschool.bible.databinding.SsBibleVersionItemBinding;
import com.cryart.sabbathschool.bible.databinding.SsBibleVersionItemHeaderBinding;
import com.cryart.sabbathschool.core.model.SSBibleVerses;

import java.util.List;

public class SSBibleVersionsAdapter extends BaseAdapter {
    List<SSBibleVerses> ssBibleVerses;

    public SSBibleVersionsAdapter(List<SSBibleVerses> ssBibleVerses) {
        this.ssBibleVerses = ssBibleVerses;
    }

    @Override
    public int getCount() {
        return ssBibleVerses.size();
    }


    @Override
    public Object getItem(int position) {
        return ssBibleVerses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            SsBibleVersionItemHeaderBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_bible_version_item_header, parent, false);
            view = binding.getRoot();
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(((SSBibleVerses) getItem(position)).name);

        return view;
    }


    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null) {
            SsBibleVersionItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_bible_version_item, parent, false);
            view = binding.getRoot();
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(((SSBibleVerses) getItem(position)).name);
        return view;
    }
}
