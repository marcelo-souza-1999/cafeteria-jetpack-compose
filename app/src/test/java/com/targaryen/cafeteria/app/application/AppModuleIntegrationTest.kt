package com.targaryen.cafeteria.app.application

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.annotation.Single

class AppModuleIntegrationTest {
    @Test
    fun appModule_declaresFirebaseAuthDefinition() {
        val appModuleClass = AppModule::class.java
        val authMethod =
            appModuleClass.declaredMethods.find { method ->
                method.returnType == FirebaseAuth::class.java
            }
        assertTrue("AppModule deve declarar provider para FirebaseAuth", authMethod != null)
        assertTrue(
            "Provider de FirebaseAuth deve ser anotado com @Single",
            authMethod?.isAnnotationPresent(Single::class.java) == true,
        )
    }

    @Test
    fun appModule_declaresFirebaseFirestoreDefinition() {
        val appModuleClass = AppModule::class.java
        val firestoreMethod =
            appModuleClass.declaredMethods.find { method ->
                method.returnType == FirebaseFirestore::class.java
            }
        assertTrue("AppModule deve declarar provider para FirebaseFirestore", firestoreMethod != null)
        assertTrue(
            "Provider de FirebaseFirestore deve ser anotado com @Single",
            firestoreMethod?.isAnnotationPresent(Single::class.java) == true,
        )
    }

    @OptIn(KoinInternalApi::class)
    @Test
    fun appModule_koinModule_containsFirebaseDefinitions() {
        val appModule = AppModule()
        val module = appModule.module()
        val declaredTypes =
            module.mappings.keys.map { key ->
                key
            }

        val hasAuth =
            declaredTypes.any { typeName ->
                typeName.contains("FirebaseAuth")
            }
        val hasFirestore =
            declaredTypes.any { typeName ->
                typeName.contains("FirebaseFirestore")
            }

        assertTrue("Koin AppModule deve registrar o binding de FirebaseAuth", hasAuth)
        assertTrue("Koin AppModule deve registrar o binding de FirebaseFirestore", hasFirestore)
    }
}
