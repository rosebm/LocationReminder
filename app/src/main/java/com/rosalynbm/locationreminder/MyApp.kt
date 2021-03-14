package com.rosalynbm.locationreminder

import android.app.Application
import com.rosalynbm.locationreminder.authentication.AuthenticationViewModel
import com.rosalynbm.locationreminder.locationreminders.data.ReminderDataSource
import com.rosalynbm.locationreminder.locationreminders.data.local.LocalDB
import com.rosalynbm.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.rosalynbm.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.rosalynbm.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.rosalynbm.locationreminder.utils.NetworkMonitor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        //  To start cheking the network state, we fire up the callback
        NetworkMonitor(this).startNetworkCallback()
        val preferences = this.getSharedPreferences("LocationReminder", 0)

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@MyApp) }

            single {
                AuthenticationViewModel(this@MyApp, preferences)
            }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}