package com.wanzi.appmanager

import android.graphics.drawable.Drawable

data class AppEntry(
        val name: String,
        val packageName: String,
        val drawable: Drawable,
        var isDisable: Boolean
) : Comparable<AppEntry> {
    override fun compareTo(other: AppEntry): Int {
        return this.name.compareTo(other.name)
    }

}