package pl.j.reinmar.modules.weather.infrastructure

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.repository.ILocationProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Produkcyjna implementacja dostawcy lokalizacji [ILocationProvider]
 * oparta na Google Play Services Fused Location Provider.
 *
 * Klasa bezpiecznie weryfikuje uprawnienia w czasie działania (Runtime Permissions),
 * zarządza tokenami anulowania zapytań lokalizacyjnych oraz integruje mechanizm
 * zadań Google Play Services z asynchronicznymi korutynami Kotlina.
 *
 * @property context Kontekst aplikacji Android.
 * @property fusedClient Klient geolokalizacji Google Play Services.
 */
class GpsLocationProvider(
    private val context: Context,
    private val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : ILocationProvider {

    /**
     * Weryfikuje, czy aplikacja posiada przynajmniej jedno uprawnienie lokalizacyjne
     * (ACCESS_FINE_LOCATION lub ACCESS_COARSE_LOCATION).
     *
     * @return `true` jeśli uprawnienie zostało nadane przez użytkownika, w przeciwnym razie `false`.
     */
    override fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    /**
     * Asynchronicznie pobiera bieżące współrzędne GPS urządzenia.
     *
     * W pierwszej kolejności podejmuje próbę pobrania dokładnej, bieżącej pozycji
     * za pomocą [FusedLocationProviderClient.getCurrentLocation]. W przypadku braku sygnału,
     * wykorzystuje ostatnią zapamiętaną pozycję [FusedLocationProviderClient.getLastLocation].
     *
     * @return Wynik [Result] zawierający poprawny obiekt [Coordinates]
     *         lub [WeatherException.LocationPermissionDenied] / [WeatherException.LocationUnavailable].
     */
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentCoordinates(): Result<Coordinates> {
        if (!hasLocationPermission()) {
            return Result.failure(WeatherException.LocationPermissionDenied())
        }

        return try {
            val cancellationTokenSource = CancellationTokenSource()

            val location: Location? = suspendCancellableCoroutine { continuation ->
                val task = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

                task.addOnSuccessListener { loc ->
                    if (continuation.isActive) {
                        continuation.resume(loc)
                    }
                }

                task.addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(exception)
                    }
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }

            if (location != null) {
                Coordinates.createSafe(location.latitude, location.longitude)
            } else {
                // Próba odczytu ostatniej znanej pozycji (fallback)
                val lastLocation = getLastKnownLocationFromTask()
                if (lastLocation != null) {
                    Coordinates.createSafe(lastLocation.latitude, lastLocation.longitude)
                } else {
                    Result.failure(WeatherException.LocationUnavailable())
                }
            }
        } catch (e: Exception) {
            Result.failure(WeatherException.LocationUnavailable(e))
        }
    }

    /**
     * Zwraca ostatnią znaną pozycję GPS lub domyślne współrzędne Warszawy.
     *
     * @return Obiekt [Coordinates] z aktualną lub domyślną pozycją.
     */
    override suspend fun getLastKnownCoordinatesOrDefault(): Coordinates {
        return try {
            val result = getCurrentCoordinates()
            result.getOrElse { Coordinates.WARSAW }
        } catch (_: Exception) {
            Coordinates.WARSAW
        }
    }

    /**
     * Pobiera ostatnią znaną pozycję z bufora Google Play Services.
     *
     * @return Obiekt [Location] lub `null` w przypadku braku pozycji.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getLastKnownLocationFromTask(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (continuation.isActive) {
                        continuation.resume(loc)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }
}
