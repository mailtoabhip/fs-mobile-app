  /**
   * Fragment action observer
   */
  fun fragmentAction(action: BaseHomeFragmentAction) {
    when (action.type) {
      /* navigate to fragment action */
      HomeFragmentActionType.Navigate -> {
        val fragmentType = (action as NavigateHomeFragmentAction).fragmentType
        binding.viewpager.setCurrentItem(fragmentType.position, true)
        binding.toolbarTitle.text = title
      }
      /* update placement badge action */
      HomeFragmentActionType.UpdatePlacementBadge -> {
        val delayedCount = (action as UpdatePlacementBadgeAction).delayedCount
        updatePlacementBadgeCount(delayedCount)
      }
    }
  }

  /**
   * Update placement badge count on bottom navigation
   */
  fun updatePlacementBadgeCount(delayedCount: Int) {
    try {
      val placementMenuItem = binding.bottomNav.menu.findItem(R.id.nav_placements)
      if (delayedCount > 0) {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_placements)
        badge.apply {
          number = delayedCount
          isVisible = true
          backgroundColor = ContextCompat.getColor(this@HomeActivity, R.color.colorDelhiveryRed)
        }
      } else {
        binding.bottomNav.removeBadge(R.id.nav_placements)
      }
    } catch (e: Exception) {
      Log.e("HomeActivity", "Error updating placement badge", e)
    }
  }
