package di

import com.example.oficiolocal.data.repository.FakeProviderRepository
import com.example.oficiolocal.data.repository.FakeRequestRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.data.repository.RequestRepository

/** Inyección manual para arrancar. Se puede migrar a Hilt después. */
object ServiceLocator {
    val providerRepository: ProviderRepository by lazy { FakeProviderRepository() }
    val requestRepository: RequestRepository by lazy { FakeRequestRepository() }
}