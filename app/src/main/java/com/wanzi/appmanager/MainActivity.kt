package com.wanzi.appmanager

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.view.LayoutInflater
import com.wanzi.appmanager.databinding.DialogListBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val data = ArrayList<AppEntry>()
    private var adapter: GeneralAdapter<AppEntry>

    init {
        adapter = GeneralAdapter(R.layout.item_app, data, BR.item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 申请root权限
        ShellCommand.shellCommand("chmod 777 $packageCodePath")

        tv_system_app.setOnClickListener {
            loadData(AppUtils.getApps(this, AppType.SYSTEM_APP))
        }
        tv_other_app.setOnClickListener {
            loadData(AppUtils.getApps(this, AppType.OTHER_APP))
        }

        adapter.setOnItemClickListener { adapter, view, position ->
            val entry = adapter.data[position] as AppEntry
            if (entry.isDisable) {
                ShellCommand.shellCommand("${ShellCommand.ENABLE} ${entry.packageName}")
                entry.isDisable = false
            } else {
                ShellCommand.shellCommand("${ShellCommand.DISABLE} ${entry.packageName}")
                entry.isDisable = true
            }
            adapter.notifyItemChanged(position)
        }
    }

    private fun loadData(list: List<AppEntry>) {
        data.clear()
        data.addAll(list)
        data.sort() // 排序
        adapter.notifyDataSetChanged()

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_list, null)
        val binding = DataBindingUtil.bind<DialogListBinding>(view)
        binding!!.recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.show()
    }
}
