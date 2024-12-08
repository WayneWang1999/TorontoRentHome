package com.example.torontorenthome.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.torontorenthome.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class AccountFragment : Fragment() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        emailEditText = view.findViewById(R.id.etName)
        passwordEditText = view.findViewById(R.id.etPassword)
        val loginButton = view.findViewById<View>(R.id.btnLogin)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        return view
    }

    private fun validateInput(email: String, password: String): Boolean {
        // Simple validation
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // User is logged in successfully
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // Fetch user data from Firestore after login
                        fetchUserData(it)
                    }
                } else {
                    // Login failed
                    Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchUserData(user: FirebaseUser) {
        val userId = user.uid
        val userRef = firestore.collection("tenants").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Handle user data from Firestore
                    val name = document.getString("name") ?: "No name"
                    Toast.makeText(requireContext(), "Welcome, $name", Toast.LENGTH_SHORT).show()
                    // You can navigate to another fragment or do any other operation here
                } else {
                    Toast.makeText(requireContext(), "User data not found in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}