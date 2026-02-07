package com.jet.admob

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper


/**
 * Helper function to find the current Activity from a given Context.
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 06.02.2026
 */
internal fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
