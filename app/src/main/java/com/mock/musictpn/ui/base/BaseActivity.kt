package com.mock.musictpn.ui.base

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.mock.musictpn.views.LoadingDialog
import com.mock.musictpn.views.MessageDialog

abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected lateinit var mBinding: DB
    protected abstract val mViewModel: VM
    private lateinit var errorDialog: MessageDialog

    private lateinit var mLoadingDialog: LoadingDialog

    @LayoutRes
    protected abstract fun getLayoutRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getLayoutRes())
        mLoadingDialog = LoadingDialog(this)
        errorDialog = MessageDialog(this)
        intViewModel()
    }

    private fun intViewModel() {
        mViewModel.isLoading.observe(this) { isShow: Boolean ->
            showLoading(isShow)
        }
        mViewModel.errorMessage.observe(this) { message: String? ->
            message?.let {
                showError(it)
            }
        }
    }

    fun hideKeyBoard() {
        try {
            runOnUiThread {
                try {
                    val inputManager = this@BaseActivity
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(
                        this@BaseActivity.currentFocus?.applicationWindowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                } catch (e: IllegalStateException) {
                } catch (e: Exception) {
                }
            }

        } catch (e: IllegalStateException) {
        } catch (e: Exception) {
        }
    }

    open fun showError(message: String) {
        errorDialog.message = message
        if (!errorDialog.isShowing) {
            errorDialog.show()
        }
    }

    open fun showLoading(isShow: Boolean) {
        if (isShow) {
            mLoadingDialog.show()
        } else {
            if (mLoadingDialog.isShowing) {
                mLoadingDialog.dismiss()
            }
        }
    }


}