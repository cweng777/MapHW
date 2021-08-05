package com.weng.maphw.di

import com.weng.maphw.ui.map.MapsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MapsViewModel(get())
    }
}