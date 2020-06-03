package ubb.cscluj.financialforecasting.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ActivityNavigator
import ubb.cscluj.financialforecasting.MainApplication
import ubb.cscluj.financialforecasting.databinding.ActivityLoginBinding
import ubb.cscluj.financialforecasting.repository.login.LoginRepository

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    lateinit var loginRepository: LoginRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        loginRepository =
            LoginRepository((application as MainApplication).networkService)
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }
}

