package com.wanzi.appmanager

import android.graphics.drawable.Drawable

data class AppEntry(
        val name: String,           // 应用名
        val packageName: String,    // 包名
        val drawable: Drawable,     // 图标
        val version: String,        // 版本
        val sha1: String,           // SHA1
        var isDisable: Boolean      // 是否隐藏
) : Comparable<AppEntry> {
    override fun compareTo(other: AppEntry): Int {
        return this.name.compareTo(other.name)
    }

}