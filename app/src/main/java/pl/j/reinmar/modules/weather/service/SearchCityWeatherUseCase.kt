package pl.j.reinmar.modules.weather.service

import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.repository.IWeatherRepository

/**
 * Przypadek użycia (Use Case) odpowiedzialny za wyszukiwanie pogody dla podanej nazwy miasta.
 *
 * Realizuje zasadę pojedynczej odpowiedzialności (SRP).
 *
 * @property repository Abstrakcja repozytorium pogodowego [IWeatherRepository].
 */
class SearchCityWeatherUseCase(
    private val repository: IWeatherRepository
) {
    /**
     * Wyszukuje pogodę dla miasta.
     *
     * @param cityName Nazwa miasta wpisana przez użytkownika.
     * @return Wynik [Result] zawierający dane pogodowe [WeatherData] lub wyjątek.
     */
    suspend operator fun invoke(cityName: String): Result<WeatherData> {
        return repository.searchWeatherByCity(cityName)
    }
}
