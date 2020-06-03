package ubb.cscluj.financialforecasting.utils

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputLayout

class CustomTextWatcher(
    private val errorMessage: String,
    private val validateFunction: (String) -> Boolean,
    private val attachedView: TextInputLayout,
    private val endIconInvisibleInGeneral: Boolean = true
) : TextWatcher {
    override fun afterTextChanged(text: Editable?) {
        if (text != null) {
            val textString = text.toString().trim()
            if (validateFunction(textString)) {
                attachedView.isEndIconVisible = true
                attachedView.error = null
                attachedView.isErrorEnabled = false
            }
            else {
                if (textString.isNotEmpty()) {
                    attachedView.isErrorEnabled = true
                    attachedView.error = errorMessage
                } else {
                    attachedView.isHintEnabled = true
                    attachedView.error = null
                    attachedView.isErrorEnabled = false
                    if (endIconInvisibleInGeneral)
                        attachedView.isEndIconVisible = false
                }
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (attachedView.isHintEnabled)
            attachedView.isHintEnabled = false
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }
}