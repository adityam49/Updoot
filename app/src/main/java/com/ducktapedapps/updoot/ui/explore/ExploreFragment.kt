package com.ducktapedapps.updoot.ui.explore

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentExploreBinding
import com.ducktapedapps.updoot.ui.explore.search.SearchAdapter
import com.ducktapedapps.updoot.ui.explore.trending.ExploreTrendingAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                findNavController().navigate(ExploreFragmentDirections.actionExploreDestinationToSubredditDestination().setSubreddit(subredditName))
            }
        })
        binding.apply {
            root.post { animateSearchView(searchAdapter, trendingAdapter) }
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

    private fun setUpViewModels(trendingAdapter: ExploreTrendingAdapter, searchAdapter: SearchAdapter) = viewModel.apply {
        trendingSubs.observe(viewLifecycleOwner) {

            trendingAdapter.submitList(it)
        }
        searchResults.observe(viewLifecycleOwner) {
            searchAdapter.submitList(it)
        }
        isLoading.observe(viewLifecycleOwner) { binding.progressCircular.visibility = if (it) View.VISIBLE else View.GONE }
    }


    private fun animateSearchView(searchAdapter: SearchAdapter, trendingAdapter: ExploreTrendingAdapter) {
        val margin = (resources.displayMetrics.density * 16).toInt()

        val changeBounds = ChangeBounds().apply {
            duration = 300
            addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    setUpViewModels(trendingAdapter, searchAdapter)
                }

                override fun onTransitionResume(transition: Transition) = Unit

                override fun onTransitionPause(transition: Transition) = Unit

                override fun onTransitionCancel(transition: Transition) = Unit

                override fun onTransitionStart(transition: Transition) {
                    ObjectAnimator.ofFloat(
                            binding.cardView,
                            "radius",
                            0f,
                            16f
                    ).apply {
                        duration = 300
                        start()
                    }
                }
            })
        }
        TransitionManager.beginDelayedTransition(binding.root, changeBounds)

        ConstraintSet().apply {
            clone(binding.root)
            setMargin(R.id.card_view, ConstraintSet.START, margin)
            setMargin(R.id.card_view, ConstraintSet.END, margin)
            setMargin(R.id.card_view, ConstraintSet.TOP, margin)
            setMargin(R.id.card_view, ConstraintSet.BOTTOM, margin)
            applyTo(binding.root)
        }
    }

}