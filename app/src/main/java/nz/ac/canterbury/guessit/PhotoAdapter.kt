package nz.ac.canterbury.guessit

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PhotoAdapter(private val photos: List<Photo>) : RecyclerView.Adapter<PhotoAdapter.PhotoHolder>() {

    class PhotoHolder(view: View) : RecyclerView.ViewHolder(view) {
        val photoView: ImageView = view.findViewById(R.id.photoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.photo, parent, false)
        return PhotoHolder(view)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: PhotoHolder, i: Int) {
        val bitmap = BitmapFactory.decodeFile(photos[i].file.absolutePath)
        holder.photoView.setImageBitmap(bitmap)
    }
}
