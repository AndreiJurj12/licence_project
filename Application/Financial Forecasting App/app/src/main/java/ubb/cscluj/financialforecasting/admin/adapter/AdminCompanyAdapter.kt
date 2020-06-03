package ubb.cscluj.financialforecasting.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ubb.cscluj.financialforecasting.R
import ubb.cscluj.financialforecasting.model.Company

class AdminCompanyAdapter internal constructor(
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<AdminCompanyAdapter.AdminCompanyViewHolder>() {

    private var companyList: List<Company> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCompanyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.recyclerview_company_item_admin,
            parent,
            false)

        return AdminCompanyViewHolder(itemView)
    }

    override fun getItemCount(): Int = companyList.size


    override fun onBindViewHolder(holder: AdminCompanyViewHolder, position: Int) {
        val currentCompany = companyList[position]
        holder.bindCompanyToClick(currentCompany, itemClickListener)
    }

    fun updateList(companyList: List<Company>) {
        this.companyList = companyList
        notifyDataSetChanged()
    }

    inner class AdminCompanyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyNameTextView: MaterialTextView =
            itemView.findViewById(R.id.company_name_text_view)
        val companySymbolTextView: MaterialTextView =
            itemView.findViewById(R.id.company_symbol_text_view)
        val companyReadyForPredictionImageView: ImageView =
            itemView.findViewById(R.id.company_ready_for_prediction_image_view)


        fun bindCompanyToClick(company: Company, itemClickListener: OnItemClickListener) {
            companyNameTextView.text = company.name
            companySymbolTextView.text = company.stockTickerSymbol
            if (company.readyForPrediction) {
                companyReadyForPredictionImageView.setImageResource(R.drawable.ic_done)
            }
            else {
                companyReadyForPredictionImageView.setImageResource(R.drawable.ic_announcement)
            }

            itemView.setOnClickListener {
                itemClickListener.onItemClicked(company)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(company: Company)
    }
}