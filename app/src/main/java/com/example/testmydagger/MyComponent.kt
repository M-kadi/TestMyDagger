package com.example.testmydagger

import com.example.testmydagger.dataModule.ViewModelModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [MyModule::class, ContextModule::class, ViewModelModule::class])
interface MyComponent {
    fun inject(target: MainActivity)
}
