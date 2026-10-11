package pl.j.reinmar.modules.weather.service

import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherForecast
import pl.j.reinmar.modules.weather.repository.IWeatherRepository

/**
 * Przypadek użycia (Use Case) odpowiedzialny za pobranie wielodniowej prognozy pogody.
 *
 * Realizuje zasadę pojedynczej odpowiedzialności (SRP).
 *
 * @property repository Abstrakcja repozytorium pogodowego [IWeatherRepository].
 */
class GetForecastUseCase(
    private val repository: IWeatherRepository
) {
    /**
     * Wykonuje przypadek użycia pobierania prognozy pogody dla podanych współrzędnych.
     *
     * @param coordinates Zweryfikowane współrzędne geograficzne punktu pomiarowego.
     * @return Wynik [Result] zawierający agregat [WeatherForecast] lub wyjątek domenowy.
     */
    suspend operator fun invoke(coordinates: Coordinates): Result<WeatherForecast> {
        return repository.getForecast(coordinates)
    }
}
