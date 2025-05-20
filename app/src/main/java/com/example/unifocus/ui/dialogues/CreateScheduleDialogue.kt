package com.example.unifocus.ui.dialogues

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.UniFocusApp
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.ui.adapter.ScheduleAdapter
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory

class CreateScheduleDialogue : DialogFragment() {
    private var listener: OnScheduleCreatedListener? = null
    private lateinit var adapter: ScheduleAdapter
    private lateinit var viewModel: UniFocusViewModel
    private var onLoadingStateChanged: ((Boolean) -> Unit)? = null

    interface OnScheduleCreatedListener {
        fun onScheduleCreated(task: Task)
    }

    fun setOnScheduleCreatedListener(listener: OnScheduleCreatedListener) {
        this.listener = listener
    }

    fun setOnLoadingStateChangedListener(listener: (Boolean) -> Unit) {
        this.onLoadingStateChanged = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.schedule_list_dialogue, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = (requireActivity().application as UniFocusApp).repository
        val factory = UniFocusViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[UniFocusViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSchedules)
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        adapter = ScheduleAdapter(
            onDeleteClick = { schedule ->
                onLoadingStateChanged?.invoke(true)
                Toast.makeText(
                    requireContext(),
                    "Добавляем расписание...",
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.selectSchedule(schedule.groupName, true)
                viewModel.selectScheduleTasks(schedule, true)

                view.post {
                    dialog?.hide()

                    requireActivity().window.decorView.postDelayed({
                        if (isAdded) {
                            onLoadingStateChanged?.invoke(false)
                            Toast.makeText(
                                requireContext(),
                                "Расписание успешно добавлено",
                                Toast.LENGTH_SHORT
                            ).show()
                            dismiss()
                        }
                    }, 5000)
                }
            },
            onLoadingStateChanged = {}
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.schedules.observe(viewLifecycleOwner) { list ->
            adapter.submitData(list)
            if (list.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Обновите таблицы расписания",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun newInstance() = CreateScheduleDialogue()
    }
}