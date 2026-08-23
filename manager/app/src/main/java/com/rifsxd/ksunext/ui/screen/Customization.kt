package com.rifsxd.ksunext.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.rifsxd.ksunext.ui.LocalScrollState
import com.rifsxd.ksunext.ui.rememberScrollConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.compose.dropUnlessResumed
import com.rifsxd.ksunext.ui.MainActivity
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ksuApp
import com.rifsxd.ksunext.ui.component.SwitchItem
import com.rifsxd.ksunext.ui.component.rememberCustomDialog
import com.rifsxd.ksunext.ui.util.refreshActivity
import com.rifsxd.ksunext.ui.util.LocalSnackbarHost
import com.rifsxd.ksunext.ui.util.LocaleHelper

/**
 * @author rifsxd
 * @date 2025/6/1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun CustomizationScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    // Bottom bar scroll tracking
    val bottomBarScrollState = LocalScrollState.current
    val bottomBarScrollConnection = if (bottomBarScrollState != null) {
        rememberScrollConnection(
            isScrollingDown = bottomBarScrollState.isScrollingDown,
            scrollOffset = bottomBarScrollState.scrollOffset,
            previousScrollOffset = bottomBarScrollState.previousScrollOffset,
            threshold = 30f
        )
    } else null
    val snackBarHost = LocalSnackbarHost.current

    val isManager = Natives.isManager
    val ksuVersion = if (isManager) Natives.version else null

    val scrollState = LocalScrollState.current
    val isNavBarHidden = scrollState?.isScrollingDown?.value ?: false
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + if (isNavBarHidden) 0.dp else 112.dp

    Scaffold(
        topBar = {
            TopBar(
                onBack = dropUnlessResumed {
                    navigator.popBackStack()
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost, modifier = Modifier.padding(bottom = navBarPadding)) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
    
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .let { modifier ->
                    if (bottomBarScrollConnection != null) {
                        modifier
                            .nestedScroll(bottomBarScrollConnection)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    }
                }
                .verticalScroll(rememberScrollState())
        ) {

            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

            // Track language state with current app locale
            var currentAppLocale by remember { mutableStateOf(LocaleHelper.getCurrentAppLocale(context)) }
            
            // Listen for preference changes
            LaunchedEffect(Unit) {
                currentAppLocale = LocaleHelper.getCurrentAppLocale(context)
            }

            // Language setting with selection dialog
            val languageDialog = rememberCustomDialog { dismiss ->
                // In-app selector on all Android versions. On 13+ the locale is
                // applied through LocaleManager; jumping to the system picker is
                // avoided because some OEM settings apps crash on it (#987).
                run {
                    // Dynamically detect supported locales from resources
                    val supportedLocales = remember {
                        val locales = mutableListOf<java.util.Locale>()
                        
                        // Add system default first
                        locales.add(java.util.Locale.ROOT) // This will represent "System Default"
                        
                        // Dynamically detect available locales by checking resource directories
                        val resourceDirs = listOf(
                            "ar", "bg", "de", "fa", "fr", "hu", "in", "it", 
                            "ja", "ko", "pl", "pt-rBR", "ru", "th", "tr", 
                            "uk", "vi", "zh-rCN", "zh-rTW"
                        )
                        
                        resourceDirs.forEach { dir ->
                            try {
                                val locale = when {
                                    dir.contains("-r") -> {
                                        val parts = dir.split("-r")
                                        java.util.Locale.Builder()
                                            .setLanguage(LocaleHelper.normalizeLegacyLanguageCode(parts[0]))
                                            .setRegion(parts[1])
                                            .build()
                                    }
                                    else -> java.util.Locale.Builder()
                                        .setLanguage(LocaleHelper.normalizeLegacyLanguageCode(dir))
                                        .build()
                                }
                                
                                // Test if this locale has translated resources
                                val config = android.content.res.Configuration()
                                config.setLocale(locale)
                                val localizedContext = context.createConfigurationContext(config)
                                
                                // Try to get a translated string to verify the locale is supported
                                val testString = localizedContext.getString(R.string.settings_language)
                                val defaultString = context.getString(R.string.settings_language)
                                
                                // If the string is different or it's English, it's supported
                                if (testString != defaultString || locale.language == "en") {
                                    locales.add(locale)
                                }
                            } catch (_: Exception) {
                                // Skip unsupported locales
                            }
                        }
                        
                        // Sort by display name
                        val sortedLocales = locales.drop(1).sortedBy { it.getDisplayName(it) }
                        mutableListOf<java.util.Locale>().apply {
                            add(locales.first()) // System default first
                            addAll(sortedLocales)
                        }
                    }
                    
                    val allOptions = supportedLocales.map { locale ->
                        val tag = if (locale == java.util.Locale.ROOT) {
                            "system"
                        } else if (locale.country.isEmpty()) {
                            locale.language
                        } else {
                            "${locale.language}_${locale.country}"
                        }
                        
                        val displayName = if (locale == java.util.Locale.ROOT) {
                            context.getString(R.string.system_default)
                        } else {
                            locale.getDisplayName(locale)
                        }
                        
                        tag to displayName
                    }
                    
                    val currentLocale = prefs.getString("app_locale", "system") ?: "system"
                    val options = allOptions.map { (tag, displayName) ->
                        ListOption(
                            titleText = displayName,
                            selected = currentLocale == tag
                        )
                    }
                    
                    var selectedIndex by remember { 
                        mutableIntStateOf(allOptions.indexOfFirst { (tag, _) -> currentLocale == tag })
                    }
                    
                    ListDialog(
                        state = rememberUseCaseState(
                            visible = true,
                            onFinishedRequest = {
                                if (selectedIndex >= 0 && selectedIndex < allOptions.size) {
                                    val newLocale = allOptions[selectedIndex].first
                                    LocaleHelper.setPreferredLocale(context, newLocale)
                                    
                                    // Update local state immediately
                                    currentAppLocale = LocaleHelper.getCurrentAppLocale(context)
                                    
                                    // Apply locale change immediately for Android < 13;
                                    // on Android 13+ LocaleManager recreates automatically
                                    if (!LocaleHelper.useSystemLanguageSettings) {
                                        refreshActivity(context)
                                    }
                                }
                                dismiss()
                            },
                            onCloseRequest = {
                                dismiss()
                            }
                        ),
                        header = Header.Default(
                            title = stringResource(R.string.settings_language),
                        ),
                        selection = ListSelection.Single(
                            showRadioButtons = true,
                            options = options
                        ) { index, _ ->
                            selectedIndex = index
                        }
                    )
                }
            }

            val language = stringResource(id = R.string.settings_language)
            
            // Compute display name based on current app locale (similar to the reference implementation)
            val currentLanguageDisplay = remember(currentAppLocale) {
                val locale = currentAppLocale
                if (locale != null) {
                    locale.getDisplayName(locale)
                } else {
                    context.getString(R.string.system_default)
                }
            }
            
            ListItem(
                leadingContent = { Icon(Icons.Filled.Translate, language) },
                headlineContent = { Text(
                    text = language,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                ) },
                supportingContent = { Text(currentLanguageDisplay) },
                modifier = Modifier.clickable {
                    languageDialog.show()
                }
            )

            var useBanner by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("use_banner", true)
                )
            }
            if (ksuVersion != null) {
                SwitchItem(
                    icon = Icons.Filled.ViewCarousel,
                    title = stringResource(id = R.string.settings_banner),
                    summary = stringResource(id = R.string.settings_banner_summary),
                    checked = useBanner
                ) {
                    prefs.edit { putBoolean("use_banner", it) }
                    useBanner = it
                }
            }

            var enableAmoled by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("enable_amoled", false)
                )
            }
            if (isSystemInDarkTheme()) {
                val activity = LocalContext.current as? MainActivity
                SwitchItem(
                    icon = Icons.Filled.Contrast,
                    title = stringResource(id = R.string.settings_amoled_mode),
                    summary = stringResource(id = R.string.settings_amoled_mode_summary),
                    checked = enableAmoled
                ) { checked ->
                    activity?.setAmoledMode(checked)
                    enableAmoled = checked
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val activity = LocalContext.current as? MainActivity
                var enableDynamicColor by rememberSaveable {
                    mutableStateOf(prefs.getBoolean("enable_dynamic_color", true))
                }
                SwitchItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(id = R.string.settings_dynamic_color),
                    summary = stringResource(id = R.string.settings_dynamic_color_summary),
                    checked = enableDynamicColor
                ) { checked ->
                    activity?.setDynamicColor(checked)
                    enableDynamicColor = checked
                }

                if (!enableDynamicColor) {
                    var accentIndex by rememberSaveable {
                        mutableIntStateOf(prefs.getInt("accent_color_index", -1))
                    }
                    val accentDialog = rememberCustomDialog { dismiss ->
                        AlertDialog(
                            onDismissRequest = { dismiss() },
                            confirmButton = {
                                TextButton(onClick = { dismiss() }) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            },
                            title = { Text(stringResource(R.string.settings_accent_color)) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    accentIndex = -1
                                                    activity?.setAccentColorIndex(-1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                stringResource(R.string.system_default).take(1),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    ACCENT_PRESETS.chunked(5).forEach { rowColors ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            rowColors.forEachIndexed { colIdxInRow, color ->
                                                val idx = ACCENT_PRESETS.indexOf(color)
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                        .border(
                                                            width = if (accentIndex == idx) 2.dp else 0.dp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            shape = CircleShape
                                                        )
                                                        .padding(4.dp)
                                                        .clickable {
                                                            accentIndex = idx
                                                            activity?.setAccentColorIndex(idx)
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text(stringResource(id = R.string.settings_accent_color)) },
                        supportingContent = {
                            val idx = accentIndex
                            val color = if (idx in ACCENT_PRESETS.indices) ACCENT_PRESETS[idx] else null
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (color == null) {
                                    Text(stringResource(R.string.system_default))
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.settings_accent_custom))
                                }
                            }
                        },
                        modifier = Modifier.clickable { accentDialog.show() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = { Text(
                text = stringResource(R.string.customization),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            ) }, navigationIcon = {
            IconButton(
                onClick = onBack
            ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Preview
@Composable
private fun CustomizationPreview() {
    CustomizationScreen(EmptyDestinationsNavigator)
}
