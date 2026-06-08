package org.monogram.presentation.core.util

import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.v2.OssLicensesMenuActivity

internal object OssLicensesNavigator {
    fun open(context: Context) {
        val intent = Intent(context, OssLicensesMenuActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
