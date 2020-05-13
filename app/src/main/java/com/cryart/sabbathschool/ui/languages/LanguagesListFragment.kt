/*
 * Copyright (c) 2020 Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cryart.sabbathschool.ui.languages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.cryart.sabbathschool.R
import com.cryart.sabbathschool.data.di.ViewModelFactory
import com.cryart.sabbathschool.extensions.arch.observeNonNull
import com.google.android.material.transition.MaterialFadeThrough
import dagger.android.support.DaggerDialogFragment
import javax.inject.Inject

class LanguagesListFragment : DaggerDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: LanguagesListViewModel by viewModels { viewModelFactory }

    private lateinit var languagesListAdapter: LanguagesListAdapter

    private var codeSelectedCallback: ((String) -> Unit)? = null

    override fun getTheme(): Int = R.style.AppTheme_DialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough.create()
        exitTransition = MaterialFadeThrough.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ss_fragment_langauges_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.navClose).setOnClickListener { dismiss() }

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                languagesListAdapter.search(newText, null)
                return false
            }
        })

        languagesListAdapter = LanguagesListAdapter {
            codeSelectedCallback?.invoke(it.code)
            dismiss()
        }
        val listView = view.findViewById<RecyclerView>(R.id.languagesListView)
        listView.apply {
            adapter = languagesListAdapter
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.list_divider)!!)
            })
        }

        viewModel.languagesLiveData.observeNonNull(viewLifecycleOwner) {
            languagesListAdapter.setData(it)
        }
    }

    companion object {
        fun newInstance(callback: (String) -> Unit): LanguagesListFragment {
            return LanguagesListFragment().also {
                it.codeSelectedCallback = callback
            }
        }
    }
}