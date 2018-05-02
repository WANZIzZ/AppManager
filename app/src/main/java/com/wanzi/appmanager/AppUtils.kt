package com.wanzi.appmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import java.io.ByteArrayInputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import kotlin.collections.ArrayList

object AppUtils {


    /**
     * 获取应用列表
     *
     * @param context
     * @param type    类型
     * @return
     */
    @SuppressLint("PackageManagerGetSignatures")
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
                    "${info.versionName}(${info.versionCode})",
                    getCertificateSHA1Fingerprint(
                            pckManager
                                    .getPackageInfo(
                                            info.packageName,
                                            PackageManager.GET_SIGNATURES
                                    )
                                    .signatures[0]
                    ),
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

    /**
     * 获取SHAI
     *
     * @param signature 签名文件
     * @return
     */
    private fun getCertificateSHA1Fingerprint(signature: Signature): String {
        val cert = signature.toByteArray()
        // 将签名转换为字节数组流
        val input = ByteArrayInputStream(cert)
        // 证书工厂类，这个类实现了出厂合格证算法的功能
        val cf = CertificateFactory.getInstance("X509")
        // X509证书。X.509是一种很通用的证书格式
        val c = cf.generateCertificate(input) as X509Certificate
        // 获得公钥
        val publicKey = MessageDigest.getInstance("SHA1").digest(c.encoded)
        // 字节到十六进制的格式转换
        return byte2HexFormatted(publicKey)
    }

    /**
     * 将编码进行16进制转换
     *
     * @param arr
     * @return
     */
    private fun byte2HexFormatted(arr: ByteArray): String {
        val str = StringBuilder(arr.size * 2)
        for (i in arr.indices) {
            var h = Integer.toHexString(arr[i].toInt())
            val l = h.length
            if (l == 1)
                h = "0$h"
            if (l > 2)
                h = h.substring(l - 2, l)
            str.append(h.toUpperCase())
            if (i < arr.size - 1)
                str.append(':')
        }
        return str.toString()
    }

}