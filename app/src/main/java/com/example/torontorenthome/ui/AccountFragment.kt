package com.example.torontorenthome.ui
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.torontorenthome.R
import com.example.torontorenthome.databinding.FragmentAccountBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI() // Ensure the UI is updated when the fragment is resumed
       }

    private fun updateUI() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            showLoggedInUI(currentUser.email ?: "User")
        } else {
            showLoggedOutUI()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etName.error = "Please enter a valid email"
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        return isValid
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                 //   val user = firebaseAuth.currentUser
                    updateUI()
                    val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(
                        R.id.bottomNavigationView)
                    bottomNavigationView.selectedItemId = R.id.miFavorite
                } else {
                    // Handle login failure (you can show a toast or error message here)

                }
            }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        // Check that the user is null before updating the UI
        val isUserSignedOut = firebaseAuth.currentUser == null
        if (isUserSignedOut) {
            updateUI()
        } else {
            Log.e("LogoutError", "Sign-out not fully completed")
        }
    }
    private fun showLoggedInUI(email: String) {
        setVisibility(loggedIn = true, email = email)
    }

    private fun showLoggedOutUI() {
        setVisibility(loggedIn = false)
    }

    private fun setVisibility(loggedIn: Boolean, email: String = "") {
        if (loggedIn) {
            binding.etName.visibility = View.GONE
            binding.etPassword.visibility = View.GONE
            binding.btnLogin.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.tvLoginUser.text = email
            binding.tvLoginUser.visibility = View.VISIBLE
        } else {
            binding.etName.visibility = View.VISIBLE
            binding.etPassword.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.tvLoginUser.text = ""
            binding.tvLoginUser.visibility = View.GONE
        }
    }
}

