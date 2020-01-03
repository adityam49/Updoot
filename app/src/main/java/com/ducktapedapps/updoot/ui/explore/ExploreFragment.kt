package com.ducktapedapps.updoot.ui.explore

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import kotlinx.coroutines.*
import javax.inject.Inject


class ExploreFragment : Fragment(), CoroutineScope by MainScope() {

    @Inject
    lateinit var application: Application

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: SearchAdapter
    private lateinit var binding: FragmentExploreBinding
    private lateinit var viewModel: ExploreVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentExploreBinding.inflate(inflater, container, false).apply { lifecycleOwner = viewLifecycleOwner }
        setUpViewModel()
        setUpRecyclerView()
        setUpSearchView()
        return binding.root
    }

    private fun setUpRecyclerView() {
        adapter = SearchAdapter(ClickHandler())
        recyclerView = binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context).apply {
                stackFromEnd = false
                reverseLayout = true
            }
        }
        recyclerView.adapter = adapter
    }

    private fun setUpSearchView() {
        searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener, androidx.appcompat.widget.SearchView.OnQueryTextListener {
            private var oldQuery: String = ""
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newQuery: String?): Boolean {
                if (oldQuery != newQuery) {
                    oldQuery = newQuery ?: ""
                    launch {
                        delay(700)
                        if (oldQuery == newQuery && oldQuery.isNotEmpty())
                            viewModel.searchSubreddit(oldQuery)

                    }
                }
                return false
            }
        })
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this@ExploreFragment, ExploreVMFactory(application)).get(ExploreVM::class.java)
        viewModel.result.observe(viewLifecycleOwner, Observer { newResults -> adapter.submitList(newResults.toMutableList()) })
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { loading -> binding.loadingView.visibility = if (loading) View.VISIBLE else View.INVISIBLE })
    }

    inner class ClickHandler {
        fun onSubredditClick(rSubreddit: String) {
            val action = ExploreFragmentDirections.actionExploreDestinationToSubredditDestination()
            action.rSubreddit = rSubreddit
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}