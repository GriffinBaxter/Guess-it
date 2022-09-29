package nz.ac.canterbury.guessit

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

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
        val bitmap = BitmapFactory.decodeFile(photos[position].thumbnailFile)
        viewHolder.photoView.setImageBitmap(bitmap)
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
