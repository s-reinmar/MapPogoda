package pl.j.reinmar.modules.weather.model

/**
 * Reprezentacja typu warunków atmosferycznych w domenie aplikacji.
 *
 * Klasa mapuje kody zjawisk pogodowych na spójne, domenowe stany
 * wraz z ich polskimi nazwami oraz zalecanymi ikonami.
 *
 * @property displayName Nazwa zjawiska pogodowego w języku polskim.
 */
enum class WeatherCondition(val displayName: String) {
    /** Czyste, bezchmurne niebo. */
    CLEAR_SKY("Bezchmurnie"),

    /** Niewielkie zachmurzenie. */
    FEW_CLOUDS("Małe zachmurzenie"),

    /** Umiarkowane zachmurzenie. */
    SCATTERED_CLOUDS("Umiarkowane zachmurzenie"),

    /** Duże zachmurzenie lub całkowite zachmurzenie. */
    BROKEN_CLOUDS("Duże zachmurzenie"),

    /** Przelotny deszcz. */
    SHOWER_RAIN("Przelotny deszcz"),

    /** Deszcz ciągły lub ulewa. */
    RAIN("Deszcz"),

    /** Burza z piorunami i opadami. */
    THUNDERSTORM("Burza z piorunami"),

    /** Opady śniegu. */
    SNOW("Śnieg"),

    /** Mgła, zamglenie lub pył. */
    MIST("Mgła"),

    /** Inne, nietypowe zjawisko meteorologiczne. */
    UNKNOWN("Zmienna pogoda");

    companion object {
        /**
         * Mapuje kod ikony lub identyfikator z OpenWeather na domenowy stan [WeatherCondition].
         *
         * @param weatherId Numeryczny kod stanu z OpenWeather (np. 800 dla czystego nieba, 2xx dla burzy).
         * @return Odpowiadająca wartość typu [WeatherCondition].
         */
        fun fromOpenWeatherId(weatherId: Int): WeatherCondition {
            return when (weatherId) {
                in 200..232 -> THUNDERSTORM
                in 300..321 -> SHOWER_RAIN
                in 500..504 -> RAIN
                511 -> SNOW
                in 520..531 -> SHOWER_RAIN
                in 600..622 -> SNOW
                in 701..781 -> MIST
                800 -> CLEAR_SKY
                801 -> FEW_CLOUDS
                802 -> SCATTERED_CLOUDS
                in 803..804 -> BROKEN_CLOUDS
                else -> UNKNOWN
            }
        }
    }
}
