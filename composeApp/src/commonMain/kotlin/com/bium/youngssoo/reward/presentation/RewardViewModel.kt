package com.bium.youngssoo.reward.presentation

import androidx.lifecycle.ViewModel
import com.bium.youngssoo.reward.domain.RewardRepository

class RewardViewModel(val rewardRepository: RewardRepository) : ViewModel() {
    val totalPoints = rewardRepository.totalPoints
    val mathDailyCount = rewardRepository.mathDailyCount
    val vocabDailyCount = rewardRepository.vocabDailyCount
    val weeklyTotalCount = rewardRepository.weeklyTotalCount

    val isDailyRewardClaimed = rewardRepository.isDailyRewardClaimed
    val isWeeklyRewardClaimed = rewardRepository.isWeeklyRewardClaimed

    fun claimDailyReward() {
        rewardRepository.claimDailyReward()
    }

    fun claimWeeklyReward() {
        rewardRepository.claimWeeklyReward()
    }
}
