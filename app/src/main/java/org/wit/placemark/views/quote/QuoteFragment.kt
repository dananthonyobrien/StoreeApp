package org.wit.placemark.views.quote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.wit.placemark.R

class QuoteFragment : Fragment() {

    private lateinit var quoteViewModel: QuoteViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        quoteViewModel =
                ViewModelProvider(this).get(QuoteViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_quote, container, false)
        //val textView: TextView = root.findViewById(R.id.text_slideshow)
        quoteViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        })
        return root
    }
}