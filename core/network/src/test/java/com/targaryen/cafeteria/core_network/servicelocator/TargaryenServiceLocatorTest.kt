package com.targaryen.cafeteria.core_network.servicelocator

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TargaryenServiceLocatorTest {
    private interface SampleService {
        fun label(): String
    }

    private class CounterService(
        val id: Int,
    ) : SampleService {
        override fun label(): String = "service-$id"
    }

    @After
    fun tearDown() {
        TargaryenServiceLocator.clear()
    }

    @Test
    fun registerSingleton_returnsSameInstanceAcrossResolutions() {
        val counter = AtomicInteger(0)
        TargaryenServiceLocator.registerSingleton<SampleService> {
            CounterService(counter.incrementAndGet())
        }

        val first = TargaryenServiceLocator.resolve<SampleService>()
        val second = TargaryenServiceLocator.resolve<SampleService>()

        assertTrue(TargaryenServiceLocator.isRegistered<SampleService>())
        assertSame(first, second)
        assertEquals("service-1", first.label())
    }

    @Test
    fun registerFactory_createsNewInstanceOnEveryResolution() {
        val counter = AtomicInteger(0)
        TargaryenServiceLocator.registerFactory<SampleService> {
            CounterService(counter.incrementAndGet())
        }

        val first = TargaryenServiceLocator.resolve<SampleService>()
        val second = TargaryenServiceLocator.resolve<SampleService>()

        assertNotSame(first, second)
        assertEquals("service-1", first.label())
        assertEquals("service-2", second.label())
    }

    @Test(expected = IllegalStateException::class)
    fun resolve_whenUnregistered_throwsIllegalStateException() {
        TargaryenServiceLocator.resolve<SampleService>()
    }

    @Test
    fun clear_removesAllBindings() {
        TargaryenServiceLocator.registerSingleton<SampleService> { CounterService(1) }
        assertTrue(TargaryenServiceLocator.isRegistered<SampleService>())

        TargaryenServiceLocator.clear()

        assertFalse(TargaryenServiceLocator.isRegistered<SampleService>())
    }
}
