package com.kpstv.home_internal

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.kpstv.home_internal.worker.HomeInternalWorker
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeInternalFragment : ValueFragment(R.layout.fragment_home_internal) {

  @Inject lateinit var homeButtonClicked: HomeButtonClicked

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // don't do this, this is just for differentiating nested modules
    requireParentFragment().requireView().findViewById<Toolbar>(R.id.toolbar).title = "Module: home-internal"

    val btnGoto = view.findViewById<Button>(R.id.btn_goto)
    btnGoto.setOnClickListener {
      homeButtonClicked.goToNext(FragmentNavigator.NavOptions(remember = true))
    }

    val workerStatus = view.findViewById<TextView>(R.id.tv_work_status)
    val btnWorker = view.findViewById<Button>(R.id.btn_work)

    workerStatus.text = getString(R.string.worker_status, "NOT STARTED")

    HomeInternalWorker.observe(requireContext()).observe(viewLifecycleOwner) { workInfos ->
      if (workInfos.isNotEmpty()) {
        val latestWork = workInfos.first()
        workerStatus.text = getString(R.string.worker_status, latestWork.state.name)
      }
    }

    btnWorker.setOnClickListener {
      HomeInternalWorker.schedule(requireContext())
    }
  }
}