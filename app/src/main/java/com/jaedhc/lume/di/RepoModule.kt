package com.jaedhc.lume.di

import com.jaedhc.lume.data.repo.AccountRepoRoom
import com.jaedhc.lume.data.repo.CategoryRulesRepoRoom
import com.jaedhc.lume.data.repo.MerchantRepoRoom
import com.jaedhc.lume.data.repo.TransactionQueryRepoRoom
import com.jaedhc.lume.data.repo.TransactionRepoRoom
import com.jaedhc.lume.domain.parser.ports.AccountRepo
import com.jaedhc.lume.domain.parser.ports.CategoryRulesRepo
import com.jaedhc.lume.domain.parser.ports.MerchantRepo
import com.jaedhc.lume.domain.parser.ports.TransactionQueryRepo
import com.jaedhc.lume.domain.parser.ports.TransactionRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
abstract class RepoModule {
    @Binds @Singleton abstract fun bindTxRepo(impl: TransactionRepoRoom): TransactionRepo
    @Binds @Singleton abstract fun bindTxQueryRepo(impl: TransactionQueryRepoRoom): TransactionQueryRepo
    @Binds @Singleton abstract fun bindAccountRepo(impl: AccountRepoRoom): AccountRepo
    @Binds @Singleton abstract fun bindMerchantRepo(impl: MerchantRepoRoom): MerchantRepo
    @Binds @Singleton abstract fun bindCategoryRulesRepo(impl: CategoryRulesRepoRoom): CategoryRulesRepo

}