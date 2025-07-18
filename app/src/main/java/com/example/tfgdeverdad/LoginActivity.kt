package com.example.tfgdeverdad

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {
    // LoginActivity: Maneja el inicio de sesión con email/contraseña y Google Sign-In usando Firebase


    //aquí guardo lo que se comparte entre pantallas
    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
    }
    //acceso al sistema de autenticación Firebase
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    //variables para recoger en los inputs
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout //por los terminos y condiciones

    private lateinit var googleSignInClient: GoogleSignInClient
    //identificador de la acción. Código de solicitud para el intent de inicio de sesión de Google
    private val RC_SIGN_IN = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //ocultar lineatrLayout
        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        // Cierra sesión al iniciar la actividad. Usado para pruebas rápidas
       /*mAuth.signOut()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show() */


// Configuración de inicio ed sesión con google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser //para acceder a todos los valores relacionados con el user
        if (currentUser != null) goHome(
            currentUser.email.toString(),
            currentUser.providerId
        ) //verifica si este usuario existe o no?
    }

    //Reescribe el botón "atras" para cerrar la app si ya está logueadoo
    override fun onBackPressed() {
        super.onBackPressed()
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    //Listener del botón login. Llama a loginuser(), donde se hace el inicio de sesion
    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        //login real con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->  //task es una expresion lambda y this hace que la funcion se cumpla con el inicio de sesion especificamente
                if (task.isSuccessful) goHome(
                    email,
                    "email"
                ) //si el inicio va bien, manda a la pagina principal
                else {
                    Toast.makeText(this, "Este usuario no existe, regístrate.", Toast.LENGTH_SHORT).show()
                    lyTerms.visibility = View.VISIBLE
                }
            }
    }

    //Guarda los datos de inicio de sesión y pasa a MainActivity
    private fun goHome(
        email: String,
        provider: String
    ) {
        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    //lanza la pantalla de registro
    fun SignIn(view: View) {
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
    }

    //lanza la pantalla de términos y condiciones
    fun goTerms(v: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }


    fun forgotPassword(view: View) {
        resetPassword()
    }
    //comprueba si hay un correo escrito y envía un correo de recuperación
    private fun resetPassword() {
        var e = etEmail.text.toString()
        if (!TextUtils.isEmpty(e)) { //si el campo del email no está vacío
            mAuth.sendPasswordResetEmail(e) //enviar la contraseña por Email
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Toast.makeText(this, "Email enviado a $e", Toast.LENGTH_SHORT).show() //Si se envía bien el correo, que aparezca ese mensaje en pantalla
                    else Toast.makeText(this, "No se encontró el usuario con este correo", Toast.LENGTH_SHORT).show()
                }
        }
        else Toast.makeText(this, "Indica un email", Toast.LENGTH_SHORT). show()


    }

    //abre la pantalla de Google
    fun loginWithGoogle(view: View) {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    //Guardo aquí su id de inici de sesión para autenticarlo en Firebase
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!) //Autentica el usuario en  Firebase usando el token de Google recibido
            } catch (e: ApiException) {
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    goHome(user?.email ?: "no-email", "google")
                } else {
                    Toast.makeText(this, "Falló la autenticación con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }




}