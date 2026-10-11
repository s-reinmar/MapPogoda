package pl.j.reinmar.modules.weather.service

import android.content.Context
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.j.reinmar.modules.weather.infrastructure.GpsLocationProvider
import pl.j.reinmar.modules.weather.infrastructure.WeatherRepositoryImpl
import pl.j.reinmar.modules.weather.infrastructure.api.OpenWeatherApi
import pl.j.reinmar.modules.weather.repository.ILocationProvider
import pl.j.reinmar.modules.weather.repository.IWeatherRepository
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Wzorzec Service Locator / Fabryka wstrzykiwania zależności (Dependency Injection)
 * dla modułu pogodowego.
 *
 * Realizuje zasadę odwrócenia zależności (DIP) poprzez centralną konfigurację
 * klientów sieciowych, repozytoriów oraz przypadków użycia (Use Cases).
 * Umożliwia również podmianę implementacji w testach jednostkowych.
 */
object WeatherServiceLocator {

    /**
     * Bazowy adres URL OpenWeatherMap API.
     */
    private const val OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/"

    /**
     * Opcjonalna instancja klucza API do nadpisania w czasie testów lub konfiguracji.
     */
    @Volatile
    private var customApiKey: String? = null

    /**
     * Buforowana instancja interfejsu Retrofit [OpenWeatherApi].
     */
    @Volatile
    private var cachedApi: OpenWeatherApi? = null

    /**
     * Buforowana instancja repozytorium pogodowego [IWeatherRepository].
     */
    @Volatile
    private var cachedRepository: IWeatherRepository? = null

    /**
     * Buforowana instancja dostawcy lokalizacji [ILocationProvider].
     */
    @Volatile
    private var cachedLocationProvider: ILocationProvider? = null

    /**
     * Ustawia niestandardowy klucz API (np. wprowadzony przez użytkownika w ustawieniach lub w testach).
     *
     * @param apiKey Nowy klucz API OpenWeather.
     */
    fun setCustomApiKey(apiKey: String) {
        customApiKey = apiKey
        // Reset buforowanego repozytorium, aby odświeżyć klucz
        cachedRepository = null
    }

    /**
     * Zwraca aktualny klucz API: zdefiniowany niestandardowo lub odczytany z [BuildConfig.OPENWEATHER_API_KEY].
     *
     * @return Ciąg tekstowy z kluczem API.
     */
    fun getApiKey(): String {
        return customApiKey ?: BuildConfig.OPENWEATHER_API_KEY
    }

    /**
     * Zwraca lub tworzy klienta HTTP OkHttp z konfiguracją timeoutów i logowania.
     *
     * @return Skonfigurowana instancja [OkHttpClient].
     */
    private fun createOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    /**
     * Zwraca lub tworzy instancję Moshi ze wsparciem dla klas Kotlina.
     *
     * @return Skonfigurowana instancja [Moshi].
     */
    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Zwraca instancję API Retrofit [OpenWeatherApi].
     *
     * @return Singleton [OpenWeatherApi].
     */
    fun provideOpenWeatherApi(): OpenWeatherApi {
        return cachedApi ?: synchronized(this) {
            cachedApi ?: Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .client(createOkHttpClient())
                .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
                .build()
                .create(OpenWeatherApi::class.java)
                .also { cachedApi = it }
        }
    }

    /**
     * Zwraca implementację repozytorium pogodowego [IWeatherRepository].
     *
     * @return Instancja [IWeatherRepository].
     */
    fun provideWeatherRepository(): IWeatherRepository {
        return cachedRepository ?: synchronized(this) {
            cachedRepository ?: WeatherRepositoryImpl(
                api = provideOpenWeatherApi(),
                apiKeyProvider = { getApiKey() }
            ).also { cachedRepository = it }
        }
    }

    /**
     * Zwraca implementację dostawcy geolokalizacji [ILocationProvider].
     *
     * @param context Kontekst aplikacji Android.
     * @return Instancja [ILocationProvider].
     */
    fun provideLocationProvider(context: Context): ILocationProvider {
        return cachedLocationProvider ?: synchronized(this) {
            cachedLocationProvider ?: GpsLocationProvider(
                context = context.applicationContext
            ).also { cachedLocationProvider = it }
        }
    }

    /**
     * Udostępnia przypadek użycia pobierania aktualnej pogody [GetCurrentWeatherUseCase].
     *
     * @return Instancja [GetCurrentWeatherUseCase].
     */
    fun provideGetCurrentWeatherUseCase(): GetCurrentWeatherUseCase {
        return GetCurrentWeatherUseCase(provideWeatherRepository())
    }

    /**
     * Udostępnia przypadek użycia pobierania prognozy [GetForecastUseCase].
     *
     * @return Instancja [GetForecastUseCase].
     */
    fun provideGetForecastUseCase(): GetForecastUseCase {
        return GetForecastUseCase(provideWeatherRepository())
    }

    /**
     * Udostępnia przypadek użycia lokalizacji użytkownika [GetUserLocationUseCase].
     *
     * @param context Kontekst aplikacji Android.
     * @return Instancja [GetUserLocationUseCase].
     */
    fun provideGetUserLocationUseCase(context: Context): GetUserLocationUseCase {
        return GetUserLocationUseCase(provideLocationProvider(context))
    }

    /**
     * Udostępnia przypadek użycia wyszukiwania pogody po nazwie miasta [SearchCityWeatherUseCase].
     *
     * @return Instancja [SearchCityWeatherUseCase].
     */
    fun provideSearchCityWeatherUseCase(): SearchCityWeatherUseCase {
        return SearchCityWeatherUseCase(provideWeatherRepository())
    }

    /**
     * Metoda pomocnicza dla środowiska testowego umożliwiająca wstrzyknięcie atrap repozytorium.
     *
     * @param repository Niestandardowa implementacja repozytorium.
     */
    fun setRepositoryForTesting(repository: IWeatherRepository?) {
        cachedRepository = repository
    }

    /**
     * Metoda pomocnicza dla środowiska testowego umożliwiająca wstrzyknięcie atrapy dostawcy lokalizacji.
     *
     * @param provider Niestandardowa implementacja dostawcy lokalizacji.
     */
    fun setLocationProviderForTesting(provider: ILocationProvider?) {
        cachedLocationProvider = provider
    }
}
