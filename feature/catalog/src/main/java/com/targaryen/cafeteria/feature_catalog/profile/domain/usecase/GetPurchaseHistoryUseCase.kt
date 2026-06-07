package com.targaryen.cafeteria.feature_catalog.profile.domain.usecase

import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class GetPurchaseHistoryUseCase(
    private val repository: ProfileRepository,
) {
    operator fun invoke(): Flow<Resource<List<PurchaseHistoryItem>, ProfileError>> = repository.getPurchaseHistory()
}
