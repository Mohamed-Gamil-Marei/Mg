package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("حسابات الذهب", appName)
  }

  @Test
  fun `verify gold 21k equivalent calculation`() {
    val grams18k = 21.0
    val eq21 = com.example.data.model.GoldTransaction.calculate21Equivalent(grams18k, 18)
    assertEquals(18.0, eq21, 0.001)

    val grams24k = 21.0
    val eq24to21 = com.example.data.model.GoldTransaction.calculate21Equivalent(grams24k, 24)
    assertEquals(24.0, eq24to21, 0.001)
  }
}
