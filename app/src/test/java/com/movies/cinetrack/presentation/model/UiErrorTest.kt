package com.movies.cinetrack.presentation.model

import com.movies.cinetrack.R
import com.movies.cinetrack.domain.model.DataError
import org.junit.Assert.assertEquals
import org.junit.Test

class UiErrorTest {
    @Test fun technicalFailuresUseTheSameGenericMessageWithoutLosingTheirCategory() {
        for (error in listOf(DataError.UNAUTHORIZED, DataError.SERVER, DataError.UNKNOWN)) {
            assertEquals(error.name, error.toUiError().name)
            assertEquals(R.string.error_unknown, error.toUiError().messageResId())
        }
    }

    @Test fun actionableFailuresKeepTheirSpecificMessages() {
        assertEquals(R.string.error_network, DataError.NETWORK.toUiError().messageResId())
        assertEquals(R.string.error_rate_limited, DataError.RATE_LIMITED.toUiError().messageResId())
        assertEquals(R.string.error_not_found, DataError.NOT_FOUND.toUiError().messageResId())
    }
}
