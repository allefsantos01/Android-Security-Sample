package com.developer.allefsousa.totp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.developer.allefsousa.totop.databinding.ActivityHomeBinding
import com.google.android.material.tabs.TabLayout


class HomeActivity : AppCompatActivity(com.developer.allefsousa.totop.R.layout.activity_home) {

    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        funSetupFirst()
        binding.tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> openActivity(GeneratorFragment.newInstance())

                    1 -> openActivity(RSAFragment.newInstance())
                    2-> {

                    }
                    3-> {

                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
    }

    private fun funSetupFirst() {
        openActivity(GeneratorFragment())
    }

    fun openActivity(nameScreen:Fragment){
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.replace(binding.frag.id, nameScreen)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}