package com.example.unifocus.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.MainActivity
import com.example.unifocus.R
import com.example.unifocus.UniFocusApp
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.domain.ScheduleItem
import com.example.unifocus.domain.ScheduleRepository
import com.example.unifocus.domain.ScheduleTableDownloader
import com.example.unifocus.domain.ScheduleTableParser
import com.example.unifocus.ui.adapter.ScheduleAdapter
import com.example.unifocus.ui.dialogues.CreateScheduleDialogue
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class ProfileScreen : Fragment(), CreateScheduleDialogue.OnScheduleCreatedListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private lateinit var viewModel: UniFocusViewModel
    private lateinit var downloader: ScheduleTableDownloader
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingRing: ProgressBar
    private lateinit var updateButton: Button
    private lateinit var loadingText: TextView
    private lateinit var addScheduleButton: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        val repository = (requireActivity().application as UniFocusApp).repository
        val factory = UniFocusViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniFocusViewModel::class.java]

        adapter = ScheduleAdapter(
            onDeleteClick = { schedule ->
                viewModel.selectSchedule(schedule.groupName, false)
                viewModel.selectScheduleTasks(schedule, false)
            },
            onLoadingStateChanged = { isLoading ->
                updateButton.isEnabled = !isLoading
                addScheduleButton.isEnabled = !isLoading
                (activity as? MainActivity)?.setNavigationLock(isLoading)

                if (isLoading) {
                    loadingRing.visibility = View.VISIBLE
                    loadingText.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    loadingText.text = "Обновление списка занятий..."
                } else {
                    loadingRing.visibility = View.GONE
                    loadingText.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    loadingText.text = "Загрузка..."
                }
            }
        )

        downloader = ScheduleTableDownloader()
        progressBar = view.findViewById(R.id.progressBar)
        updateButton = view.findViewById(R.id.update_tables_button)
        loadingRing = view.findViewById(R.id.loading_ring)
        loadingText = view.findViewById(R.id.loading_text)
        addScheduleButton = view.findViewById(R.id.add_schedule)

        updateButton.isEnabled = true
        addScheduleButton.isEnabled = true
        progressBar.visibility = View.GONE
        loadingRing.visibility = View.GONE
        loadingText.visibility = View.GONE

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

        addScheduleButton.setOnClickListener {
            showCreateScheduleDialog()
        }

        viewModel.selectedSchedules.observe(viewLifecycleOwner, { schedules ->
            adapter.submitList(schedules)
        })

        updateButton.setOnClickListener {
            startDownloadProcess()
        }

        return view
    }

    private fun completeProgressWithAnimation() {
        val animator = ObjectAnimator.ofInt(
            progressBar,
            "progress",
            progressBar.progress,
            progressBar.max
        ).apply {
            duration = 10000
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    progressBar.visibility = View.GONE
                    loadingRing.visibility = View.GONE
                    loadingText.visibility = View.GONE
                    updateButton.isEnabled = true
                    addScheduleButton.isEnabled = true
                }
            })
        }
        animator.start()
    }

    private fun startDownloadProcess() {
        (activity as? MainActivity)?.setNavigationLock(true)
        updateButton.isEnabled = false
        addScheduleButton.isEnabled = false
        progressBar.progress = 0
        progressBar.max = 1000
        progressBar.visibility = View.VISIBLE
        loadingRing.visibility = View.VISIBLE
        loadingText.visibility = View.VISIBLE

        Toast.makeText(requireContext(), "Обновление расписания...", Toast.LENGTH_LONG).show()
        val tablesToDownload = listOf(
            Pair("schedule_itkn.xls", "https://misis.ru/files/-/262aaeaf7b610a2c2ed0d5365596f5f6/itkn_120325.xls"),
//            Pair("schedule_gi.xls", "https://misis.ru/files/-/a939ace09ed30a192497ee99edbda4d0/gi_140425.xls"),
//            Pair("schedule_ibmi.xls", "https://misis.ru/files/-/1f39a37915a1066752bf3e2221bf6d5a/ibmi_120325.xls"),
//            Pair("schedule_eupp.xls", "https://misis.ru/files/-/a2f6b24a848d9f17b760cb941a475d4d/eupp_110425.xls"),
//            Pair("schedule_ifki.xls", "https://misis.ru/files/-/1ca0c742e7813101075aaf5138db4dc6/ifki_120325.xls"),
//            Pair("schedule_inm.xls", "https://misis.ru/files/-/66e305b5c5ecab6673843363f11803e4/inm-270325.xls"),
//            Pair("schedule_ekoteh.xls", "https://misis.ru/files/-/d9001c62a2054961aa607c95f273f62a/ekoteh_120325.xls"),
//            Pair("schedule_pish-mast.xls", "https://misis.ru/files/-/8b077073a7c38f58d737451e79eb5fbd/pish-mast_120325.xls"),

            // таблица с нестандартной структурой и форматом, могут быть ошибки!
            //Pair("schedule_ibo.xls", "https://misis.ru/files/-/dbba1aeada7152fef480fd72714b85b2/ibo_150425.xlsx")
        )
        val latch = CountDownLatch(tablesToDownload.size)
        val results = AtomicInteger(0)
        val errors = mutableListOf<String>()

        timeoutRunnable = Runnable {
            if (latch.count > 0) {
                Toast.makeText(context, "Превышено время ожидания загрузки. Проверьте подключение к интернету", Toast.LENGTH_SHORT).show()
                cancelDownloadProcess()
            }
        }
        handler.postDelayed(timeoutRunnable!!, 1 * 60 * 1000)

        tablesToDownload.forEach { (fileName, fileUrl) ->
            downloader.downloadAndReplace(
                requireContext(),
                fileUrl,
                fileName,
                timeoutMillis = 20000,
                progressCallback = { progress ->
                    activity?.runOnUiThread {
                        val currentProgress = progressBar.progress
                        val newProgress = currentProgress + (progress / tablesToDownload.size)
                        progressBar.progress = newProgress
                    }
                },
                onComplete = { success, error ->
                    if (success) {
                        results.incrementAndGet()
                    } else {
                        error?.let { errors.add("$fileName: $it") }
                    }
                    latch.countDown()
                }
            )
        }

        Thread {
            latch.await()
            handler.removeCallbacks(timeoutRunnable!!)

            activity?.runOnUiThread {
                val tablePathNames = tablesToDownload.map { (fileName, _) ->
                    "${context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}/$fileName"
                }

                val parser = ScheduleTableParser()
                val allScheduleData = mutableMapOf<String, MutableList<ScheduleItem>>()

                tablePathNames.forEach { filePath ->
                    val fileData = parser.parse(filePath)
                    fileData.forEach { (groupKey, items) ->
                        allScheduleData.getOrPut(groupKey) { mutableListOf() }.addAll(items)
                    }
                }

                val scheduleRepository = ScheduleRepository(allScheduleData)

                val message = buildString {
                    append(if (results.get() == tablesToDownload.size) {
                        "Данные успешно обновлены"
                    } else {
                        "Обновлено ${results.get()} из ${tablesToDownload.size} таблиц"
                    })

                    if (errors.isNotEmpty()) {
                        append("\nПроверьте подключение к интернету\n")
                    }
                }

                val teachers = scheduleRepository.getAllTeachers()
                val groups = scheduleRepository.getAllGroups()

                teachers.forEach { teacher ->
                    viewModel.createSchedule(teacher)
                }

                groups.forEach { group ->
                    val groupTask = scheduleRepository.filterByGroup(group)
                    groupTask.forEach {
                        scheduleRepository.parseScheduleItemToTask(it, viewModel, listOf(it.teachers.first(), group)).forEach {
                            Log.d("Created tasks", it.toString())
                        }
                    }
                    Log.d("PRINT GROUP", group)
                    viewModel.createSchedule(group)
                }

                handler.postDelayed({
                    updateButton.isEnabled = true
                    addScheduleButton.isEnabled = true
                    progressBar.visibility = View.GONE
                    loadingRing.visibility = View.GONE
                    loadingText.visibility = View.GONE
                    completeProgressWithAnimation()
                    (activity as? MainActivity)?.setNavigationLock(false)
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    Log.d("Delay", "DELAY OVER")
                }, 10000)
            }
        }.start()
    }

    private fun cancelDownloadProcess() {
        downloader.cancelAllDownloads()
        handler.removeCallbacks(timeoutRunnable!!)
        completeProgressWithAnimation()
        (activity as? MainActivity)?.setNavigationLock(false)
    }

    private fun showCreateScheduleDialog() {
        val dialog = CreateScheduleDialogue().apply {
            setOnLoadingStateChangedListener { isLoading ->
                if (isLoading) {
                    progressBar.visibility = View.INVISIBLE
                    loadingRing.visibility = View.VISIBLE
                    loadingText.visibility = View.VISIBLE
                    loadingText.text = "Добавление расписания..."

                    updateButton.isEnabled = false
                    addScheduleButton.isEnabled = false
                    (activity as? MainActivity)?.setNavigationLock(true)
                } else {
                    progressBar.visibility = View.GONE
                    loadingRing.visibility = View.GONE
                    loadingText.visibility = View.GONE

                    updateButton.isEnabled = true
                    addScheduleButton.isEnabled = true
                    (activity as? MainActivity)?.setNavigationLock(false)
                }
            }
        }
        dialog.show(parentFragmentManager, "CreateScheduleDialogue")
    }

    override fun onScheduleCreated(task: Task) {
        viewModel.addTask(task)
    }

    override fun onDestroyView() {
        if (::downloader.isInitialized) {
            downloader.cancelAllDownloads()
        }
        timeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        timeoutRunnable = null
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}