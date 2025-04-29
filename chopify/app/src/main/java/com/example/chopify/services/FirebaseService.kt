package com.example.chopify.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
}