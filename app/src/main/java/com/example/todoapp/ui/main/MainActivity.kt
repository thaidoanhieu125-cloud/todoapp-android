package com.example.todoapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.todoapp.R
import com.example.todoapp.ui.auth.LoginActivity
import com.example.todoapp.ui.category.CategoryFragment
import com.example.todoapp.ui.profile.ProfileFragment
import com.example.todoapp.ui.statistics.StatisticsFragment
import com.example.todoapp.ui.task.TaskListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.todoapp.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabAddTask: FloatingActionButton
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        session = SessionManager(this)
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        bottomNav = findViewById(R.id.bottomNav)
        fabAddTask = findViewById(R.id.fabAddTask)

        val current = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (current == null) loadFragment(TaskListFragment())
        fabAddTask.isVisible = current == null || current is TaskListFragment

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_tasks -> TaskListFragment()
                R.id.nav_categories -> CategoryFragment()
                R.id.nav_statistics -> StatisticsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            loadFragment(fragment)
            fabAddTask.isVisible = item.itemId == R.id.nav_tasks
            true
        }

        fabAddTask.setOnClickListener {
            val intent = Intent(this, com.example.todoapp.ui.task.AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}
