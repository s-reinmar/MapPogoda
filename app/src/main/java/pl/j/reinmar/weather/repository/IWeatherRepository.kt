package pl.j.reinmar.modules.weather.repository

import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.model.WeatherForecast

/**
 * Kontrakt (interfejs) dostępu do danych meteorologicznych.
 *
 * Realizuje zasadę odwrócenia zależności (DIP) oraz segregacji interfejsów (ISP).
 * Warstwa domeny i logiki aplikacji polega wyłącznie na tej abstrakcji,
 * niezależnie od konkretnego dostawcy API (np. OpenWeather) czy mechanizmu buforowania.
 */
interface IWeatherRepository {

    /**
     * Pobiera aktualne dane pogodowe dla podanych współrzędnych geograficznych.
     *
     * @param coordinates Poprawne współrzędne punktu na Ziemi.
     * @return Wynik operacji [Result] zawierający [WeatherData] w przypadku sukcesu
     *         lub [pl.j.reinmar.modules.weather.model.WeatherException] w przypadku błędu.
     */
    suspend fun getCurrentWeather(coordinates: Coordinates): Result<WeatherData>

    /**
     * Pobiera wielodniową prognozę pogody dla podanych współrzędnych geograficznych.
     *
     * @param coordinates Poprawne współrzędne punktu na Ziemi.
     * @return Wynik operacji [Result] zawierający agregat [WeatherForecast]
     *         lub [pl.j.reinmar.modules.weather.model.WeatherException] w przypadku błędu.
     */
    suspend fun getForecast(coordinates: Coordinates): Result<WeatherForecast>

    /**
     * Wyszukuje dane pogodowe dla wpisanej nazwy miejscowości lub miasta.
     *
     * @param cityName Nazwa poszukiwanego miasta (np. "Warszawa", "Gdańsk").
     * @return Wynik operacji [Result] z danymi [WeatherData] lub błędem.
     */
    suspend fun searchWeatherByCity(cityName: String): Result<WeatherData>
}
