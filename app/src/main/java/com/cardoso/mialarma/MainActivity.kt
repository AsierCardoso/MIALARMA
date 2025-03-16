package com.cardoso.mialarma

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.cardoso.mialarma.utils.PreferenciasManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var preferenciasManager: PreferenciasManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar PreferenciasManager
        preferenciasManager = PreferenciasManager(this)
        
        // Aplicar tema guardado
        preferenciasManager.aplicarTema()
        
        // Aplicar idioma guardado
        aplicarIdioma(preferenciasManager.obtenerIdioma())
        
        setContentView(R.layout.activity_main)

        // Configurar Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_drawer_open,
            R.string.nav_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar NavigationView
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Configurar BottomNavigationView
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reloj -> {
                    cargarFragmento(RelojFragment())
                    true
                }
                R.id.nav_alarma -> {
                    cargarFragmento(AlarmaFragment())
                    true
                }
                R.id.nav_cronometro -> {
                    cargarFragmento(CronometroFragment())
                    true
                }
                R.id.nav_temporizador -> {
                    cargarFragmento(TemporizadorFragment())
                    true
                }
                else -> false
            }
        }

        // Cargar el fragmento inicial
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RelojFragment())
                .commit()
            bottomNav.selectedItemId = R.id.nav_reloj
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_theme -> mostrarDialogoTema()
            R.id.nav_language -> mostrarDialogoIdioma()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun mostrarDialogoTema() {
        val temas = arrayOf(
            getString(R.string.light_mode),
            getString(R.string.dark_mode)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.theme))
            .setSingleChoiceItems(temas, if (preferenciasManager.obtenerTema()) 1 else 0) { dialog, which ->
                val isDarkMode = which == 1
                preferenciasManager.guardarTema(isDarkMode)
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarDialogoIdioma() {
        val idiomas = arrayOf(
            getString(R.string.spanish),
            getString(R.string.english)
        )
        val codigosIdioma = arrayOf("es", "en")
        val idiomaActual = preferenciasManager.obtenerIdioma()
        val seleccionInicial = codigosIdioma.indexOf(idiomaActual)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.language))
            .setSingleChoiceItems(idiomas, seleccionInicial) { dialog, which ->
                val nuevoIdioma = codigosIdioma[which]
                preferenciasManager.guardarIdioma(nuevoIdioma)
                aplicarIdioma(nuevoIdioma)
                recreate()
                dialog.dismiss()
            }
            .show()
    }

    private fun aplicarIdioma(codigoIdioma: String) {
        val locale = Locale(codigoIdioma)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun attachBaseContext(newBase: Context) {
        val preferenciasManager = PreferenciasManager(newBase)
        val idioma = preferenciasManager.obtenerIdioma()
        val locale = Locale(idioma)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    private fun cargarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}