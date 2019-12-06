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

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsQuarterlyLanguageItemBinding;
import com.cryart.sabbathschool.model.SSQuarterlyLanguage;
import com.cryart.sabbathschool.viewmodel.SSQuarterliesViewModel;
import com.cryart.sabbathschool.viewmodel.SSQuarterlyLanguageItemViewModel;

import java.util.Collections;
import java.util.List;

public class SSQuarterliesLanguageFilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SSQuarterlyLanguageItemViewModel.SSQuarterliesLanguageChangeListener{
    private List<SSQuarterlyLanguage> ssQuarterlyLanguages;
    private SSQuarterliesViewModel ssQuarterliesViewModel;

    public SSQuarterliesLanguageFilterAdapter() {
        this.ssQuarterlyLanguages = Collections.emptyList();
    }

    public SSQuarterliesLanguageFilterAdapter(List<SSQuarterlyLanguage> ssQuarterlyLanguages) {
        this.ssQuarterlyLanguages = ssQuarterlyLanguages;
    }

    public void setQuarterlyLanguages(List<SSQuarterlyLanguage> ssQuarterlyLanguages) {
        this.ssQuarterlyLanguages = ssQuarterlyLanguages;
    }

    public void setQuarterliesViewModel(SSQuarterliesViewModel ssQuarterliesViewModel){
        this.ssQuarterliesViewModel = ssQuarterliesViewModel;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        SsQuarterlyLanguageItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_quarterly_language_item, parent, false);
        viewHolder = new SSQuarterliesLanguageFilterHolder(binding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SSQuarterliesLanguageFilterHolder holder1 = (SSQuarterliesLanguageFilterHolder) holder;
        holder1.bindQuarterlyLanguage(ssQuarterlyLanguages.get(position), ssQuarterliesViewModel);
        holder1.binding.getViewModel().setChangeListener(this);
        holder1.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return ssQuarterlyLanguages.size();
    }

    @Override
    public boolean onLanguageCheck(String code) {
        for(SSQuarterlyLanguage lang : ssQuarterlyLanguages){
            if (!lang.code.equals(code)) lang.selected = 0;
        }

        notifyDataSetChanged();
        return true;
    }

    private static class SSQuarterliesLanguageFilterHolder extends RecyclerView.ViewHolder {
        final SsQuarterlyLanguageItemBinding binding;

        SSQuarterliesLanguageFilterHolder(SsQuarterlyLanguageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterlyLanguage(SSQuarterlyLanguage ssQuarterlyLanguage, SSQuarterliesViewModel ssQuarterliesViewModel) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSQuarterlyLanguageItemViewModel(itemView.getContext(), ssQuarterlyLanguage, ssQuarterliesViewModel));
            } else {
                binding.getViewModel().setSsQuarterlyLanguage(ssQuarterlyLanguage);
            }
        }
    }
}
