package ubb.cscluj.financialforecasting.utils

import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ubb.cscluj.financialforecasting.R


fun Any.logd(message: Any? = "no message!", cause: Throwable? = null) {
    Log.d(this.javaClass.simpleName, message.toString(), cause)
}


fun Any.loge(message: Any? = "no message!", cause: Throwable? = null) {
    Log.e(this.javaClass.simpleName, message.toString(), cause)
}

fun showSnackbarError(message: Any? = "no message!", view: View) {
    val snackbar = Snackbar.make(view, message.toString(), Snackbar.LENGTH_LONG)
    val snackbarView = snackbar.view
    snackbarView.apply {
        setBackgroundColor(ContextCompat.getColor(context,
            R.color.color_on_background
        ))
    }
    snackbarView.findViewById<TextView>(R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(snackbarView.context,
            R.color.color_accent
        ))
    snackbar.show()
}

fun showSnackbarSuccessful(message: Any? = "no message!", view: View) {
    val snackbar = Snackbar.make(view, message.toString(), Snackbar.LENGTH_SHORT)
    val snackbarView = snackbar.view
    snackbarView.apply {
        setBackgroundColor(ContextCompat.getColor(context,
            R.color.color_on_background
        ))
    }
    snackbarView.findViewById<TextView>(R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(snackbarView.context,
            R.color.financialforecasting_green_500
        ))
    snackbar.show()
}

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)