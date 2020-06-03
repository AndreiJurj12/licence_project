package ubb.cscluj.financialforecasting.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.Company

class ClientCompanyAdapter internal constructor(
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ClientCompanyAdapter.ClientCompanyViewHolder>() {

    private var companyList: List<Company> = emptyList()
    private var favouriteCompanyList: List<Company> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientCompanyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_company_item_client,
            parent,
            false
        )

        return ClientCompanyViewHolder(itemView)
    }

    override fun getItemCount(): Int = companyList.size


    override fun onBindViewHolder(holder: ClientCompanyViewHolder, position: Int) {
        val currentCompany = companyList[position]
        holder.bindCompanyToClick(currentCompany, itemClickListener)
    }

    fun updateCompanyList(companyList: List<Company>) {
        this.companyList = companyList
        notifyDataSetChanged()
    }

    fun updateFavouriteCompanyList(favouriteCompanyList: List<Company>) {
        this.favouriteCompanyList = favouriteCompanyList
        notifyDataSetChanged()
    }

    inner class ClientCompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyNameTextView: MaterialTextView =
            itemView.findViewById(R.id.company_name_text_view)
        val companySymbolTextView: MaterialTextView =
            itemView.findViewById(R.id.company_symbol_text_view)
        val companyIsFavouriteImageView: ImageView =
            itemView.findViewById(R.id.company_is_favourite_image_view)
        val companyReadyForPredictionImageView: ImageView =
            itemView.findViewById(R.id.company_ready_for_prediction_image_view)


        fun bindCompanyToClick(company: Company, itemClickListener: OnItemClickListener) {
            companyNameTextView.text = company.name
            companySymbolTextView.text = company.stockTickerSymbol
            if (company.readyForPrediction) {
                companyReadyForPredictionImageView.setImageResource(R.drawable.ic_done)
            } else {
                companyReadyForPredictionImageView.setImageResource(R.drawable.ic_announcement)
            }

            if (favouriteCompanyList.contains(company)) {
                companyIsFavouriteImageView.setImageResource(R.drawable.ic_favorite)
            } else {
                companyIsFavouriteImageView.setImageResource(R.drawable.ic_favorite_border)
            }

            companyIsFavouriteImageView.setOnClickListener {
                itemClickListener.onNewFavouriteCompany(company)
            }
            itemView.setOnClickListener {
                itemClickListener.onItemClicked(company)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(company: Company)
        fun onNewFavouriteCompany(newFavouriteCompany: Company)
    }
}