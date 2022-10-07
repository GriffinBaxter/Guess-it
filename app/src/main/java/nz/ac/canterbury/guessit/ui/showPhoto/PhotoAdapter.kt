package nz.ac.canterbury.guessit.ui.showPhoto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import nz.ac.canterbury.guessit.database.Photo
import nz.ac.canterbury.guessit.R

class PhotoAdapter(private var photos: List<Photo>, private val onPhotoListener: OnPhotoListener)
    : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View, val onPhotoListener: OnPhotoListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val photoView: ImageView

        init {
            photoView = itemView.findViewById(R.id.photoView)
            photoView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            onPhotoListener.onPhotoClick(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo, parent, false)
        return PhotoViewHolder(view, onPhotoListener)
    }

    override fun onBindViewHolder(viewHolder: PhotoViewHolder, position: Int) {
        Picasso.get()
            .load("file:" + photos[position].file)
            .resize(256, 256)
            .centerCrop()
            .into(viewHolder.photoView)
    }

    override fun getItemCount() = photos.size

    fun setData(newPhotos: List<Photo>) {
        photos = newPhotos
        notifyDataSetChanged()
    }

    interface OnPhotoListener {
        fun onPhotoClick(position: Int)
    }
}
