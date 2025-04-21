package com.example.unifocus.ui.view

import androidx.fragment.app.Fragment
import android.os.Bundle
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
import com.example.unifocus.domain.ScheduleTableDownloader
import com.example.unifocus.ui.adapter.ScheduleAdapter
import com.example.unifocus.ui.dialogues.CreateScheduleDialogue
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory
import java.util.UUID

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


        // Скачивание расписания (тест)
        var scheduleTableManager: ScheduleTableDownloader = ScheduleTableDownloader()
        view.findViewById<Button>(R.id.update_tables_button).also {
            it.setOnClickListener {
                Toast.makeText(requireContext(), "Обновление расписания...", Toast.LENGTH_SHORT).show()
                val fileUrl = "https://misis.ru/files/-/0d087c30c57f81686c70d853648d7812/gi_170325.xls"
                val fileName = "test_schedule_table.xls"

                scheduleTableManager.downloadAndReplace(requireContext(), fileUrl, fileName) { success -> activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(context, "Расписание успешно обновлено", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Не удалось обновить данные", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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