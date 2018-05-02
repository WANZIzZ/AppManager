package com.wanzi.appmanager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlin.collections.ArrayList

object AppUtils {

    /**
     * 获取应用列表
     *
     * @param context
     * @param type    类型
     * @return
     */
    fun getApps(context: Context, type: AppType): List<AppEntry> {
        val allApps = ArrayList<AppEntry>()
        val systemApps = ArrayList<AppEntry>()
        val otherApps = ArrayList<AppEntry>()

        val pckManager = context.packageManager
        val packageInfo = pckManager.getInstalledPackages(0)
        for (info in packageInfo) {
            val entry = AppEntry(
                    info.applicationInfo.loadLabel(pckManager) as String,
                    info.packageName,
                    info.applicationInfo.loadIcon(pckManager),
                    isDisable(context, info.packageName)
            )

            if ((info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0) {
                systemApps.add(entry)
            } else {
                otherApps.add(entry)
            }
        }

        allApps.addAll(systemApps)
        allApps.addAll(allApps.size, otherApps)

        return when (type) {
            AppType.SYSTEM_APP -> systemApps
            AppType.OTHER_APP -> otherApps
            else -> allApps
        }
    }

    /**
     * 判断应用是否被禁用
     *
     * @param context
     * @param packageName
     * @return
     */
    private fun isDisable(context: Context, packageName: String): Boolean {
        val pckManager = context.packageManager
        val flag = pckManager.getApplicationEnabledSetting(packageName)
        if (flag == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            return true
        } else if (flag == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || flag == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            return false
        }
        return false
    }


}