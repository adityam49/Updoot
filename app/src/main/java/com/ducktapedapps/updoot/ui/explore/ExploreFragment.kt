package com.ducktapedapps.updoot.ui.explore

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import com.ducktapedapps.updoot.ui.explore.trending.ExploreTrendingAdapter
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
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
        val trendingAdapter = ExploreTrendingAdapter()
        setUpViewModels(trendingAdapter)
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = trendingAdapter
            }
        }
    }

    private fun openSubreddit(subredditName: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.fragment_container, SubredditFragment.newInstance(subredditName)).commit()
    }

    private fun setUpViewModels(trendingAdapter: ExploreTrendingAdapter) =
            viewModel.apply {
                trendingSubs.asLiveData().observe(viewLifecycleOwner) { trendingAdapter.submitList(it) }
                isLoading.asLiveData().observe(viewLifecycleOwner) { binding.progressCircular.visibility = if (it) VISIBLE else GONE }
            }
}