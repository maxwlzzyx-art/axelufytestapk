package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun getInstalledLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedActivities = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
        val myPackage = context.packageName

        resolvedActivities
            .mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == myPackage) return@mapNotNull null
                val label = resolveInfo.loadLabel(pm).toString()
                InstalledApp(
                    packageName = pkgName,
                    appName = label.ifBlank { pkgName }
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName.lowercase() }
    }
}
