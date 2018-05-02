package com.wanzi.appmanager

import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView
import com.wanzi.appmanager.databinding.DialogListBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    val data = ArrayList<AppEntry>()        // 适配器中的数据
    private var adapter: GeneralAdapter<AppEntry>

    private val sourceData = ArrayList<AppEntry>()  // 源数据（所有的App）

    private lateinit var binding: DialogListBinding
    private lateinit var dialog: BottomSheetDialog

    init {
        adapter = GeneralAdapter(R.layout.item_app, data, BR.item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 申请root权限
        ShellCommand.shellCommand("chmod 777 $packageCodePath")

        initView()

        initListener()
    }

    private fun initView() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_list, null)
        binding = DataBindingUtil.bind(view)!!
        binding.recyclerView.adapter = adapter

        dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        // 默认展开高度为整个屏幕
        behavior.peekHeight = displayMetrics.heightPixels
    }

    private fun initListener() {
        tv_system_app.setOnClickListener {
            loadData(AppType.SYSTEM_APP)
        }
        tv_other_app.setOnClickListener {
            loadData(AppType.OTHER_APP)
        }

        adapter.setOnItemClickListener { adapter, _, position ->

            Observable
                    .create<AppEntry> {
                        val entry = adapter.data[position] as AppEntry
                        if (entry.isDisable) {
                            ShellCommand.shellCommand("${ShellCommand.ENABLE} ${entry.packageName}")
                            entry.isDisable = false
                        } else {
                            ShellCommand.shellCommand("${ShellCommand.DISABLE} ${entry.packageName}")
                            entry.isDisable = true
                        }
                        it.onNext(entry)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        adapter.notifyItemChanged(position)
                    }
        }

        RxSearchView
                .queryTextChanges(binding.search)
                .map {
                    return@map it.toString()
                }
                .subscribe {
                    data.clear()
                    for (item in sourceData) {
                        if (item.name.contains(it)) {
                            data.add(item)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
    }

    private fun loadData(type: AppType) {
        binding.search.setQuery("", false)
        binding.recyclerView.visibility = View.INVISIBLE
        binding.loadingProgress.visibility = View.VISIBLE
        dialog.show()

        Observable
                .create<List<AppEntry>> {
                    it.onNext(AppUtils.getApps(this, type))
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    data.clear()
                    data.addAll(it)
                    data.sort() // 排序
                    adapter.notifyDataSetChanged()

                    binding.recyclerView.visibility = View.VISIBLE
                    binding.loadingProgress.visibility = View.GONE

                    sourceData.clear()
                    sourceData.addAll(data)
                }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dialog.isShowing) dialog.dismiss()
    }
}
