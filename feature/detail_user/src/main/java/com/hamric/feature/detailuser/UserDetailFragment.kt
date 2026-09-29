package com.hamric.feature.detailuser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.hamric.feature.detailuser.databinding.FragmentUserDetailBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private val login: String by lazy {
        requireArguments().getString("login").orEmpty()
    }

    private val viewModel: UserDetailViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = bars.top,
                bottom = bars.bottom,
                left = bars.left,
                right = bars.right
            )
            insets
        }

        binding.buttonRetry.setOnClickListener { viewModel.retry() }

        viewModel.state.observe(viewLifecycleOwner) { state -> render(state) }

        viewModel.load(login)
    }

    private fun render(state: UserDetailUiState) {
        val user = state.user

        binding.progressBar.visibility =
            if (state.isLoading && user == null) View.VISIBLE else View.GONE

        binding.groupError.visibility =
            if (state.error != null) View.VISIBLE else View.GONE
        binding.textError.text = state.error.orEmpty()

        if (user == null) {
            binding.textName.text = ""
            binding.textLogin.text = ""
            binding.textBio.visibility = View.GONE
            return
        }

        Glide.with(binding.imageAvatar)
            .load(user.avatarUrl)
            .placeholder(android.R.color.darker_gray)
            .into(binding.imageAvatar)

        binding.textName.text = user.name ?: user.login
        binding.textLogin.text = "@${user.login}"

        binding.textBio.text = user.bio.orEmpty()
        binding.textBio.visibility = if (user.bio.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.textRepos.text = user.publicRepos.toString()
        binding.textFollowers.text = user.followers.toString()
        binding.textFollowing.text = user.following.toString()

        renderMeta(binding.textCompany, "\uD83C\uDFE2", user.company)
        renderMeta(binding.textLocation, "\uD83D\uDCCD", user.location)
        renderMeta(binding.textBlog, "\uD83D\uDD17", user.blog)
        renderMeta(
            binding.textTwitter,
            "\uD83D\uDC26",
            user.twitterUsername?.let { "@$it" }
        )
    }

    private fun renderMeta(view: android.widget.TextView, emoji: String, value: String?) {
        if (value.isNullOrBlank()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = "$emoji $value"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}