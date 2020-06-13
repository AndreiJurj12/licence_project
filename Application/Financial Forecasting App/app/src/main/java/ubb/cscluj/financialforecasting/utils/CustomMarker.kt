package ubb.cscluj.financialforecasting.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.network_model.DateClosePrice

@SuppressLint("ViewConstructor", "SetTextI18n")
class CustomMarker(context: Context, @LayoutRes layoutRes: Int, private val historicalDateClosePrice: List<DateClosePrice>) :
    MarkerView(context, layoutRes) {
    private var tv: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e ?: return
        tv.text = "${e.y}$ at ${historicalDateClosePrice[e.x.toInt()].date}"
        super.refreshContent(e, highlight)
    }
}