package com.example.unifocus.ui.view

import androidx.lifecycle.ViewModelProvider
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.unifocus.R
import com.example.unifocus.data.database.Converters
import com.example.unifocus.data.database.UniFocusDatabase
import com.example.unifocus.data.models.calendar_day.CalendarDay
import com.example.unifocus.data.models.task.Task
import com.example.unifocus.data.repository.UniFocusRepository
import com.example.unifocus.ui.adapter.CalendarAdapter
import com.example.unifocus.ui.adapter.TaskAdapter
import com.example.unifocus.ui.decorators.VerticalSpaceItemDecoration
import com.example.unifocus.ui.dialogues.EditTaskDialogFragment
import com.example.unifocus.ui.viewmodels.UniFocusViewModel
import com.example.unifocus.ui.viewmodels.UniFocusViewModelFactory
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class ScheduleScreen : Fragment() {
    private lateinit var calendarGrid: RecyclerView
    private lateinit var monthYearHeader: TextView
    private lateinit var todayHeader: TextView
    private lateinit var calendarContainer: LinearLayout
    private var currentCalendar: Calendar = Calendar.getInstance()
    private var isAnimating = false
    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var toggleCalendarButton: ImageButton
    private var isCalendarExpanded = true
    private lateinit var repository: UniFocusRepository
    private val days = mutableListOf<CalendarDay>()
    private lateinit var tasksList: RecyclerView
    private lateinit var viewModel: UniFocusViewModel

    private val gestureDetector by lazy {
        GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            @Suppress("NOTHING_TO_OVERRIDE", "ACCIDENTAL_OVERRIDE")
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                if (abs(velocityX) > 1000) {
                    if (e2.x - e1.x > 0) showPreviousMonth() else showNextMonth()
                    return true
                }
                return false
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.schedule, container, false)
        calendarGrid = view.findViewById(R.id.calendar_grid)
        monthYearHeader = view.findViewById(R.id.month_year_header)
        todayHeader = view.findViewById(R.id.today_header)
        tasksList = view.findViewById(R.id.tasks_list)
        calendarContainer = view.findViewById(R.id.calendar_container)
        toggleCalendarButton = view.findViewById(R.id.toggle_calendar)
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = UniFocusRepository(UniFocusDatabase.getDatabase(requireContext()))

        viewModel = ViewModelProvider(
            this,
            UniFocusViewModelFactory(repository)
        )[UniFocusViewModel::class.java]

        tasksList.layoutManager = LinearLayoutManager(requireContext())
        tasksList.adapter = TaskAdapter(
            onDeleteClick = {
                    task -> viewModel.deleteTaskById(task.id)
            },
            onTaskClick = {
                    task -> showEditTaskDialog(task)
            }
        )

        tasksList.addItemDecoration(VerticalSpaceItemDecoration(16))

        selectedDate = Calendar.getInstance()
        updateDateHeader()
        loadTasksForSelectedDate()

        updateMonthYearHeader()
        setupCalendar()

        calendarGrid.adapter = CalendarAdapter(days) { clickedDay ->
            selectedDate = clickedDay.date
            updateDateHeader()
            loadTasksForSelectedDate()
        }

        calendarGrid.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        toggleCalendarButton.setOnClickListener {
            toggleCalendarVisibility()
        }
    }

    private fun toggleCalendarVisibility() {
        isCalendarExpanded = !isCalendarExpanded

        calendarContainer.visibility = if (isCalendarExpanded) View.VISIBLE else View.GONE
        toggleCalendarButton.setImageResource(
            if (isCalendarExpanded) R.drawable.ic_arrow_up
            else R.drawable.ic_arrow_down
        )

        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        TransitionManager.beginDelayedTransition(calendarContainer as ViewGroup, transition)
    }

    private fun showPreviousMonth() {
        if (isAnimating) return
        startAnimation(R.anim.slide_in_left, R.anim.slide_out_right) {
            currentCalendar.add(Calendar.MONTH, -1)
            updateMonthYearHeader()
            resetSelectedDate()
            setupCalendar()
        }
    }

    private fun showNextMonth() {
        if (isAnimating) return
        startAnimation(R.anim.slide_in_right, R.anim.slide_out_left) {
            currentCalendar.add(Calendar.MONTH, 1)
            updateMonthYearHeader()
            resetSelectedDate()
            setupCalendar()
        }
    }

    private fun resetSelectedDate() {
        selectedDate = Calendar.getInstance()
        updateDateHeader()
        loadTasksForSelectedDate()
        calendarGrid.adapter?.notifyDataSetChanged()
    }

    private fun startAnimation(
        enterAnim: Int,
        exitAnim: Int,
        onEnd: () -> Unit
    ) {
        isAnimating = true
        val enter = AnimationUtils.loadAnimation(requireContext(), enterAnim)
        val exit = AnimationUtils.loadAnimation(requireContext(), exitAnim)

        exit.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                calendarGrid.startAnimation(enter)
                onEnd()
                isAnimating = false
            }
        })

        calendarGrid.startAnimation(exit)
    }

    private fun updateMonthYearHeader() {
        val monthNames = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        monthYearHeader.text = "${monthNames[currentCalendar.get(Calendar.MONTH)]} ${currentCalendar.get(Calendar.YEAR)}"
    }

    private fun setupCalendar() {
        val tempCalendar = currentCalendar.clone() as Calendar
        val today = Calendar.getInstance()

        days.clear()

        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)

        tempCalendar.add(Calendar.MONTH, -1)
        val daysInPrevMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysBefore = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        for (i in daysInPrevMonth - daysBefore + 1..daysInPrevMonth) {
            val date = tempCalendar.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, i)
            days.add(CalendarDay(i, false, false, date))
        }

        tempCalendar.add(Calendar.MONTH, 1)
        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val date = tempCalendar.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, i)
            days.add(CalendarDay(i,
                date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR),
                true,
                date))
        }

        val totalCells = 42
        val daysToAdd = totalCells - days.size
        tempCalendar.add(Calendar.MONTH, 1)
        for (i in 1..daysToAdd) {
            val date = tempCalendar.clone() as Calendar
            date.set(Calendar.DAY_OF_MONTH, i)
            days.add(CalendarDay(i, false, false, date))
        }

        calendarGrid.layoutManager = GridLayoutManager(requireContext(), 7)
        (calendarGrid.adapter as? CalendarAdapter)?.updateDays(days) ?: run {
            calendarGrid.adapter = CalendarAdapter(days) { clickedDay ->
                selectedDate = clickedDay.date
                updateDateHeader()
                loadTasksForSelectedDate()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.selectedTasks.collect {
                days.forEach { day ->
                    val hasTasks = it.any { task ->
                        task.deadline?.toLocalDate() == day.date.toLocalDateTime().toLocalDate()
                    }
                    day.hasTasks = hasTasks
                }
                calendarGrid.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun updateDateHeader() {
        val pattern = if (isToday(selectedDate)) "Сегодня: d MMMM yyyy"
        else "Выбранная дата: d MMMM yyyy"

        todayHeader.text = SimpleDateFormat(pattern, Locale("ru")).format(selectedDate.time)
    }

    private fun loadTasksForSelectedDate() {
        viewLifecycleOwner.lifecycleScope.launch {
            val start = selectedDate.toLocalDateTime().withHour(0)
            val end = selectedDate.toLocalDateTime().withHour(23).withMinute(59)

            repository.getTodaySelectedTasks(
                Converters().dateToTimestamp(start)!!,
                Converters().dateToTimestamp(end)!!
            ).collect { tasks ->
                (tasksList.adapter as? TaskAdapter)?.submitList(tasks)
            }
        }
    }

    private fun showEditTaskDialog(task: Task) {
        val dialog = EditTaskDialogFragment.newInstance(task)
        dialog.setOnTaskUpdatedListener(object : EditTaskDialogFragment.OnTaskUpdatedListener {

            override fun onTaskUpdated(task: Task) {
                viewModel.updateTask(task)

                rescheduleTaskNotification(task)
            }
        })
        dialog.show(parentFragmentManager, "EditTaskDialog")
    }

    fun rescheduleTaskNotification(task: Task) {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            val channelID = "Unifocus"
            val title = "Уведомление о задаче:"
            val text = "${task.name}\nДедлайн: ${
                task.deadline?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault()))
                    ?: "не указан"
            }"

            viewModel.rescheduleNotification(context, task.notificationTime, task.id, channelID, title, text)
        }
    }

    private fun isToday(calendar: Calendar): Boolean {
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun Calendar.toLocalDateTime() = LocalDateTime.ofInstant(
        this.time.toInstant(),
        ZoneId.systemDefault()
    )
}