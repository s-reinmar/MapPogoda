package pl.j.reinmar.modules.weather.service

import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.repository.IWeatherRepository

/**
 * Przypadek użycia (Use Case) odpowiedzialny za pobranie aktualnych danych pogodowych.
 *
 * Realizuje zasadę pojedynczej odpowiedzialności (SRP).
 *
 * @property repository Abstrakcja repozytorium pogodowego [IWeatherRepository].
 */
class GetCurrentWeatherUseCase(
    private val repository: IWeatherRepository
) {
    /**
     * Wykonuje przypadek użycia pobierania pogody dla podanych współrzędnych.
     *
     * @param coordinates Zweryfikowane współrzędne geograficzne punktu pomiarowego.
     * @return Wynik [Result] zawierający encję [WeatherData] lub wyjątek domenowy.
     */
    suspend operator fun invoke(coordinates: Coordinates): Result<WeatherData> {
        return repository.getCurrentWeather(coordinates)
    }
}
