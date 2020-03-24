package com.ducktapedapps.updoot.ui.explore

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import com.ducktapedapps.updoot.utils.CustomItemAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Inject


private const val TAG = "ExploreFragment"

class ExploreFragment : Fragment(), CoroutineScope by MainScope() {
    @Inject
    lateinit var application: Application

    private lateinit var trendingAdapter: TrendingSubsAdapter
    private lateinit var viewModel: ExploreVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentExploreBinding.inflate(inflater, container, false).apply { lifecycleOwner = viewLifecycleOwner }

        setUpViewModel()
        trendingAdapter = TrendingSubsAdapter()
        binding.apply {
            trendingRv.apply {
                adapter = trendingAdapter
                layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
                itemAnimator = CustomItemAnimator()
                PagerSnapHelper().attachToRecyclerView(this)
            }
            vm = viewModel
        }
        return binding.root
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(this@ExploreFragment, ExploreVMFactory(application as UpdootApplication)).get(ExploreVM::class.java)
        viewModel.trendingSubs.observe(viewLifecycleOwner, Observer {
            trendingAdapter.submitList(it)
        })
    }

    inner class ClickHandler {
        fun onSubredditClick(rSubreddit: String) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}