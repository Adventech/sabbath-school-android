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

package com.cryart.sabbathschool.lessons.ui.readings;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cryart.sabbathschool.lessons.R;
import com.cryart.sabbathschool.lessons.data.model.SSDay;
import com.cryart.sabbathschool.lessons.databinding.SsReadingItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class SSReadingSheetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SSDay> ssDays = Collections.emptyList();

    public SSReadingSheetAdapter() {
    }

    public void setDays(List<SSDay> ssDays) {
        this.ssDays = ssDays;
    }

    @Override
    public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        SsReadingItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_reading_item, parent, false);
        return new SSReadingListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, final int position) {
        SSReadingListViewHolder holder1 = (SSReadingListViewHolder) holder;
        holder1.bindQuarterly(ssDays.get(position));
        holder1.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return ssDays.size();
    }


    private static class SSReadingListViewHolder extends RecyclerView.ViewHolder {
        final SsReadingItemBinding binding;

        SSReadingListViewHolder(SsReadingItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterly(SSDay ssDay) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSReadingListItemViewModel(ssDay));
            } else {
                binding.getViewModel().setSSDay(ssDay);
            }
        }
    }
}

