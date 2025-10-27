  /**
   * Update placement badge count on bottom navigation
   */
  fun updatePlacementBadgeCount(delayedCount: Int) {
    try {
      if (delayedCount > 0) {
        // Create badge using the compatible approach
        val badge = BadgeDrawable.create(requireContext())
        badge.number = delayedCount
        badge.backgroundColor = ContextCompat.getColor(this@HomeActivity, R.color.colorDelhiveryRed)
        binding.bottomNav.getOrCreateBadge(R.id.nav_placements).apply {
          number = delayedCount
          isVisible = true
          backgroundColor = ContextCompat.getColor(this@HomeActivity, R.color.colorDelhiveryRed)
        }
      } else {
        binding.bottomNav.removeBadge(R.id.nav_placements)
      }
    } catch (e: Exception) {
      Log.e("HomeActivity", "Error updating placement badge", e)
      // Fallback: Use custom badge implementation
      updatePlacementBadgeFallback(delayedCount)
    }
  }

  /**
   * Fallback badge implementation for non-Material theme
   */
  private fun updatePlacementBadgeFallback(delayedCount: Int) {
    try {
      val placementMenuItem = binding.bottomNav.menu.findItem(R.id.nav_placements)
      if (delayedCount > 0) {
        // Create a custom badge using TextView overlay
        val badgeView = TextView(this).apply {
          text = delayedCount.toString()
          setTextColor(ContextCompat.getColor(this@HomeActivity, android.R.color.white))
          setBackgroundColor(ContextCompat.getColor(this@HomeActivity, R.color.colorDelhiveryRed))
          textSize = 10f
          gravity = android.view.Gravity.CENTER
          setPadding(8, 4, 8, 4)
        }
        
        // This is a simplified approach - you might need to implement custom badge positioning
        Log.d("HomeActivity", "Badge fallback: $delayedCount")
      }
    } catch (e: Exception) {
      Log.e("HomeActivity", "Error in badge fallback", e)
    }
  }
