package ubb.cscluj.financialforecasting.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.FeedbackMessage
import java.text.SimpleDateFormat
import java.util.*

class AdminFeedbackMessageAdapter internal constructor(
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdminFeedbackMessageAdapter.FeedbackMessageViewHolder>() {

    private var feedbackMessageList: List<FeedbackMessage> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackMessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_feedback_item,
            parent,
            false)

        return FeedbackMessageViewHolder(itemView)
    }

    override fun getItemCount(): Int = feedbackMessageList.size


    override fun onBindViewHolder(holder: FeedbackMessageViewHolder, position: Int) {
        val currentFeedbackMessage = feedbackMessageList[position]
        holder.bindFeedbackMessageToClick(currentFeedbackMessage, itemClickListener)
    }

    fun updateList(feedbackMessageList: List<FeedbackMessage>) {
        this.feedbackMessageList = feedbackMessageList
        notifyDataSetChanged()
    }

    inner class FeedbackMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val creationDateTextView: MaterialTextView =
            itemView.findViewById(R.id.message_creation_date_text_view)
        val messageRequestTextView: MaterialTextView =
            itemView.findViewById(R.id.message_request_text_view)
        val imageSolvedHintImageView: ImageView =
            itemView.findViewById(R.id.image_solved_hint_image_view)


        private val dateFormatString: String = "dd-MM-yyyy HH:mm:ss"
        private var dateFormat: SimpleDateFormat = SimpleDateFormat(dateFormatString, Locale.getDefault())


        fun bindFeedbackMessageToClick(feedbackMessage: FeedbackMessage, itemClickListener: OnItemClickListener) {
            creationDateTextView.text = dateFormat.format(feedbackMessage.creationTime)
            messageRequestTextView.text = feedbackMessage.messageRequest
            if (feedbackMessage.messageResponse.isBlank()) {
                imageSolvedHintImageView.setImageResource(R.drawable.ic_announcement)
            }
            else {
                imageSolvedHintImageView.setImageResource(R.drawable.ic_done)
            }

            itemView.setOnClickListener {
                itemClickListener.onItemClicked(feedbackMessage)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(feedbackMessage: FeedbackMessage)
    }
}