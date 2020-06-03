package ubb.cscluj.financialforecasting.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
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

class ClientActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var navController: NavController

    lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        userToken = (application as MainApplication).userToken

        toolbar = findViewById(R.id.client_toolbar)
        navigationView = findViewById(R.id.client_navigation_view)
        drawerLayout = findViewById(R.id.client_drawer_layout)

        navController = this.findNavController(R.id.client_nav_host_fragment)
        val topLevelDestinations = setOf(
            R.id.clientMainFragment,
            R.id.clientFavouritesFragment,
            R.id.clientFeedbackFragment,
            R.id.clientProfileFragment
        )
        val appBarConfiguration = AppBarConfiguration(topLevelDestinations, drawerLayout)
        toolbar.setupWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        setupEmailTextField()
    }

    private fun setupEmailTextField() {
        val navigationHeader = navigationView.getHeaderView(0)
        val emailTextView = navigationHeader.findViewById<TextView>(R.id.client_drawer_email_text)
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
            ClientProfileFragmentDirections.actionClientProfileFragmentToLoginActivity()
        navController.navigate(actionNavigateToLogin)
        finish()
    }
}
