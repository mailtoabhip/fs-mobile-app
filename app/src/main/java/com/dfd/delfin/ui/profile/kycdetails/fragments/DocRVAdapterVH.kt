package com.dfd.delfin.ui.profile.kycdetails.fragments

import android.view.View
import androidx.databinding.ViewDataBinding
import com.dfd.delfin.data.doc.DocAction_ViewDetails
import com.dfd.delfin.databinding.*
import com.dfd.delfin.ui.base.BaseViewHolder

/**
 * Base Doc items RV adapter view holder
 */
abstract class BaseDocRVAdapterViewHolder<out B : ViewDataBinding, IT : BaseDocRVAdapterItem<*>>(
        binding: B
) :
        BaseViewHolder<B>(binding) {
  abstract fun bind(
          item: IT,
          _interface: DocRVAdapterInterface
  )

  /**
   * Add on click listener for action
   */
  protected fun View.clickToAction(
          actionId: String,
          item: IT,
          _interface: DocRVAdapterInterface
  ) = setOnClickListener { action(actionId, item, _interface) }

  /**
   * Post action to UI
   */
  protected fun View.action(
          actionId: String,
          item: IT,
          _interface: DocRVAdapterInterface
  ) = post { _interface.handleAction(actionId, item) }
}


/**
 * Doc data item view holder
 */
class DocDataItemVH(binding: ViewProfileKycDocumentItemBinding) :
        BaseDocRVAdapterViewHolder<ViewProfileKycDocumentItemBinding, DocDataItem>(binding) {
  override fun bind(
          item: DocDataItem,
          _interface: DocRVAdapterInterface
  ) {
    if (item.data.docPath.isNullOrEmpty()) {
      _interface.fetchDetails(item.data)
    }
      _interface.showImage(item.data, binding.textDocumentSizeProfile, binding.docImage)
    val arrString =item.data.docUrl.split("/")
    binding.name.text = arrString.get(arrString.size-1) ?: ""
    binding.imageButtonDownloadDocumentProfile.visibility = View.VISIBLE
    binding.textDocumentSizeProfile.visibility = View.VISIBLE
    binding.imageButtonDownloadDocumentProfile.setOnClickListener { _interface.handleAction(DocAction_ViewDetails, item) }
 }
}

/**
 * Search warning item view holder
 */
internal class DocWarningItemVH(binding: ViewWarningItemBinding) :
        BaseDocRVAdapterViewHolder<ViewWarningItemBinding, DocWarningItem>(binding) {
  override fun bind(
          item: DocWarningItem,
          _interface: DocRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Search timeout view holder
 */
internal class DocTimeOutItemVH(binding: ViewTimeOutItemBinding) :
        BaseDocRVAdapterViewHolder<ViewTimeOutItemBinding, GstTimeoutItem>(binding) {
  override fun bind(
          item: GstTimeoutItem,
          _interface: DocRVAdapterInterface
  ) {
    binding.title = item.data.title
    binding.subTitle = item.data.subtitle
    binding.actionLabel = item.data.actionLabel
    binding.btnAction.clickToAction(item.data.actionId, item, _interface)
  }
}

/**
 * Progress inline viewholder
 */
internal class DocProgressItemVH(binding: ViewGstProgressItemBinding) :
        BaseDocRVAdapterViewHolder<ViewGstProgressItemBinding, DocProgressItem>(
                binding
        ) {
  override fun bind(
          item: DocProgressItem,
          _interface: DocRVAdapterInterface
  ) {

  }
}