package com.targaryen.cafeteria.core_network.servicelocator

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Implementação canônica e isolada de Service Locator / Container nativo em Kotlin puro
 * (sem dependências externas de framework).
 *
 * Documentação explícita de trade-offs arquiteturais frente ao **Koin Annotations** (motor oficial do app):
 *
 * 1. **Memória e Scoping:** Este container oferece apenas escopo global ([Binding.Singleton]) e transiente
 *    ([Binding.Factory]). Ao contrário de `@KoinViewModel`, não há vinculação automática ao ciclo de vida de
 *    `ViewModelStoreOwner` ou `Activity`; registrar referências visuais como Singleton gera memory leaks.
 * 2. **Acoplamento (Dependência Implícita):** Consumidores que invocam [resolve] diretamente ocultam suas
 *    dependências fora do construtor, dificultando inspeção estática e violando o princípio de inversão explícita.
 *    O app em produção preserva injeção pura por construtor gerenciada pelo Koin.
 * 3. **Segurança em Compilação:** Ausências de registro falham apenas em runtime ([IllegalStateException]),
 *    enquanto o `koinCompiler` (`compileSafety = true`, `strictSafety = true`) valida o grafo no build.
 * 4. **Testabilidade:** Estado estático compartilhado exige chamada obrigatória de [clear] no teardown de testes
 *    para impedir vazamento entre execuções; na DI pura basta instanciar a classe com mocks no construtor.
 */
object TargaryenServiceLocator {
    private sealed interface Binding<out T : Any> {
        fun get(): T

        class Singleton<out T : Any>(
            factory: () -> T,
        ) : Binding<T> {
            private val instance: T by lazy(LazyThreadSafetyMode.SYNCHRONIZED, factory)

            override fun get(): T = instance
        }

        class Factory<out T : Any>(
            private val factory: () -> T,
        ) : Binding<T> {
            override fun get(): T = factory()
        }
    }

    private val bindings = ConcurrentHashMap<KClass<*>, Binding<*>>()

    fun <T : Any> registerSingleton(
        clazz: KClass<T>,
        factory: () -> T,
    ) {
        bindings[clazz] = Binding.Singleton(factory)
    }

    fun <T : Any> registerFactory(
        clazz: KClass<T>,
        factory: () -> T,
    ) {
        bindings[clazz] = Binding.Factory(factory)
    }

    inline fun <reified T : Any> registerSingleton(noinline factory: () -> T) {
        registerSingleton(T::class, factory)
    }

    inline fun <reified T : Any> registerFactory(noinline factory: () -> T) {
        registerFactory(T::class, factory)
    }

    fun <T : Any> resolve(clazz: KClass<T>): T {
        val binding =
            bindings[clazz]
                ?: error("Dependência não registrada no TargaryenServiceLocator: ${clazz.qualifiedName}")
        val resolved = binding.get()
        return requireNotNull(clazz.javaObjectType.cast(resolved)) {
            "Instância incompatível para ${clazz.qualifiedName}"
        }
    }

    inline fun <reified T : Any> resolve(): T = resolve(T::class)

    fun isRegistered(clazz: KClass<*>): Boolean = bindings.containsKey(clazz)

    inline fun <reified T : Any> isRegistered(): Boolean = isRegistered(T::class)

    fun clear() {
        bindings.clear()
    }
}
