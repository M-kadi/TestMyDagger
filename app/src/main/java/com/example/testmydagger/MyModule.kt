package com.example.testmydagger

import android.content.Context
import com.example.testmydagger.room.UserDao
import com.example.testmydagger.room.UserRepository
import com.example.testmydagger.room.UserRoomDatabase
import com.example.testmydagger.userList.UsersViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
internal class MyModule {
    @Provides
    @Singleton
    fun provideCar3(): Car3 = Car3()

    // Inject room
    @Provides
    @Singleton
    fun provideUserDao(@ApplicationContext appContext: Context): UserDao =
        UserRoomDatabase.getDatabase(appContext).userDao()

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository = UserRepository(userDao)

    @Provides
    @Singleton
    fun provideUsersViewModel(userRepository: UserRepository): UsersViewModel = UsersViewModel(userRepository)
}