package ubb.cscluj.financialforecasting.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.textview.MaterialTextView
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.news.News

class ClientNewsAdapter internal constructor(
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ClientNewsAdapter.ClientNewsViewHolder>() {

    private var newsList: List<News> = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientNewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_news_item_client,
            parent,
            false
        )

        return ClientNewsViewHolder(itemView)
    }

    override fun getItemCount(): Int = newsList.size


    override fun onBindViewHolder(holder: ClientNewsViewHolder, position: Int) {
        val currentNews = newsList[position]
        holder.bindNewsToClick(currentNews, itemClickListener)
    }

    fun updateNewsList(newsList: List<News>) {
        this.newsList = newsList
        notifyDataSetChanged()
    }

    inner class ClientNewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitleTextView: MaterialTextView =
            itemView.findViewById(R.id.title_text_view)
        val newsAuthorTextView: MaterialTextView =
            itemView.findViewById(R.id.author_text_view)
        val newsImageView: ImageView =
            itemView.findViewById(R.id.news_image_view)
        val newsDescriptionTextView: MaterialTextView =
            itemView.findViewById(R.id.description_text_view)


        fun bindNewsToClick(news: News, itemClickListener: OnItemClickListener) {
            newsTitleTextView.text = news.title ?: "Unknown title"
            newsAuthorTextView.text = news.author ?: "Unknown author"
            if (news.description != null && news.description!!.isNotBlank()) {
                newsDescriptionTextView.text = news.description
            } else {
                newsDescriptionTextView.text = news.content ?: "No description"
            }

            Glide.with(itemView)
                .load(news.urlToImage)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .centerCrop()
                .transition(withCrossFade())
                .into(newsImageView)

            itemView.setOnClickListener {
                itemClickListener.onItemClicked(news)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(news: News)
    }
}