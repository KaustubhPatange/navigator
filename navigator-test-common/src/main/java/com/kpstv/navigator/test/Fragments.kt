package com.kpstv.navigator.test

import android.os.Parcelable
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kpstv.navigation.BaseArgs
import com.kpstv.navigation.ValueFragment
import kotlinx.android.parcel.Parcelize

abstract class BaseFragment : ValueFragment(R.layout.fragment_abstract) {
    var viewState: ViewState = ViewState.UNDEFINED
    override fun onViewStateChanged(viewState: ViewState) {
        this.viewState = viewState
    }
}

class FirstFragment : BaseFragment()
class SecondFragment : BaseFragment()
class ThirdFragment : BaseFragment()

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
) : BaseArgs(), Parcelable {
    companion object {
        fun create() = TestArgs("Test", TestEnum.First, arrayListOf(1,2,3), MyModel(true))
    }
}