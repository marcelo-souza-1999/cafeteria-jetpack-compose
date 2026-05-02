package com.targaryen.cafeteria.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

import org.koin.core.annotation.Single

interface DataRepository {
  val data: Flow<List<String>>
}

@Single
class DefaultDataRepository : DataRepository {
  override val data: Flow<List<String>> = flow { emit(listOf("Android")) }
}
