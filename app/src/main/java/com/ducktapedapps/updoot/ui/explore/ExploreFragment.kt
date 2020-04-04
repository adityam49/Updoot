package com.ducktapedapps.updoot.ui.explore

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.*
import javax.inject.Inject


private const val TAG = "ExploreFragment"

class ExploreFragment : Fragment(), CoroutineScope by MainScope() {
    @Inject
    lateinit var application: Application

    private lateinit var viewModel: ExploreVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentExploreBinding.inflate(inflater, container, false).apply { lifecycleOwner = viewLifecycleOwner }

        val trendingAdapter = TrendingSubsAdapter()
        val searchAdapter = SearchAdapter(ClickHandler())
        setUpViewModel(binding, searchAdapter, trendingAdapter)

        binding.apply {
            trendingRv.apply {
                adapter = trendingAdapter
                layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
                itemAnimator = SlideInUpAnimator()
                PagerSnapHelper().attachToRecyclerView(this)
            }
            vm = viewModel
            val behavior = BottomSheetBehavior.from(includedQas.cardView).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }
            searchFab.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                includedQas.searchView.onActionViewExpanded()
            }
            includedQas.apply {
                searchView.apply {
                    setOnQueryTextFocusChangeListener { v, hasFocus ->
                        if (hasFocus) behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        else hideKeyboardFrom(requireContext(), this)
                    }
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean = true
                        private var oldQuery: String = ""
                        override fun onQueryTextChange(newQuery: String): Boolean {
                            if (oldQuery != newQuery) {
                                oldQuery = newQuery
                                if (oldQuery == newQuery && oldQuery.isNotEmpty() && viewModel.isLoading.value != true) {
                                    launch {
                                        delay(700)
                                        viewModel.searchSubreddit(oldQuery)
                                    }
                                    return true
                                }
                            }
                            return false
                        }
                    })
                }
                searchResultRv.apply {
                    addItemDecoration(DividerItemDecoration(requireContext(), VERTICAL))
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = searchAdapter
                }
            }
        }

        return binding.root
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setUpViewModel(binding: FragmentExploreBinding, searchAdapter: SearchAdapter, trendingAdapter: TrendingSubsAdapter) {
        viewModel = ViewModelProvider(this@ExploreFragment, ExploreVMFactory(application as UpdootApplication)).get(ExploreVM::class.java)
                .apply {
                    trendingSubs.observe(viewLifecycleOwner, Observer {
                        trendingAdapter.submitList(it)
                    })

                    result.observe(viewLifecycleOwner, Observer {
                        searchAdapter.submitList(it.toMutableList())
                    })

                    isLoading.observe(viewLifecycleOwner, Observer {
                        binding.includedQas.searchProgress.visibility = if (it) View.VISIBLE else GONE
                    })
                }

    }

    inner class ClickHandler {
        fun onSubredditClick(subreddit_name: String) {
            findNavController().navigate(
                    ExploreFragmentDirections.exploreToSubreddit().setRSubreddit(subreddit_name)
            )
        }

    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
    }
}