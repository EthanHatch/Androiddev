package org.jdc.template.model.repository

import android.app.Application
import androidx.room.Room
import com.google.common.truth.Truth.assertThat
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.dbtools.android.room.jdbc.JdbcSQLiteOpenHelperFactory
import org.jdc.template.TestFilesystem
import org.jdc.template.inject.CommonTestModule
import org.jdc.template.model.datastore.UserPreferenceDataSource
import org.jdc.template.model.db.main.MainDatabase
import org.jdc.template.model.db.main.MainDatabaseWrapper
import org.jdc.template.model.db.main.individual.Individual
import org.jdc.template.util.log.JavaTree
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

class IndividualRepositoryTest {

    @Inject
    lateinit var individualRepository: IndividualRepository

    @BeforeEach
    fun setUp() {
        Timber.plant(JavaTree())
        TestFilesystem.deleteFilesystem()

        val component = DaggerIndividualRepositoryTestComponent.builder().individualRepositoryTestModule(IndividualRepositoryTestModule()).build()
        component.inject(this)
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun testIndividual() = runBlocking {
        // === CREATE / INSERT ===
        val individual = Individual()
        individual.firstName = "Jeff"
        individual.alarmTime = LocalTime.now()
        individualRepository.saveIndividual(individual)

        assertThat(individualRepository.getIndividualCount()).isEqualTo(1)

        // === UPDATE ===
        individual.firstName = "Jeffery"
        individualRepository.saveIndividual(individual)

        val dbFirstName = individualRepository.getIndividualFirstName(individual.id)
        assertThat(dbFirstName).isEqualTo("Jeffery")

        // === DELETE ===
        individualRepository.deleteIndividual(individual.id)
        assertThat(individualRepository.getIndividualCount()).isEqualTo(0)
    }
}

@Module
@DisableInstallInCheck // prevent Hilt from checking for @InstallIn (https://issuetracker.google.com/issues/158758786)
class IndividualRepositoryTestModule {
    @Provides
    @Singleton
    fun provideUserPreferenceDataSource(): UserPreferenceDataSource {
        return mockk()
    }

    @Provides
    @Singleton
    fun provideMainDatabaseWrapper(application: Application): MainDatabaseWrapper {
        return MainDatabaseTestWrapper(application)
    }
}

@Singleton
@Component(modules = [CommonTestModule::class, IndividualRepositoryTestModule::class])
interface IndividualRepositoryTestComponent {
    fun inject(test: IndividualRepositoryTest)
}

class MainDatabaseTestWrapper(
    application: Application
) : MainDatabaseWrapper(application) {

    override fun createDatabase(): MainDatabase {
        return Room.databaseBuilder(mockk<Application>(), MainDatabase::class.java, MainDatabase.DATABASE_NAME)
            .allowMainThreadQueries()
            .openHelperFactory(JdbcSQLiteOpenHelperFactory(TestFilesystem.INTERNAL_DATABASES_DIR_PATH))
            .build()
    }
}
