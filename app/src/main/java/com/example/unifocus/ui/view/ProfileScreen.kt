package com.example.unifocus.ui.view

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.profile, container, false)

        val repository = (requireActivity().application as UniFocusApp).repository
        val factory = UniFocusViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[UniFocusViewModel::class.java]
        adapter = ScheduleAdapter {
            viewModel.selectSchedule(it.groupName, false)
            viewModel.selectScheduleTasks(it, false)
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

        view.findViewById<AppCompatImageButton?>(R.id.add_schedule).also {
            it.setOnClickListener {
                showCreateScheduleDialog()
            }
        }

        viewModel.selectedSchedules.observe(viewLifecycleOwner, { schedules ->
            adapter.submitList(schedules)
        })


        view.findViewById<Button>(R.id.update_tables_button).also {
            it.setOnClickListener {
                it.isEnabled = false

                // скачивание и замена
                Toast.makeText(requireContext(), "Обновление расписания...", Toast.LENGTH_LONG).show()
                val tablesToDownload = listOf(
                    //Pair("schedule_gi.xls", "https://misis.ru/files/-/a939ace09ed30a192497ee99edbda4d0/gi_140425.xls"),
                    //Pair("schedule_ibmi.xls", "https://misis.ru/files/-/1f39a37915a1066752bf3e2221bf6d5a/ibmi_120325.xls"),
                    //Pair("schedule_eupp.xls", "https://misis.ru/files/-/a2f6b24a848d9f17b760cb941a475d4d/eupp_110425.xls"),
                    //Pair("schedule_ifki.xls", "https://misis.ru/files/-/1ca0c742e7813101075aaf5138db4dc6/ifki_120325.xls"),
                    Pair("schedule_itkn.xls", "https://misis.ru/files/-/262aaeaf7b610a2c2ed0d5365596f5f6/itkn_120325.xls"),
                    //Pair("schedule_inm.xls", "https://misis.ru/files/-/66e305b5c5ecab6673843363f11803e4/inm-270325.xls"),
                    //Pair("schedule_ekoteh.xls", "https://misis.ru/files/-/d9001c62a2054961aa607c95f273f62a/ekoteh_120325.xls"),
                    //Pair("schedule_pish-mast.xls", "https://misis.ru/files/-/8b077073a7c38f58d737451e79eb5fbd/pish-mast_120325.xls"),

                    // таблица с нестандартной структурой и форматом, могут быть ошибки!
                    //Pair("schedule_ibo.xls", "https://misis.ru/files/-/dbba1aeada7152fef480fd72714b85b2/ibo_150425.xlsx")
                )

                val latch = CountDownLatch(tablesToDownload.size)
                val results = AtomicInteger(0)
                val scheduleTableDownloader = ScheduleTableDownloader()

                tablesToDownload.forEach { (fileName, fileUrl) ->
                    scheduleTableDownloader.downloadAndReplace(requireContext(), fileUrl, fileName) { success ->
                        if (success) {
                            results.incrementAndGet()
                        }
                        latch.countDown()
                    }
                }

                Thread {
                    latch.await()
                    activity?.runOnUiThread {
                        // сбор данных по всем скачанным таблицам
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

                        val message = if (results.get() == tablesToDownload.size) {
                            "Данные успешно обновлены (${results.get()}/${tablesToDownload.size})"
                        } else {
                            "Обновлено ${results.get()} из ${tablesToDownload.size} таблиц"
                        }

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                        val teachers = scheduleRepository.getAllTeachers()

                        val repository = (requireActivity().application as UniFocusApp).repository
                        val factory = UniFocusViewModelFactory(repository)
                        viewModel = ViewModelProvider(this, factory)[UniFocusViewModel::class.java]

                        teachers.forEach { teacher ->
//                            val teacherTasks = scheduleRepository.filterByTeacher(teacher)
//                            teacherTasks.forEach {
//                                scheduleRepository.parseScheduleItemToTask(it, viewModel, listOf(teacher, it.group)).forEach {
//                                    Log.d("Created tasks", it.toString())
//                                }
//                            }

                            viewModel.createSchedule(teacher)
                        }

                        val groups = scheduleRepository.getAllGroups()
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

                        it.isEnabled = true
                    }
                }.start()
            }
        }

        return view
    }

   private fun showCreateScheduleDialog() {
       val dialog = CreateScheduleDialogue.newInstance()
       dialog.setOnScheduleCreatedListener(this)
       dialog.show(parentFragmentManager, "CreateScheduleDialog")
   }

   override fun onScheduleCreated(task: Task) {
       viewModel.addTask(task)
   }
}