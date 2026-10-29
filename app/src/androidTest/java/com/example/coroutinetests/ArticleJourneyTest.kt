package com.example.coroutinetests

import android.app.LocaleManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Handler
import android.os.LocaleList
import android.os.Looper
import android.os.SystemClock
import android.view.PixelCopy
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.hamcrest.Matcher
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class ArticleJourneyTest {
    private val activityRule = ActivityScenarioRule(MainActivity::class.java)
    private var previousLocales: LocaleList? = null
    private val localeRule = object : ExternalResource() {
        override fun before() {
            val language = InstrumentationRegistry.getArguments().getString("qaLanguage") ?: return
            require(language in setOf("en", "es"))
            val manager = InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(LocaleManager::class.java)
            previousLocales = manager.applicationLocales
            manager.applicationLocales = LocaleList.forLanguageTags(language)
        }
        override fun after() {
            previousLocales?.let {
                InstrumentationRegistry.getInstrumentation().targetContext.getSystemService(LocaleManager::class.java).applicationLocales = it
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(localeRule).around(activityRule)
    private fun text(id: Int): String {
        var result = ""
        activityRule.scenario.onActivity { result = it.getString(id) }
        return result
    }
    private fun revealLoadAction() {
        onView(withId(R.id.contentRoot)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isAssignableFrom(NestedScrollView::class.java)
            override fun getDescription() = "Swipe until the load action is visible, at most six gestures"
            override fun perform(controller: UiController, view: View) {
                val button = view.findViewById<Button>(R.id.loadButton)
                repeat(6) {
                    if (isDisplayingAtLeast(90).matches(button)) return
                    swipeUp().perform(controller, view)
                }
                assertTrue("The load action was not reachable by scrolling", isDisplayingAtLeast(90).matches(button))
            }
        })
    }
    private fun startLoad() {
        revealLoadAction()
        val loading = text(R.string.loading_state)
        onView(withId(R.id.loadButton)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = org.hamcrest.Matchers.allOf(isAssignableFrom(Button::class.java), isDisplayed(), isEnabled())
            override fun getDescription() = "Click and verify the immediate duplicate-load guard"
            override fun perform(controller: UiController, view: View) {
                assertTrue(view.height / view.resources.displayMetrics.density >= 48f)
                view.performClick()
                assertFalse(view.isEnabled)
                assertEquals(loading, view.rootView.findViewById<TextView>(R.id.stateName).text.toString())
            }
        })
    }
    private fun awaitSuccess() {
        val expected = text(R.string.success_state)
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "Wait for the fixed article result, at most ten seconds"
            override fun perform(controller: UiController, view: View) {
                val deadline = SystemClock.uptimeMillis() + 10_000
                while (view.findViewById<TextView>(R.id.stateName).text.toString() != expected) {
                    if (SystemClock.uptimeMillis() >= deadline) fail("The article did not reach Success")
                    controller.loopMainThreadForAtLeast(20)
                }
            }
        })
        onView(withId(R.id.loadButton)).check(matches(isEnabled()))
    }
    private fun capture(name: String) {
        if (InstrumentationRegistry.getArguments().getString("captureScreenshots") != "true") return
        val latch = CountDownLatch(1)
        var image: Bitmap? = null
        var result = -1
        var output: File? = null
        activityRule.scenario.onActivity { activity ->
            activity.findViewById<NestedScrollView>(R.id.contentRoot).scrollTo(0, 0)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        activityRule.scenario.onActivity { activity ->
            val decor = activity.window.decorView
            val bitmap = Bitmap.createBitmap(decor.width, decor.height, Bitmap.Config.ARGB_8888)
            image = bitmap
            val language = activity.resources.configuration.locales[0].language
            val mode = if (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) "dark" else "light"
            File(activity.filesDir, "mini-typography-$language-$mode.json").writeText(
                "{\"title_weight\":${activity.findViewById<TextView>(R.id.titleLabel).typeface.weight}," +
                    "\"state_weight\":${activity.findViewById<TextView>(R.id.stateName).typeface.weight}," +
                    "\"font_scale\":${activity.resources.configuration.fontScale}}"
            )
            output = File(activity.filesDir, "$name-$language-$mode.png")
            PixelCopy.request(activity.window, bitmap, { code -> result = code; latch.countDown() }, Handler(Looper.getMainLooper()))
        }
        assertTrue("Window capture timed out", latch.await(5, TimeUnit.SECONDS))
        assertEquals(PixelCopy.SUCCESS, result)
        output!!.outputStream().use { image!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image!!.recycle()
    }
    @Test fun loadDisablesTheActionUntilTheConfirmedResult() {
        onView(withId(R.id.stateName)).check(matches(withText(text(R.string.idle_state))))
        capture("mini-idle")
        startLoad(); awaitSuccess()
        onView(withId(R.id.stateLabel)).check(matches(withText(org.hamcrest.Matchers.containsString("Coroutines and Flow"))))
        capture("mini-success")
    }
    @Test fun recreationKeepsTheRequestResult() {
        startLoad(); awaitSuccess()
        activityRule.scenario.recreate()
        onView(withId(R.id.stateName)).check(matches(withText(text(R.string.success_state))))
        revealLoadAction()
        onView(withId(R.id.loadButton)).check(matches(isEnabled()))
    }
    @Test fun anotherRequestCanStartAfterThePreviousResult() {
        startLoad(); awaitSuccess()
        startLoad(); awaitSuccess()
        onView(withId(R.id.loadButton)).check(matches(withText(text(R.string.reload_button))))
    }
}
