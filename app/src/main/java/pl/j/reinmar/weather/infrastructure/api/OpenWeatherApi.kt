package pl.j.reinmar.modules.weather.infrastructure.api

import pl.j.reinmar.modules.weather.infrastructure.api.dto.CurrentWeatherDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.ForecastDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfejs klienta HTTP Retrofit dla OpenWeatherMap API v2.5.
 *
 * Umożliwia pobieranie aktualnej pogody oraz 5-dniowej prognozy
 * na podstawie współrzędnych geograficznych lub nazwy miasta.
 */
interface OpenWeatherApi {

    /**
     * Pobiera aktualną pogodę dla zadanych współrzędnych geograficznych (GPS).
     *
     * @param latitude Szerokość geograficzna w stopniach dziesiętnych.
     * @param longitude Długość geograficzna w stopniach dziesiętnych.
     * @param apiKey Klucz uwierzytelniający OpenWeather (appid).
     * @param units Jednostki pomiarowe (domyślnie "metric" dla Celsjusza i m/s).
     * @param lang Język zwracanych opisów pogodowych (domyślnie "pl").
     * @return Obiekt DTO z danymi aktualnej pogody [CurrentWeatherDto].
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pl"
    ): CurrentWeatherDto

    /**
     * Pobiera aktualną pogodę na podstawie tekstowej nazwy miejscowości.
     *
     * @param query Nazwa miejscowości (np. "Warszawa" lub "Kraków,PL").
     * @param apiKey Klucz uwierzytelniający OpenWeather (appid).
     * @param units Jednostki pomiarowe (domyślnie "metric").
     * @param lang Język zwracanych opisów pogodowych (domyślnie "pl").
     * @return Obiekt DTO z danymi aktualnej pogody [CurrentWeatherDto].
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") query: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pl"
    ): CurrentWeatherDto

    /**
     * Pobiera 5-dniową prognozę pogody (w krokach 3-godzinnych) dla danych współrzędnych.
     *
     * @param latitude Szerokość geograficzna w stopniach dziesiętnych.
     * @param longitude Długość geograficzna w stopniach dziesiętnych.
     * @param apiKey Klucz uwierzytelniający OpenWeather (appid).
     * @param units Jednostki pomiarowe (domyślnie "metric").
     * @param lang Język zwracanych opisów pogodowych (domyślnie "pl").
     * @return Obiekt DTO z prognozą pogody [ForecastDto].
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pl"
    ): ForecastDto
}
