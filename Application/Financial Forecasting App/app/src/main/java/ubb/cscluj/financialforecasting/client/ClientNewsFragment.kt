package ubb.cscluj.financialforecasting.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ubb.cscluj.financialforecasting.client.adapter.ClientNewsAdapter
import ubb.cscluj.financialforecasting.client.viewmodel.ClientNewsViewModel
import ubb.cscluj.financialforecasting.client.viewmodel.exposed_states.ClientGenericExposedTaskState
import ubb.cscluj.financialforecasting.databinding.FragmentClientNewsBinding
import ubb.cscluj.financialforecasting.model.news.News
import ubb.cscluj.financialforecasting.utils.logd
import ubb.cscluj.financialforecasting.utils.showSnackbarError
import ubb.cscluj.financialforecasting.utils.showSnackbarSuccessful

class ClientNewsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    ClientNewsAdapter.OnItemClickListener {
    private var _binding: FragmentClientNewsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var clientNewsAdapter: ClientNewsAdapter
    private lateinit var clientNewsViewModel: ClientNewsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientNewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        this.setupRecyclerView()
        this.setupViewModel()

        clientNewsViewModel.refreshFromServer()

        //setup swipe refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener(this)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        clientNewsAdapter = ClientNewsAdapter(this)
        binding.newsRecyclerView.apply {
            layoutManager = viewManager
            adapter = clientNewsAdapter
        }
    }

    private fun setupViewModel() {
        this.clientNewsViewModel =
            ViewModelProvider(this).get(ClientNewsViewModel::class.java)
        clientNewsViewModel.clientGenericExposedTaskState.observe(
            viewLifecycleOwner,
            Observer { exposedTaskState ->
                interpretChangeClientGenericExposedTaskState(exposedTaskState)
            })
    }

    private fun interpretChangeClientGenericExposedTaskState(clientGenericExposedTaskState: ClientGenericExposedTaskState) {
        if (clientGenericExposedTaskState.isFinished) {
            binding.loadingProgressBar.hide()
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            if (clientGenericExposedTaskState.errorReceived) {
                showSnackbarError(clientGenericExposedTaskState.message, binding.coordinatorLayout)
            } else {
                logd("Obtained news are: ${clientNewsViewModel.news}")
                clientNewsAdapter.updateNewsList(clientNewsViewModel.news)
                showSnackbarSuccessful(clientGenericExposedTaskState.message, binding.coordinatorLayout)
            }
        }
        else {
            binding.loadingProgressBar.show()
        }
    }

    override fun onRefresh() {
        clientNewsViewModel.refreshFromServer()
    }

    override fun onItemClicked(news: News) {
        val webpage: Uri = Uri.parse(news.url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }
}