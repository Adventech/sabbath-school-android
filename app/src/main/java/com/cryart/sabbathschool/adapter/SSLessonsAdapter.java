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

package com.cryart.sabbathschool.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsLessonItemBinding;
import com.cryart.sabbathschool.model.SSLessonInfo;
import com.cryart.sabbathschool.viewmodel.SSLessonInfoViewModel;

import java.util.Collections;
import java.util.List;

public class SSLessonsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SSLessonInfo> ssLessonsInfo;
    private int selectedPos = 0;

    public SSLessonsAdapter() {
        this.ssLessonsInfo = Collections.emptyList();
    }

    public SSLessonsAdapter(List<SSLessonInfo> ssLessonsInfo) {
        this.ssLessonsInfo = ssLessonsInfo;
    }

    public void setLessons(List<SSLessonInfo> ssLessonsInfo) {
        this.ssLessonsInfo = ssLessonsInfo;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SsLessonItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_lesson_item, parent, false);
        return new SSLessonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        SSLessonViewHolder holder1 = (SSLessonViewHolder) holder;
        holder1.bindQuarterly(ssLessonsInfo.get(position));
        holder1.binding.executePendingBindings();

        holder1.binding.ssLessonItemNormalIndex.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        return ssLessonsInfo.size();
    }


    public static class SSLessonViewHolder extends RecyclerView.ViewHolder {
        final SsLessonItemBinding binding;

        public SSLessonViewHolder(SsLessonItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterly(SSLessonInfo ssLessonInfo) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSLessonInfoViewModel(itemView.getContext(), ssLessonInfo));
            } else {
                binding.getViewModel().setSsLessonInfo(ssLessonInfo);
            }
        }
    }
}
