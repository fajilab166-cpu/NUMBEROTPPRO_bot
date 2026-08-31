package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.models.*
import com.example.data.repository.BotRepository
import com.example.data.repository.OperationResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: BotRepository
    private val testUserId = 684920194L

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BotRepository(db.appDao())

        // Insert initial test user
        val testUser = UserEntity(
            userId = testUserId,
            username = "pro_trader_77",
            firstName = "Pro",
            lastName = "Trader",
            mainBalance = 250.0,
            withdrawableBalance = 250.0,
            pendingBalance = 0.0,
            bonusBalance = 20.0,
            totalEarned = 350.0,
            referralCode = "TGPRO889",
            riskScore = 15,
            riskLevel = RiskLevel.LOW
        )
        db.appDao().insertUser(testUser)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Telegram OTP Bot Pro", appName)
    }

    @Test
    fun testPurchaseNumberAndSimulateOtp() = runBlocking {
        val country = CountryEntity("US", "United States", "+1", "🇺🇸", 1.0, 50, true)
        val service = ServiceEntity("tg", "Telegram", "✈️", 25.0, 100, true)

        val purchaseResult = repository.purchaseNumber(testUserId, country, service)
        assertTrue(purchaseResult is OperationResult.Success)

        val order = (purchaseResult as OperationResult.Success).data
        assertEquals("Telegram", order.serviceName)
        assertEquals(OrderStatus.WAITING_OTP, order.status)

        // Check user balance deduction
        val updatedUser = db.appDao().getUser(testUserId)
        assertNotNull(updatedUser)
        assertEquals(225.0, updatedUser!!.withdrawableBalance, 0.01)

        // Simulate OTP arrival
        val otpResult = repository.simulateOtpArrival(order.id)
        assertTrue(otpResult is OperationResult.Success)

        val updatedOrder = db.appDao().getOrderById(order.id)
        assertNotNull(updatedOrder)
        assertEquals(OrderStatus.RECEIVED, updatedOrder!!.status)
        assertNotNull(updatedOrder.otpCode)
    }

    @Test
    fun testDailyBonusClaim() = runBlocking {
        val bonusResult = repository.claimDailyBonus(testUserId)
        assertTrue(bonusResult is OperationResult.Success)

        val userAfterBonus = db.appDao().getUser(testUserId)
        assertNotNull(userAfterBonus)
        assertTrue(userAfterBonus!!.mainBalance > 250.0)
    }

    @Test
    fun testWithdrawalSubmissionAndApproval() = runBlocking {
        val withdrawResult = repository.requestWithdrawal(
            userId = testUserId,
            gateway = PaymentGateway.BKASH,
            accountAddress = "01711223344",
            amount = 100.0
        )
        assertTrue(withdrawResult is OperationResult.Success)

        val withdrawal = (withdrawResult as OperationResult.Success).data
        assertEquals(WithdrawalStatus.PENDING, withdrawal.status)
        assertEquals(100.0, withdrawal.amount, 0.01)

        // Balance should be deducted from withdrawable
        val userAfterWithdraw = db.appDao().getUser(testUserId)
        assertEquals(150.0, userAfterWithdraw!!.withdrawableBalance, 0.01)

        // Admin approves
        val approveResult = repository.adminProcessWithdrawal(
            withdrawalId = withdrawal.id,
            status = WithdrawalStatus.PAID,
            adminNote = "Approved test"
        )
        assertTrue(approveResult is OperationResult.Success)

        val approvedWithdrawal = db.appDao().getWithdrawalById(withdrawal.id)
        assertEquals(WithdrawalStatus.PAID, approvedWithdrawal!!.status)
        assertTrue(approvedWithdrawal.transactionHash.isNotEmpty())
    }
}
