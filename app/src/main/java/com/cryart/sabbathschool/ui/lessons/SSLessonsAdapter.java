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

package com.cryart.sabbathschool.ui.lessons;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.cryart.sabbathschool.R;
import com.cryart.sabbathschool.databinding.SsLessonItemBinding;
import com.cryart.sabbathschool.model.SSLesson;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class SSLessonsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SSLesson> ssLessons;

    SSLessonsAdapter() {
        this.ssLessons = Collections.emptyList();
    }

    public void setLessons(List<SSLesson> ssLessonsInfo) {
        this.ssLessons = ssLessonsInfo;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        SsLessonItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.ss_lesson_item, parent, false);
        return new SSLessonViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        SSLessonViewHolder holder1 = (SSLessonViewHolder) holder;
        holder1.bindQuarterly(ssLessons.get(position));
        holder1.binding.executePendingBindings();
        holder1.binding.ssLessonItemIndex.setText(String.valueOf(position + 1));
    }

    @Override
    public int getItemCount() {
        return ssLessons.size();
    }

    private static class SSLessonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final SsLessonItemBinding binding;

        SSLessonViewHolder(SsLessonItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindQuarterly(SSLesson ssLesson) {
            if (binding.getViewModel() == null) {
                binding.setViewModel(new SSLessonItemViewModel(itemView.getContext(), ssLesson));
            } else {
                binding.getViewModel().setSsLesson(ssLesson);
            }
        }

        @Override
        public void onClick(View view) {
            final PopupMenu popupMenu = new PopupMenu(itemView.getContext(), view);
            final Menu menu = popupMenu.getMenu();

            popupMenu.getMenuInflater().inflate(R.menu.ss_lesson_item_menu, menu);
            popupMenu.setGravity(Gravity.END);
            popupMenu.show();
        }
    }
}
