package ubb.cscluj.financialforecasting.admin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.R

class AdminActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var navController: NavController

    lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        userToken = (application as MainApplication).userToken

        toolbar = findViewById(R.id.admin_toolbar)
        navigationView = findViewById(R.id.admin_navigation_view)
        drawerLayout = findViewById(R.id.admin_drawer_layout)

        navController = this.findNavController(R.id.admin_nav_host_fragment)
        val topLevelDestinations = setOf(
            R.id.adminMainFragment,
            R.id.adminPanelFragment,
            R.id.adminFeedbackFragment,
            R.id.adminProfileFragment
        )
        val appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        setupEmailTextField()
    }

    private fun setupEmailTextField() {
        val navigationHeader = navigationView.getHeaderView(0)
        val emailTextView = navigationHeader.findViewById<TextView>(R.id.admin_drawer_email_text)
        val userEmail = (application as MainApplication).email
        emailTextView.text = userEmail
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }

    fun logoutUser() {
        (application as MainApplication).email = ""
        (application as MainApplication).isAdmin = false
        (application as MainApplication).userToken = ""
        (application as MainApplication).userId = -1
        val actionNavigateToLogin =
            AdminProfileFragmentDirections.actionAdminProfileFragmentToLoginActivity()
        navController.navigate(actionNavigateToLogin)
        finish()
    }
}
