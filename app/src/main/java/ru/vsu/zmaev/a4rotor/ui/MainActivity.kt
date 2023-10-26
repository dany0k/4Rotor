package ru.vsu.zmaev.a4rotor.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.yandex.mapkit.Animation
import com.yandex.mapkit.Animation.Type
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.GeoObjectSelectionMetadata
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.ToponymObjectMetadata
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import ru.vsu.zmaev.a4rotor.R
import ru.vsu.zmaev.a4rotor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), CameraListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mapObjectCollection: MapObjectCollection // Коллекция различных объектов на карте
    private lateinit var placemarkMapObject: PlacemarkMapObject // Геопозиционированный объект (метка со значком) на карте
    private val startLocation = Point(59.9402, 30.315) // Координаты Эрмитажа
    private var zoomValue: Float = 16.5f // Величина зума
    lateinit var searchManager: SearchManager
    lateinit var searchSession: Session

    private val mapObjectTapListener = MapObjectTapListener { mapObject, point ->
        Toast.makeText(applicationContext, "Эрмитаж — музей изобразительных искусств", Toast.LENGTH_SHORT).show()
        true
    }

    private val geoObjectTapListener = object : GeoObjectTapListener {
        override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {
            val selectionMetadata: GeoObjectSelectionMetadata = geoObjectTapEvent
                .geoObject
                .metadataContainer
                .getItem(GeoObjectSelectionMetadata::class.java)
            binding.mapview.map.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)
            return false
        }
    }

    private val searchListener = object : Session.SearchListener {
        override fun onSearchResponse(response: Response) {
            val street = response.collection.children.firstOrNull()?.obj
                ?.metadataContainer
                ?.getItem(ToponymObjectMetadata::class.java)
                ?.address
                ?.components
                ?.firstOrNull { it.kinds.contains(Address.Component.Kind.STREET) }
                ?.name ?: "Информация об улице не найдена"

            Toast.makeText(applicationContext, street, Toast.LENGTH_SHORT).show()
        }

        override fun onSearchError(p0: Error) {}
    }

    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            searchSession = searchManager.submit(point, 20, SearchOptions(), searchListener)
        }

        override fun onMapLongTap(map: Map, point: Point) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setApiKey(savedInstanceState) // Проверяем: был ли уже ранее установлен API-ключ в приложении. Если нет - устанавливаем его.
        MapKitFactory.initialize(this) // Инициализация библиотеки для загрузки необходимых нативных библиотек.
        binding = ActivityMainBinding.inflate(layoutInflater) // Раздуваем макет только после того, как установили API-ключ
        setContentView(binding.root) // Размещаем пользовательский интерфейс в экране активности
        moveToStartLocation() // Перемещаем камеру в определенную область на карте
        setMarkerInStartLocation() // Устанавливаем маркер на карте
        binding.mapview.map.addCameraListener(this) // Добавляем карте слушатель камеры для слежки за изменением величины зума
        binding.mapview.map.addTapListener(geoObjectTapListener) // Добавляем слушатель тапов по объектам
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE) // Инициализирует поисковый менеджер
        binding.mapview.map.addInputListener(inputListener) // Добавляем слушатель тапов по карте с извлечением информации
    }

    override fun onCameraPositionChanged(
        map: Map,
        cameraPosition: CameraPosition,
        cameraUpdateReason: CameraUpdateReason,
        finished: Boolean
    ) {
        if (finished) { // Если камера закончила движение
            when {
                cameraPosition.zoom >= ZOOM_BOUNDARY && zoomValue <= ZOOM_BOUNDARY -> {
                    placemarkMapObject.setIcon(ImageProvider.fromBitmap(createBitmapFromVector(R.drawable.ic_drone)))
                }
                cameraPosition.zoom <= ZOOM_BOUNDARY && zoomValue >= ZOOM_BOUNDARY -> {
                    placemarkMapObject.setIcon(ImageProvider.fromBitmap(createBitmapFromVector(R.drawable.ic_drone)))
                }
            }
            zoomValue = cameraPosition.zoom // После изменения позиции камеры сохраняем величину зума
        }
    }

    private fun setMarkerInStartLocation() {
        val marker = createBitmapFromVector(R.drawable.ic_drone)
        mapObjectCollection = binding.mapview.map.mapObjects // Инициализируем коллекцию различных объектов на карте
        placemarkMapObject =
            mapObjectCollection.addPlacemark(startLocation, ImageProvider.fromBitmap(marker)) // Добавляем метку со значком
        placemarkMapObject.opacity = 0.5f // Устанавливаем прозрачность метке
        placemarkMapObject.addTapListener(mapObjectTapListener) //Добавляем слушатель клика на метку
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        ) ?: return null
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun moveToStartLocation() {
        binding.mapview.map.move(
            CameraPosition(startLocation, zoomValue, 0.0f, 0.0f), // Позиция камеры
            Animation(Type.SMOOTH, 2f), // Красивая анимация при переходе на стартовую точку
            null
        )
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean("haveApiKey") ?: false // При первом запуске приложения всегда false
        if (!haveApiKey) {
            MapKitFactory.setApiKey(MAPKIT_API_KEY) // API-ключ должен быть задан единожды перед инициализацией MapKitFactory
        }
    }

    // Если Activity уничтожается (например, при нехватке памяти или при повороте экрана) - сохраняем информацию, что API-ключ уже был получен ранее
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("haveApiKey", true)
    }

    // Отображаем карты перед тем моментом, когда активити с картой станет видимой пользователю:
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

    // Останавливаем обработку карты, когда активити с картой становится невидимым для пользователя:
    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    companion object {
        const val MAPKIT_API_KEY = "a7ecdf68-8e9e-48f7-822d-62410141743e" //6ed44a4b-6543-4064-bebd-3029ebe6a1b9
        const val ZOOM_BOUNDARY = 16.4f
    }
}