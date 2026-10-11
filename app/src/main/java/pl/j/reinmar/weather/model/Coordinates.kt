package pl.j.reinmar.modules.weather.model

/**
 * Obiekt wartości (Value Object) reprezentujący współrzędne geograficzne.
 *
 * Klasa zapewnia niezmienność danych oraz gwarantuje poprawność wartości
 * szerokości i długości geograficznej zgodnie ze standardem WGS 84.
 *
 * @property latitude Szerokość geograficzna w stopniach (zakres od -90.0 do 90.0).
 * @property longitude Długość geograficzna w stopniach (zakres od -180.0 do 180.0).
 */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Szerokość geograficzna musi mieścić się w przedziale [-90.0, 90.0]. Otrzymano: $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Długość geograficzna musi mieścić się w przedziale [-180.0, 180.0]. Otrzymano: $longitude"
        }
    }

    /**
     * Zwraca sformatowany ciąg tekstowy ze współrzędnymi do celów prezentacji.
     *
     * @return Czytelny ciąg w formacie np. "52.23°N, 21.01°E".
     */
    fun toReadableString(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format(java.util.Locale.US, "%.2f°%s, %.2f°%s", Math.abs(latitude), latDir, Math.abs(longitude), lonDir)
    }

    companion object {
        /**
         * Domyślna lokalizacja rezerwowa (Warszawa, Polska).
         */
        val WARSAW: Coordinates = Coordinates(52.2297, 21.0122)

        /**
         * Fabryka bezpiecznie tworząca obiekt [Coordinates], zwracająca [Result].
         *
         * @param latitude Szerokość geograficzna.
         * @param longitude Długość geograficzna.
         * @return Wynik [Result] z poprawnym obiektem [Coordinates] lub błędem [WeatherException.InvalidCoordinatesException].
         */
        fun createSafe(latitude: Double, longitude: Double): Result<Coordinates> {
            return try {
                Result.success(Coordinates(latitude, longitude))
            } catch (e: IllegalArgumentException) {
                Result.failure(WeatherException.InvalidCoordinatesException(latitude, longitude, e.message ?: "Nieprawidłowe współrzędne"))
            }
        }
    }
}
