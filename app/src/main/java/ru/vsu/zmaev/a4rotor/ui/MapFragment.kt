//package ru.vsu.zmaev.a4rotor.ui
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import com.yandex.mapkit.map.CameraListener
//import com.yandex.mapkit.map.CameraPosition
//import com.yandex.mapkit.map.CameraUpdateReason
//import com.yandex.mapkit.map.Map
//import ru.vsu.zmaev.a4rotor.databinding.FragmentMapBinding
//
//class MapFragment : Fragment(), CameraListener {
//
//    companion object {
//        val MAPKIT_API_KEY = "a7ecdf68-8e9e-48f7-822d-62410141743e"
//    }
//
//    private var _binding: FragmentMapBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentMapBinding.inflate(inflater, container, false)
//        return binding.root;
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onCameraPositionChanged(
//        p0: Map,
//        p1: CameraPosition,
//        p2: CameraUpdateReason,
//        p3: Boolean
//    ) {
//        if (finished) { // Если камера закончила движение
//            when {
//                cameraPosition.zoom >= ZOOM_BOUNDARY && zoomValue <= ZOOM_BOUNDARY -> {
//                    placemarkMapObject.setIcon(ImageProvider.fromBitmap(createBitmapFromVector(R.drawable.ic_pin_blue_svg)))
//                }
//                cameraPosition.zoom <= ZOOM_BOUNDARY && zoomValue >= ZOOM_BOUNDARY -> {
//                    placemarkMapObject.setIcon(ImageProvider.fromBitmap(createBitmapFromVector(R.drawable.ic_pin_red_svg)))
//                }
//            }
//            zoomValue = cameraPosition.zoom // После изменения позиции камеры сохраняем величину зума
//        }
//    }
//}