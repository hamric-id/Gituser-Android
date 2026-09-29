package com.hamric.feature.searchuser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hamric.feature.searchuser.databinding.FragmentSearchBinding
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

        binding.recyclerUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUsers.adapter = adapter

        binding.editSearch.doAfterTextChanged { text ->
            viewModel.onQueryChange(text?.toString().orEmpty())
        }

        binding.buttonRetry.setOnClickListener { viewModel.retry() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)
        }
    }

    private fun render(state: SearchUiState) {
        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        binding.groupError.visibility = if (state.error != null) View.VISIBLE else View.GONE
        binding.textError.text = state.error.orEmpty()

        binding.textEmpty.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
        binding.textEmpty.text = getString(R.string.search_empty, state.query)

        adapter.submitList(state.users)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerUsers.adapter = null
        _binding = null
    }
}