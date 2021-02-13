package studio.seno.companion_animal.ui.feed

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import studio.seno.companion_animal.R

class SelectedImageAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    ItemTouchHelperListener {
    private var imageItems = mutableListOf<String>()
    private var deleteListener : OnItemDeleteListener? = null
    private val context = context


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ViewHolder(view, deleteListener!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var imageUri = imageItems[position]
        var holder = holder as ViewHolder
        holder.bind(imageUri, context)
    }

    override fun getItemCount(): Int {
        return imageItems.size
    }

    fun setOnDeleteItemListener(deleteListener: OnItemDeleteListener){
        this.deleteListener = deleteListener
    }

    fun addItem(uriString: String) {
        imageItems.add(uriString)
    }

    fun removeItem(uriString: String){
        imageItems.remove(uriString)
    }

    fun getItem(position: Int) : String {
        return imageItems[position]
    }

    fun getItems() : MutableList<String>{
        return imageItems
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val item = imageItems[fromPosition]
        imageItems.removeAt(fromPosition)
        imageItems.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemSwipe(position: Int) {
    }

    class ViewHolder(itemView: View, deleteListener: OnItemDeleteListener) : RecyclerView.ViewHolder(
        itemView
    ){
        private var selectedImageView : ImageView? = null

        init {
            selectedImageView = itemView.findViewById(R.id.imageView)
            itemView.findViewById<ImageButton>(R.id.close_btn).setOnClickListener{
                deleteListener.onDeleted(adapterPosition)
            }
        }

        fun bind(imageUri: String, context: Context) {
            Glide.with(context)
                .load(Uri.parse(imageUri))
                .centerCrop()
                .into(selectedImageView!!)
        }
    }

}