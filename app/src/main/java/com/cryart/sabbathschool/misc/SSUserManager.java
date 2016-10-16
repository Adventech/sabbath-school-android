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

package com.cryart.sabbathschool.misc;

import com.cryart.sabbathschool.SSApplication;
import com.cryart.sabbathschool.model.SSUser;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

public class SSUserManager {
    private static final String SS_USER_CACHE_KEY = "user";
    private static SSUserManager instance;
    public SSUser user;

    public void setUser(SSUser user){
        this.user = user;
        try {
            DB snappydb = DBFactory.open(SSApplication.get());
            snappydb.put(SS_USER_CACHE_KEY, user);
            snappydb.close();
        } catch (SnappydbException e) {}
    }

    public static SSUserManager getInstance(){
        if (instance == null) instance = getInstanceSync();
        return instance;
    }

    private static synchronized SSUserManager getInstanceSync(){
        if (instance == null){
            instance = new SSUserManager();

            SSUser _user = null;

            try {
                DB snappydb = DBFactory.open(SSApplication.get());
                _user = snappydb.getObject(SS_USER_CACHE_KEY, SSUser.class);
                snappydb.close();
            } catch (SnappydbException e) {}

            if (_user == null){
                _user = new SSUser("Anonymous", "anonymous@adventech.io", "https://pbs.twimg.com/profile_images/666965875350708224/fAXZsqgw_400x400.png");
            }

            instance.user = _user;
        }
        return instance;
    }
}
