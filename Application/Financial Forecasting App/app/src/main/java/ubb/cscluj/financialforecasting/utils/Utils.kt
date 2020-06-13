package ubb.cscluj.financialforecasting.utils

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.Company
import ubb.cscluj.financialforecasting.model.FavouriteCompany


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


fun Company.toFavouriteCompany(): FavouriteCompany = FavouriteCompany(
    id, name, stockTickerSymbol, description, foundedYear, urlLink, urlLogo, csvDataPath, readyForPrediction
)

fun closeKeyboard(activity: Activity, view: View) {
    (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        .hideSoftInputFromWindow(view.windowToken, 0)
}