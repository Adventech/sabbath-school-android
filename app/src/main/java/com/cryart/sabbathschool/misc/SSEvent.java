/*
 * Copyright (c) 2017 Adventech.
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

package com.cryart.sabbathschool.misc;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class SSEvent {
    public static void track(String event_name){
        SSEvent.track(event_name, new HashMap<String, Object>());
    }

    public static void track(String event_name, HashMap<String, ?> values){
        FirebaseAuth ssFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser ssUser = ssFirebaseAuth.getCurrentUser();

        CustomEvent newEvent = new CustomEvent(event_name);
        if (ssUser != null && ssUser.getDisplayName() != null){
            newEvent.putCustomAttribute(SSConstants.SS_EVENT_PARAM_USER_ID, ssUser.getUid());
            newEvent.putCustomAttribute(SSConstants.SS_EVENT_PARAM_USER_NAME, ssUser.getDisplayName());
        } else {
            newEvent.putCustomAttribute(SSConstants.SS_EVENT_PARAM_USER_NAME, "Anonymous");
        }

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (entry.getValue() instanceof Integer){
                newEvent.putCustomAttribute(entry.getKey(), (Integer)entry.getValue());
            } else {
                newEvent.putCustomAttribute(entry.getKey(), (String)entry.getValue());
            }
        }

        Answers.getInstance().logCustom(newEvent);
    }
}
