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

package com.cryart.sabbathschool.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsQuarterlyItemFeaturedBinding;
import com.cryart.sabbathschool.databinding.SsQuarterlyItemNormalBinding;
import com.cryart.sabbathschool.model.SSQuarterly;
import com.cryart.sabbathschool.viewmodel.SSQuarterlyItemViewModel;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SSQuarterliesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SSQuarterly> quarterlies;
    private static final int SSQuarterlyViewHolderHero = 0;
    private static final int SSQuarterlyViewHolderNormal = 1;

    public SSQuarterliesAdapter() {
        this.quarterlies = Collections.emptyList();
    }

    public void setQuarterlies(List<SSQuarterly> quarterlies) {
        this.quarterlies = quarterlies;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? SSQuarterlyViewHolderHero : SSQuarterlyViewHolderNormal;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case SSQuarterlyViewHolderHero: {
                SsQuarterlyItemFeaturedBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_quarterly_item_featured, parent, false);
                binding.setSsQuarterlyCover(binding.ssQuarterlyItemCover);
                viewHolder = new SSQuarterlyViewHolderFeatured(binding);
                break;
            }

            default:
            case SSQuarterlyViewHolderNormal: {
                SsQuarterlyItemNormalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_quarterly_item_normal, parent, false);
                binding.setSsQuarterlyCover(binding.ssQuarterlyItemNormalCover);
                viewHolder = new SSQuarterlyViewHolderNormal(binding);
                break;
            }
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SSQuarterlyViewHolderHero: {
                SSQuarterlyViewHolderFeatured holder1 = (SSQuarterlyViewHolderFeatured) holder;
                holder1.bindQuarterly(quarterlies.get(position));
                holder1.binding.executePendingBindings();
                break;
            }

            default:
            case SSQuarterlyViewHolderNormal: {
                SSQuarterlyViewHolderNormal holder1 = (SSQuarterlyViewHolderNormal) holder;
                holder1.bindQuarterly(quarterlies.get(position));
                holder1.binding.executePendingBindings();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return quarterlies.size();
    }

    private static class SSQuarterlyViewHolderFeatured extends RecyclerView.ViewHolder {
        final SsQuarterlyItemFeaturedBinding binding;

        SSQuarterlyViewHolderFeatured(SsQuarterlyItemFeaturedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterly(SSQuarterly quarterly) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSQuarterlyItemViewModel(itemView.getContext(), quarterly));
            } else {
                binding.getViewModel().setSsQuarterly(quarterly);
            }
        }
    }

    private static class SSQuarterlyViewHolderNormal extends RecyclerView.ViewHolder {
        final SsQuarterlyItemNormalBinding binding;

        SSQuarterlyViewHolderNormal(SsQuarterlyItemNormalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterly(SSQuarterly quarterly) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSQuarterlyItemViewModel(itemView.getContext(), quarterly));
            } else {
                binding.getViewModel().setSsQuarterly(quarterly);
            }

            binding.sectionTitle.setVisibility(getAdapterPosition() == 1 ? View.VISIBLE : View.GONE);
        }
    }
}
