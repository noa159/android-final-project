package com.project.accommodations.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.project.accommodations.adapter.MyAccommodationAdapter
import com.project.accommodations.databinding.FragmentMyAccommodationsBinding
import com.project.accommodations.model.Accommodation
import androidx.navigation.fragment.findNavController
import com.project.accommodations.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyAccommodationsFragment : Fragment(), MyAccommodationAdapter.AccommodationActionsListener {

    private var _binding: FragmentMyAccommodationsBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { Firebase.auth }
    private lateinit var accommodationsArrayList: ArrayList<Accommodation>
    private lateinit var myAccommodationsAdapter: MyAccommodationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyAccommodationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accommodationsArrayList = ArrayList()

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupRecyclerView()
        getData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        myAccommodationsAdapter = MyAccommodationAdapter(accommodationsArrayList).also {
            it.setOnDeleteCompleteListener(this)
        }
        binding.recyclerView.adapter = myAccommodationsAdapter
    }

    override fun onDelete(id: String, position: Int) {

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val rowsAffected = AppDatabase.getInstance(requireContext()).accommodationDao().deleteById(id)
                if (rowsAffected > 0) {
                    withContext(Dispatchers.Main) {
                        accommodationsArrayList.removeAt(position)
                        myAccommodationsAdapter.notifyDataSetChanged()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Accommodation not found.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error deleting accommodation.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onEdit(id: String, position: Int) {
        val action = MyAccommodationsFragmentDirections.actionMyAccommodationsFragmentToEditFragment(id)
        findNavController().navigate(action)
    }

    private fun getData() {
        auth.currentUser?.email?.let { userEmail ->
            AppDatabase.getInstance(requireContext()).accommodationDao()
                .getByEmail(userEmail).observe(viewLifecycleOwner) { accommodations ->
                    accommodationsArrayList.clear()
                    accommodationsArrayList.addAll(accommodations)
                    myAccommodationsAdapter.notifyDataSetChanged()
                }
        } ?: run {
            Toast.makeText(context, "User email not found.", Toast.LENGTH_LONG).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}