package com.example.lume.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountType(type: AccountTypeEntity)

    @Query("SELECT * FROM account_types")
    fun getAllAccountTypes(): Flow<List<AccountTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeferredPlan(plan: DeferredPlanEntity)

    @Update
    suspend fun updateDeferredPlan(plan: DeferredPlanEntity)

    @Query("SELECT * FROM deferred_plans WHERE status = 'ACTIVE'")
    fun getActiveDeferredPlans(): Flow<List<DeferredPlanEntity>>

    @Query("SELECT * FROM deferred_plans WHERE transactionId = :txId")
    suspend fun getDeferredPlanByTransactionId(txId: String): DeferredPlanEntity?

    @Query("SELECT * FROM transactions ORDER BY dateIso DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories")
    suspend fun getCategoriesSnapshot(): List<CategoryEntity>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :delta WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, delta: Double)

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Transaction
    @Query("SELECT * FROM accounts")
    fun getAllAccountsWithType(): Flow<List<AccountWithType>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateIso DESC, createdAt DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY dateIso DESC, createdAt DESC")
    fun getAllTransactionsDetail(): Flow<List<TransactionDetail>>

    @Query("SELECT * FROM accounts")
    suspend fun getAccountsSnapshot(): List<AccountEntity>

    @Transaction
    suspend fun insertTransactionWithBalanceUpdate(transaction: TransactionEntity) {
        // 1. Insert the transaction
        insertTransaction(transaction)

        // 2. Update account balance if accountId is provided
        val accountId = transaction.accountId
        if (accountId != null) {
            val account = getAccountById(accountId) ?: return
            
            // Logic:
            // Egreso on Debit/Cash/Savings -> subtract from balance
            // Egreso on Credit -> add to balance (debt)
            // Ingreso on Debit/Cash/Savings -> add to balance
            // Ingreso on Credit -> subtract from balance (reduce debt)
            
            val isEgreso = transaction.type == "egreso"
            val isCredit = account.accountTypeId == "CREDIT"
            
            val delta = when {
                isEgreso && !isCredit -> -transaction.amount
                isEgreso && isCredit -> transaction.amount
                !isEgreso && !isCredit -> transaction.amount
                !isEgreso && isCredit -> -transaction.amount
                else -> 0.0
            }
            
            if (delta != 0.0) {
                updateAccountBalance(accountId, delta)
            }
        }
    }

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteDeferredPlan(plan: DeferredPlanEntity)

    @Query("DELETE FROM transactions WHERE accountId = :accountId")
    suspend fun deleteTransactionsByAccountId(accountId: String)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)
}

data class AccountWithType(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "accountTypeId",
        entityColumn = "id"
    )
    val type: AccountTypeEntity?
)

data class TransactionDetail(
    @Embedded val transaction: TransactionEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: AccountEntity?
)
