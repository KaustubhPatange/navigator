package com.kpstv.navigator.test

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.FragmentNavigator
import com.kpstv.navigation.ValueFragment
import kotlinx.parcelize.Parcelize

abstract class BaseFragment : ValueFragment(R.layout.fragment_abstract) {
    var viewState: ViewState = ViewState.UNDEFINED
    override fun onViewStateChanged(viewState: ViewState) {
        this.viewState = viewState
    }
}

class FirstFragment : BaseFragment()
class SecondFragment : BaseFragment()
class ThirdFragment : BaseFragment()
class ForthFragment : BaseFragment()

class NavigatorFragment : ValueFragment(R.layout.activity_main), FragmentNavigator.Transmitter {
    private lateinit var internalNavigator: FragmentNavigator

    override fun getNavigator(): FragmentNavigator = internalNavigator

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        internalNavigator = FragmentNavigator.with(this, savedInstanceState)
            .initialize(view.findViewById(R.id.my_container))
    }
}

class FirstSheet : BottomSheetDialogFragment()
class SecondSheet : BottomSheetDialogFragment()

@Parcelize
enum class TestEnum : Parcelable {
    First,
    Second
}

@Parcelize
data class MyModel(val whatever: Boolean) : Parcelable

@Parcelize
data class TestArgs(
    val data: String,
    val enum: TestEnum,
    val arrayList: ArrayList<Int>,
    val model: MyModel
) : BaseArgs() {
    companion object {
        fun create() = TestArgs("Test", TestEnum.First, arrayListOf(1,2,3), MyModel(true))
    }
}