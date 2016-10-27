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


import android.widget.SeekBar;

import com.cryart.sabbathschool.databinding.SsReadingDisplayOptionsBinding;
import com.cryart.sabbathschool.model.SSReadingDisplayOptions;

public class SSReadingDisplayOptionsViewModel implements SSViewModel {
    public SSReadingViewModel ssReadingViewModel;
    private SsReadingDisplayOptionsBinding binding;
    private SSReadingDisplayOptions ssReadingDisplayOptions;

    public SSReadingDisplayOptionsViewModel(SsReadingDisplayOptionsBinding binding, SSReadingViewModel ssReadingViewModel, SSReadingDisplayOptions ssReadingDisplayOptions){
        this.ssReadingDisplayOptions = ssReadingDisplayOptions;
        this.ssReadingViewModel = ssReadingViewModel;
        this.binding = binding;

        updateWidget();

        binding.ssReadingMenuDisplayOptionsSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i){
                    case 0: {
                        setSizeTiny();
                        break;
                    }

                    case 1: {
                        setSizeSmall();
                        break;
                    }

                    case 2: {
                        setSizeMedium();
                        break;
                    }

                    case 3: {
                        setSizeLarge();
                        break;
                    }

                    case 4: {
                        setSizeHuge();
                        break;
                    }

                    default: {
                        setSizeMedium();
                        break;
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void updateWidget(){
        switch(ssReadingDisplayOptions.size){
            case SSReadingDisplayOptions.SS_SIZE_TINY: {
                binding.ssReadingMenuDisplayOptionsSize.setProgress(0);
                break;
            }

            case SSReadingDisplayOptions.SS_SIZE_SMALL: {
                binding.ssReadingMenuDisplayOptionsSize.setProgress(1);
                break;
            }

            case SSReadingDisplayOptions.SS_SIZE_MEDIUM: {
                binding.ssReadingMenuDisplayOptionsSize.setProgress(2);
                break;
            }

            case SSReadingDisplayOptions.SS_SIZE_LARGE: {
                binding.ssReadingMenuDisplayOptionsSize.setProgress(3);
                break;
            }

            case SSReadingDisplayOptions.SS_SIZE_HUGE: {
                binding.ssReadingMenuDisplayOptionsSize.setProgress(4);
                break;
            }
        }
    }

    public void setFontAndada(){
        setFont(SSReadingDisplayOptions.SS_FONT_ANDADA);
    }

    public void setFontLato(){
        setFont(SSReadingDisplayOptions.SS_FONT_LATO);
    }

    public void setFontPTSerif(){
        setFont(SSReadingDisplayOptions.SS_FONT_PT_SERIF);
    }

    public void setFontPTSans(){
        setFont(SSReadingDisplayOptions.SS_FONT_PT_SANS);
    }

    public void setThemeLight(){
        setTheme(SSReadingDisplayOptions.SS_THEME_LIGHT);
    }

    public void setThemeSepia(){
        setTheme(SSReadingDisplayOptions.SS_THEME_SEPIA);
    }

    public void setThemeDark(){
        setTheme(SSReadingDisplayOptions.SS_THEME_DARK);
    }

    public void setSizeTiny(){
        setSize(SSReadingDisplayOptions.SS_SIZE_TINY);
    }

    public void setSizeSmall(){
        setSize(SSReadingDisplayOptions.SS_SIZE_SMALL);
    }

    public void setSizeMedium(){
        setSize(SSReadingDisplayOptions.SS_SIZE_MEDIUM);
    }

    public void setSizeLarge(){
        setSize(SSReadingDisplayOptions.SS_SIZE_LARGE);
    }

    public void setSizeHuge(){
        setSize(SSReadingDisplayOptions.SS_SIZE_HUGE);
    }

    private void setTheme(String theme){
        ssReadingDisplayOptions.theme = theme;
        relaySSReadingDisplayOptions();
    }

    private void setSize(String size){
        ssReadingDisplayOptions.size = size;
        relaySSReadingDisplayOptions();
    }

    private void setFont(String font){
        ssReadingDisplayOptions.font = font;
        relaySSReadingDisplayOptions();
    }

    private void relaySSReadingDisplayOptions(){
        ssReadingViewModel.onSSReadingDisplayOptions(ssReadingDisplayOptions);
    }

    @Override
    public void destroy(){}
}
