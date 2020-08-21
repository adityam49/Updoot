package com.ducktapedapps.updoot.ui.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import com.ducktapedapps.updoot.ui.explore.search.SearchAdapter
import com.ducktapedapps.updoot.ui.explore.trending.ExploreTrendingAdapter
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class ExploreFragment : Fragment() {
    @Inject
    lateinit var vmFactory: ExploreVMFactory
    private val viewModel by lazy { ViewModelProvider(this@ExploreFragment, vmFactory).get(ExploreVM::class.java) }
    private var _binding: FragmentExploreBinding? = null
    private val binding: FragmentExploreBinding
        get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as UpdootApplication).updootComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val exploreMergeAdapter = ConcatAdapter()
        val trendingAdapter = ExploreTrendingAdapter()
        val searchAdapter = SearchAdapter(object : SearchAdapter.ResultAction {
            override fun goToSubreddit(subredditName: String) {
                binding.searchView.clearFocus()
                openSubreddit(subredditName)
            }
        })
        setUpViewModels(trendingAdapter, searchAdapter)
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = exploreMergeAdapter.apply {
                    addAdapter(trendingAdapter)
                    addAdapter(searchAdapter)
                }
            }
            searchView.apply {
                isFocusable = true
                isIconified = false
                setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    private var oldQuery: String = ""
                    override fun onQueryTextSubmit(query: String?) = false
                    override fun onQueryTextChange(newQuery: String?): Boolean {
                        if (oldQuery != newQuery) {
                            oldQuery = newQuery ?: ""
                            lifecycleScope.launch {
                                delay(400)
                                if (oldQuery == newQuery) viewModel.searchSubreddit(oldQuery)
                            }
                        }
                        return false
                    }
                })
            }
        }
    }

    private fun openSubreddit(subredditName: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, SubredditFragment.newInstance(subredditName)).commit()
    }

    private fun setUpViewModels(trendingAdapter: ExploreTrendingAdapter, searchAdapter: SearchAdapter) =
            viewModel.apply {
                trendingSubs.asLiveData().observe(viewLifecycleOwner) { trendingAdapter.submitList(it) }
                searchResults.asLiveData().observe(viewLifecycleOwner) { searchAdapter.submitList(it) }
                isLoading.asLiveData().observe(viewLifecycleOwner) { binding.progressCircular.visibility = if (it) VISIBLE else GONE }
            }
}