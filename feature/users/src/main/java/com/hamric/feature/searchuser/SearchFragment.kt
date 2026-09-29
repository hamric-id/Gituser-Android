package com.hamric.feature.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hamric.feature.users.databinding.FragmentSearchBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private val adapter by lazy { SearchUserAdapter(onClick = { /* detail next step */ }) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top,
                bottom = systemBars.bottom,
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }

        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUsers.adapter = adapter
        binding.recyclerUsers.addOnScrollListener(scrollListener)

        binding.editSearch.doAfterTextChanged { text ->
            viewModel.onQueryChange(text?.toString().orEmpty())
        }

        binding.buttonRetry.setOnClickListener { viewModel.retry() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0) return
            val lm = rv.layoutManager as? LinearLayoutManager ?: return
            val lastVisible = lm.findLastVisibleItemPosition()
            val total = lm.itemCount
            if (total == 0) return
            if (lastVisible >= total - 3) {
                when (viewModel.state.value?.mode) {
                    SearchUiState.Mode.LIST -> viewModel.onListScrolledToEnd()
                    SearchUiState.Mode.SEARCH -> viewModel.onSearchScrolledToEnd()
                    null -> Unit
                }
            }
        }
    }

    private fun render(state: SearchUiState) {
        binding.searchProgress.visibility = if (state.isSearching) View.VISIBLE else View.GONE
        binding.pagingProgress.visibility =  if (state.isLoadingMore) View.VISIBLE else View.GONE

        binding.groupError.visibility = if (state.error != null) View.VISIBLE else View.GONE
        binding.textError.text =
            if(state.error == null)  ""
            else "Result Showed from Cached because ${state.error}"

        val showEmpty = state.isEmpty ||
                (state.mode == SearchUiState.Mode.LIST
                        && state.users.isEmpty()
                        && !state.isLoadingMore
                        && state.error == null)
        binding.textEmpty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
        binding.textEmpty.text =
            if (state.mode == SearchUiState.Mode.SEARCH && state.query.isNotBlank()) {
                getString(R.string.search_empty, state.query)
            }else{
                getString(R.string.list_empty)
            }

        adapter.submitList(state.users)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerUsers.removeOnScrollListener(scrollListener)
        binding.recyclerUsers.adapter = null
        _binding = null
    }
}