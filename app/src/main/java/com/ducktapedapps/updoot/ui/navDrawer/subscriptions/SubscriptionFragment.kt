package com.ducktapedapps.updoot.ui.navDrawer.subscriptions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubscriptionBinding
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.LoginState
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragment
import com.ducktapedapps.updoot.utils.Constants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class SubscriptionFragment : Fragment() {
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding: FragmentSubscriptionBinding
        get() = _binding!!

    @Inject
    lateinit var vmFactory: SubscriptionViewModelFactory

    private val activityVM by lazy { ViewModelProvider(requireActivity()).get(ActivityVM::class.java) }

    private val viewModel by lazy { ViewModelProvider(this, vmFactory).get(SubscriptionViewModel::class.java) }

    private val subscriptionAdapter = SubscriptionsAdapter(object : SubscriptionsAdapter.ClickHandler {
        override fun goToSubreddit(subredditName: String) {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_container, SubredditFragment.newInstance(subredditName))
                    .commit()
            binding.searchView.clearFocus()
            (requireActivity() as MainActivity).collapseBottomNavDrawer()
        }
    })

    override fun onAttach(context: Context) {
        (requireActivity().application as UpdootApplication).updootComponent.inject(this)
        super.onAttach(context)
    }

    private fun setUpViewModel() {
        viewModel.apply {
            results.asLiveData().observe(viewLifecycleOwner) {
                subscriptionAdapter.submitList(it)
            }
        }
        activityVM.loginState.asLiveData().observe(viewLifecycleOwner) { user ->
            when (user) {
                LoginState.LoggedOut -> viewModel.setCurrentUser(Constants.ANON_USER)
                is LoginState.LoggedIn -> viewModel.setCurrentUser(user.userName)
            }
        }
    }

    private fun setUpViews() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = subscriptionAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        when (newState) {
                            RecyclerView.SCROLL_STATE_IDLE -> fab.show()
                            RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                                fab.hide()
                                searchView.visibility = View.GONE
                                if (searchView.hasFocus()) searchView.clearFocus()
                            }
                        }
                    }
                })
            }
            lifecycleScope.launch {
                searchView.getQueryFlow()
                        .debounce(Constants.DEBOUNCE_TIME_OUT)
                        .filterNotNull()
                        .distinctUntilChanged()
                        .collectLatest {
                            viewModel.searchSubreddit(it)
                        }
            }
            fab.setOnClickListener {
                searchView.apply {
                    this.visibility = when (this.visibility) {
                        View.GONE, View.INVISIBLE -> {
                            searchView.isFocusable = true
                            searchView.isIconified = false
                            View.VISIBLE
                        }
                        else -> View.GONE
                    }
                }
            }
        }

    }

    private fun SearchView.getQueryFlow(): StateFlow<String?> {
        val query = MutableStateFlow<String?>("")
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                query.value = newText
                return true
            }
        })
        return query
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSubscriptionBinding.inflate(inflater, container, false)
        setUpViewModel()
        setUpViews()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}