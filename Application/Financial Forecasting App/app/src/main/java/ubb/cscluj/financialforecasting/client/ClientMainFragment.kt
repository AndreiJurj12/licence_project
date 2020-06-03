package ubb.cscluj.financialforecasting.client

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ubb.cscluj.financialforecasting.admin.adapter.AdminCompanyAdapter
import ubb.cscluj.financialforecasting.client.adapter.ClientCompanyAdapter
import ubb.cscluj.financialforecasting.databinding.FragmentClientMainBinding
import ubb.cscluj.financialforecasting.model.Company

class ClientMainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    ClientCompanyAdapter.OnItemClickListener{
    private var _binding: FragmentClientMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var clientCompanyAdapter: ClientCompanyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientMainBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                    startActivity(intent)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        this.setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        clientCompanyAdapter = ClientCompanyAdapter(this)
        binding.companyRecyclerView.apply {
            layoutManager = viewManager
            adapter = clientCompanyAdapter
        }
    }

    override fun onRefresh() {
        TODO("Not yet implemented")
    }

    override fun onItemClicked(company: Company) {
        TODO("Not yet implemented")
    }

    override fun onNewFavouriteCompany(newFavouriteCompany: Company) {
        TODO("Not yet implemented")
    }
}