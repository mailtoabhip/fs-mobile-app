package com.dfd.delfin.ui.home.fragments.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.dfd.delfin.R

/**
 * Data class representing a single banner item in the carousel.
 *
 * @param imageUrl URL of the banner image (loaded via Glide)
 * @param imageRes Fallback local drawable resource (used if imageUrl is null)
 * @param deepLink Optional deep link for click handling
 */
data class HomeBannerItem(
    val imageUrl: String? = null,
    val imageRes: Int? = null,
    val deepLink: String? = null
)

/**
 * PagerAdapter for the home screen banner carousel.
 * Supports both remote image URLs (via Glide) and local drawable resources.
 */
class HomeBannerPagerAdapter(
    private val banners: List<HomeBannerItem>,
    private val onBannerClick: ((HomeBannerItem) -> Unit)? = null
) : PagerAdapter() {

    override fun getCount(): Int = banners.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.item_home_banner, container, false)

        val bannerImage = view.findViewById<ImageView>(R.id.bannerImage)
        val banner = banners[position]

        when {
            banner.imageUrl != null -> {
                Glide.with(container.context)
                    .load(banner.imageUrl)
                    .apply(RequestOptions().transform(RoundedCorners(32)))
                    .placeholder(R.drawable.bg_quick_action_card)
                    .into(bannerImage)
            }
            banner.imageRes != null -> {
                bannerImage.setImageResource(banner.imageRes)
            }
        }

        view.setOnClickListener {
            onBannerClick?.invoke(banner)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}
