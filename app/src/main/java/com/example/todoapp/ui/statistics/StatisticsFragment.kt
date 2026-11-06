package com.example.todoapp.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.todoapp.R
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var tvTotal: TextView
    private lateinit var tvCompleted: TextView
    private lateinit var tvPending: TextView
    private lateinit var tvRate: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var pieChartView: StatsPieView

    private lateinit var taskRepo: TaskRepository
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskRepo = TaskRepository(requireContext())
        session = SessionManager(requireContext())

        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        tvTotal = view.findViewById(R.id.tvTotal)
        tvCompleted = view.findViewById(R.id.tvCompleted)
        tvPending = view.findViewById(R.id.tvPending)
        tvRate = view.findViewById(R.id.tvRate)
        progressBar = view.findViewById(R.id.progressBar)
        pieChartView = view.findViewById(R.id.pieChartView)

        val filters = arrayOf("Tất cả", "Hôm nay", "Tuần này", "Tháng này")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filters)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadStats(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        loadStats(0)
    }

    override fun onResume() { super.onResume(); loadStats(spinnerFilter.selectedItemPosition) }

    private fun loadStats(filterIndex: Int) {
        val userId = session.getUserId()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val cal = Calendar.getInstance()

        val (fromDate, toDate) = when (filterIndex) {
            1 -> Pair(today, today)
            2 -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val from = sdf.format(cal.time)
                cal.add(Calendar.DAY_OF_WEEK, 6)
                Pair(from, sdf.format(cal.time))
            }
            3 -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val from = sdf.format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                Pair(from, sdf.format(cal.time))
            }
            else -> Pair(null, null)
        }

        val stats = taskRepo.getStatsByUser(userId, fromDate, toDate)
        tvTotal.text = "Tổng: ${stats.total}"
        tvCompleted.text = "Hoàn thành: ${stats.completed}"
        tvPending.text = "Chưa hoàn thành: ${stats.pending}"
        tvRate.text = "Tỉ lệ hoàn thành: ${"%.1f".format(stats.completionRate)}%"
        progressBar.progress = stats.completionRate.toInt()

        pieChartView.setData(stats.completed, stats.pending)
    }
}
