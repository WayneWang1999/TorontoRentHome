package com.example.torontorenthome.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.torontorenthome.viewmodels.FavoriteFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteFragmentViewModel::class.java)) {
            return FavoriteFragmentViewModel(firestore, firebaseAuth) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}