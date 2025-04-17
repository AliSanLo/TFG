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
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
    }
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var email by Delegates.notNull<String>() // Deelgates.notNull se asegura de que String no sea null
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var lyTerms: LinearLayout //por los terminos y condiciones

    //private lateinit var mAuth: FirebaseAuth //para la autentificaciond el usuario


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //ocultar lineatrLayout
        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
       //mAuth.signOut() // Cierra sesión al iniciar la actividad
        //Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show() // <-- Prueba si se ejecuta

    }


    public override fun onStart() {
        super.onStart()
        val currentUser =
            FirebaseAuth.getInstance().currentUser //para acceder a todos los valores relacionados con el user
        if (currentUser != null) goHome(
            currentUser.email.toString(),
            currentUser.providerId
        ) //verifica si este usuario existe o no?

    }

    override fun onBackPressed() {
        super.onBackPressed() //PUEDE QUE ESTA LINEA NO SIRVA Y HAYA QUE BORRARLO
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain) //cada vez q e inicie si ya esta iniciada la sesion se abrira la ventan de inicio directamente
    }

    fun login(view: View) {
        loginUser()
    }

    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()


        //inicio de sesion
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->  //task es una expresion lambda y this hace que la funcion se cumpla con el inicio de sesion especificamente
                if (task.isSuccessful) goHome(
                    email,
                    "email"
                ) //si el inicio va bien, manda a la pagina principal
                else {
                    if (lyTerms.visibility == View.INVISIBLE) lyTerms.visibility =
                        View.VISIBLE //si no va bien, hacer visible los terminos y condiciones
                    else {
                        var cbAcept =
                            findViewById<CheckBox>(R.id.cbAcept) //si el checkbox está checkeado, derivar a la funcion Registrar
                        if (cbAcept.isChecked) register()
                    }
                }
            }


    }

    private fun goHome(
        email: String,
        provider: String
    ) { //que receurde los datosde inicio de sesion
        useremail = email
        providerSession = provider

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) { // si funciona, nada
                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())

                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email)
                        .set(hashMapOf("user" to email, "dateRegister" to dateRegister))


                    goHome(email, "email")
                } else Toast.makeText(this, "Error, algo ha salido mal :(", Toast.LENGTH_SHORT).show()
            }
    }

    fun goTerms(v: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(view: View) {
        resetPassword()
    }


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
}