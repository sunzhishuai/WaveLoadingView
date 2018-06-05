package com.reliance.waveloadingview;

import android.content.Context;

/**
 * Created by sunzhishuai on 17/3/5.
 * E-mail itzhishuaisun@sina.com
 */

public class Utils {
    public static int convertDpToPixel(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);

    }




}
