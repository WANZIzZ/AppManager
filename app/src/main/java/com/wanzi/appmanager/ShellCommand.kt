package com.wanzi.appmanager

import java.io.DataOutputStream

/**
 * Created by WZ on 2018-01-26.
 */
object ShellCommand {

    /**
     * 禁用
     */
    val DISABLE = "pm disable "

    /**
     * 启用
     */
    val ENABLE = "pm enable "

    /**
     * Shell命令
     *
     * @param command 命令语句
     */
    fun shellCommand(command: String): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process!!.outputStream)
            os.writeBytes(command + "\n")
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
        } catch (e: Exception) {
            throw Exception(e.message)
        } finally {
            try {
                if (os != null) {
                    os.close()
                }
                process!!.destroy()
            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
        return true
    }

}