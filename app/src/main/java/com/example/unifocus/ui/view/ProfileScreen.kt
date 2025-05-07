package com.example.unifocus.ui.view

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
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
import java.util.UUID
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
            viewModel.deleteSchedule(it.groupName)
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.adapter = adapter

        view.findViewById<AppCompatImageButton?>(R.id.schedule_add_button).also {
            it.setOnClickListener {
                showCreateScheduleDialog()
            }
        }

        view.findViewById<AppCompatImageButton>(R.id.schedule_add_test_button).also {
            it.setOnClickListener {
                viewModel.createSchedule(UUID.randomUUID().toString(), listOf())
            }
        }

        viewModel.selectedSchedules.observe(viewLifecycleOwner, { schedules ->
            adapter.submitList(schedules)
        })


        view.findViewById<Button>(R.id.update_tables_button).also {
            it.setOnClickListener {
                it.isEnabled = false

                // скачивание и замена
                Toast.makeText(requireContext(), "Обновление расписания...", Toast.LENGTH_SHORT).show()
                val tablesToDownload = listOf(
                    Pair("schedule_gi.xls", "https://misis.ru/files/-/a939ace09ed30a192497ee99edbda4d0/gi_140425.xls"),
                    Pair("schedule_ibmi.xls", "https://misis.ru/files/-/1f39a37915a1066752bf3e2221bf6d5a/ibmi_120325.xls"),
                    Pair("schedule_eupp.xls", "https://misis.ru/files/-/a2f6b24a848d9f17b760cb941a475d4d/eupp_110425.xls"),
                    Pair("schedule_ifki.xls", "https://misis.ru/files/-/1ca0c742e7813101075aaf5138db4dc6/ifki_120325.xls"),
                    Pair("schedule_itkn.xls", "https://misis.ru/files/-/262aaeaf7b610a2c2ed0d5365596f5f6/itkn_120325.xls"),
                    Pair("schedule_inm.xls", "https://misis.ru/files/-/66e305b5c5ecab6673843363f11803e4/inm-270325.xls"),
                    Pair("schedule_ekoteh.xls", "https://misis.ru/files/-/d9001c62a2054961aa607c95f273f62a/ekoteh_120325.xls"),
                    Pair("schedule_pish-mast.xls", "https://misis.ru/files/-/8b077073a7c38f58d737451e79eb5fbd/pish-mast_120325.xls"),

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
                        val message = if (results.get() == tablesToDownload.size) {
                            "Данные успешно обновлены (${results.get()}/${tablesToDownload.size})"
                        } else {
                            "Обновлено ${results.get()} из ${tablesToDownload.size} таблиц"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()


                        // сбор данных по всем скачанным таблицам
                        val tablePathNames = tablesToDownload.map { (fileName, _) ->
                            "/storage/emulated/0/Android/data/com.example.unifocus/files/Download/$fileName"
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

                        // ТЕСТ

                        val groupFullName = "БПИ-22-РП-3_1"
                        Log.d("Schedule","parsing test for ${groupFullName}:")
                        var result = scheduleRepository.filterByFullGroupName(groupFullName)
                        result.forEach { item ->
                            Log.d("Schedule","${item.day}, ${item.weekType} | Пара ${item.lessonNum}: ${item.subject}, ${item.rooms}, ${item.teachers}")
                        }

                        println("\n" + "---------------------------------------" + "\n")

                        val teacherName = "Ринчино"
                        Log.d("Schedule","parsing test for ${teacherName}:")
                        result = scheduleRepository.filterByTeacher(teacherName)
                        result.forEach { item ->
                            Log.d("Schedule","${item.day}, ${item.weekType} | Пара ${item.lessonNum}: ${item.subject}, ${item.group} подгруппа ${item.subgroup}, ${item.rooms}")
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