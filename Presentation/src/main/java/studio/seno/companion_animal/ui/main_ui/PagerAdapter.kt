package studio.seno.companion_animal.ui.main_ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(fm : FragmentManager, cycle : Lifecycle) : FragmentStateAdapter(fm, cycle){
    private var items = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }

    fun addItem(fragment : Fragment) {
        items.add(fragment)
    }
}
