package com.targaryen.cafeteria.feature_checkout.appfunctions

import androidx.appfunctions.AppFunctionContext
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckoutAppFunctionsTest {
    private val firestore: FirebaseFirestore = mockk(relaxed = true)
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true)
    private val appFunctionContext: AppFunctionContext = mockk(relaxed = true)
    private val collectionRef: CollectionReference = mockk(relaxed = true)
    private val queryTask: Task<QuerySnapshot> = mockk(relaxed = true)
    private val querySnapshot: QuerySnapshot = mockk(relaxed = true)
    private val mockUser: FirebaseUser = mockk(relaxed = true)

    private lateinit var checkoutAppFunctions: CheckoutAppFunctions

    @Before
    fun setUp() {
        every { firestore.collection("orders") } returns collectionRef
        every { collectionRef.whereEqualTo("userId", any()) } returns collectionRef
        every { collectionRef.get() } returns queryTask
        every { queryTask.isComplete } returns true
        every { queryTask.isSuccessful } returns true
        every { queryTask.result } returns querySnapshot
        every { queryTask.exception } returns null

        checkoutAppFunctions = CheckoutAppFunctions(firestore, firebaseAuth)
    }

    @Test
    fun getLastOrderStatus_whenUserNotAuthenticated_returnsUnauthenticatedStatus() =
        runTest {
            every { firebaseAuth.currentUser } returns null

            val response = checkoutAppFunctions.getLastOrderStatus(appFunctionContext)

            assertEquals("Não Autenticado", response.status)
            assertTrue(response.message.contains("Nenhum usuário imperial autenticado"))
        }

    @Test
    fun getLastOrderStatus_whenNoOrdersExist_returnsNoOrdersStatus() =
        runTest {
            every { firebaseAuth.currentUser } returns mockUser
            every { mockUser.uid } returns "user_123"
            every { querySnapshot.documents } returns emptyList()

            val response = checkoutAppFunctions.getLastOrderStatus(appFunctionContext)

            assertEquals("Nenhum Pedido", response.status)
            assertTrue(response.message.contains("Você ainda não realizou nenhum pedido"))
        }

    @Test
    fun getLastOrderStatus_withMultipleOrders_returnsMostRecentByDateMillis() =
        runTest {
            every { firebaseAuth.currentUser } returns mockUser
            every { mockUser.uid } returns "user_123"

            val olderDoc: DocumentSnapshot = mockk(relaxed = true)
            every { olderDoc.getString("status") } returns "Entregue"
            every { olderDoc.getString("itemsSummary") } returns "1x Capuccino Porto Real"
            every { olderDoc.getDouble("totalPrice") } returns 13.9
            every { olderDoc.getLong("dateMillis") } returns 1000L

            val recentDoc: DocumentSnapshot = mockk(relaxed = true)
            every { recentDoc.getString("status") } returns "Preparando"
            every { recentDoc.getString("itemsSummary") } returns "2x Café Expresso Valíria"
            every { recentDoc.getDouble("totalPrice") } returns 17.0
            every { recentDoc.getLong("dateMillis") } returns 5000L

            every { querySnapshot.documents } returns listOf(olderDoc, recentDoc)

            val response = checkoutAppFunctions.getLastOrderStatus(appFunctionContext)

            assertEquals("Preparando", response.status)
            assertEquals("2x Café Expresso Valíria", response.summary)
            assertEquals(17.0, response.totalPrice, 0.01)
            assertEquals(5000L, response.dateMillis)
            assertTrue(response.message.contains("2x Café Expresso Valíria com status 'Preparando'"))
        }
}
