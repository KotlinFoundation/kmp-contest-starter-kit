package com.kotlinfoundation.koko.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AgreePrivacyPolicyPreview
import com.kotlinfoundation.koko.designsystem.components.AnimatedHorizontalPagerPreview
import com.kotlinfoundation.koko.designsystem.components.AppButtonPreviews
import com.kotlinfoundation.koko.designsystem.components.AppCardContainerPreview
import com.kotlinfoundation.koko.designsystem.components.AppToolbarPreview
import com.kotlinfoundation.koko.designsystem.components.AuthButtonsPreview
import com.kotlinfoundation.koko.designsystem.components.AutoResizableTextPreview
import com.kotlinfoundation.koko.designsystem.components.ChipPreview
import com.kotlinfoundation.koko.designsystem.components.CircleButtonWithStepsPreview
import com.kotlinfoundation.koko.designsystem.components.Divider
import com.kotlinfoundation.koko.designsystem.components.DividerPreview
import com.kotlinfoundation.koko.designsystem.components.EmptyContentPreview
import com.kotlinfoundation.koko.designsystem.components.HorizontalPagerIndicatorPreview
import com.kotlinfoundation.koko.designsystem.components.HorizontalScrollableListPreview
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressPreview
import com.kotlinfoundation.koko.designsystem.components.LogoImagePreview
import com.kotlinfoundation.koko.designsystem.components.RadioButtonPreview
import com.kotlinfoundation.koko.designsystem.components.ScreenTitle
import com.kotlinfoundation.koko.designsystem.components.SectionContainerPreview
import com.kotlinfoundation.koko.designsystem.components.SettingsItemPreview
import com.kotlinfoundation.koko.designsystem.components.TitlesPreview
import com.kotlinfoundation.koko.designsystem.components.UserInput
import com.kotlinfoundation.koko.designsystem.components.UserInputPreview
import com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer.AddOrChooseFileContainerPreview
import com.kotlinfoundation.koko.designsystem.components.bottomnav.BottomNavigationBarPreview
import com.kotlinfoundation.koko.designsystem.components.modals.AppDialogPreview
import com.kotlinfoundation.koko.designsystem.components.modals.AppModalBottomSheetPreview
import com.kotlinfoundation.koko.designsystem.components.modals.DeleteUserConfirmationPreview
import com.kotlinfoundation.koko.designsystem.components.modals.NativeDialogPreview
import com.kotlinfoundation.koko.designsystem.components.modals.UpgradePremiumDialogPreview
import com.kotlinfoundation.koko.designsystem.components.premium.CurrentSubscriptionPlanAndFeaturesPreview
import com.kotlinfoundation.koko.designsystem.components.premium.ManageSubscriptionTextPreview
import com.kotlinfoundation.koko.designsystem.components.premium.PremiumFeaturesListPreview
import com.kotlinfoundation.koko.designsystem.components.premium.SuccessfulPurchaseContentPreview
import com.kotlinfoundation.koko.designsystem.components.premium.UpgradePremiumBannerPreview
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.theme.LocalThemeIsDark

data class ComponentItem(
    val title: String,
    val content: @Composable () -> Unit,
)

private val allComponentItems =
    listOf(
        ComponentItem("AddOrChooseFileContainer") { AddOrChooseFileContainerPreview() },
        ComponentItem("Radio Button") { RadioButtonPreview() },
        ComponentItem("Premium Features List") { PremiumFeaturesListPreview() },
        ComponentItem("Subscription Plan") { CurrentSubscriptionPlanAndFeaturesPreview() },
        ComponentItem("Successful Purchase View") { SuccessfulPurchaseContentPreview() },
        ComponentItem("User Inputs") { UserInputPreview() },
        ComponentItem("Chips") { ChipPreview() },
        ComponentItem("Buttons") { AppButtonPreviews() },
        ComponentItem("Social Auth Buttons") { AuthButtonsPreview() },
        ComponentItem("Divider") { DividerPreview() },
        ComponentItem("Loading Bar") { LoadingProgressPreview() },
        ComponentItem("App Toolbar") { AppToolbarPreview() },
        ComponentItem("Bottom Navigation") { BottomNavigationBarPreview() },
        ComponentItem("Horizontal Scrollable List") { HorizontalScrollableListPreview() },
        ComponentItem("Upgrade Premium Banner") { UpgradePremiumBannerPreview() },
        ComponentItem("Dialog") {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.verticalListItemSpacing)) {
                NativeDialogPreview()
                AppDialogPreview()
                UpgradePremiumDialogPreview()
                DeleteUserConfirmationPreview()
            }
        },
        ComponentItem("Modal Bottom Sheet") { AppModalBottomSheetPreview() },
        ComponentItem("Titles") { TitlesPreview() },
        ComponentItem("Settings Items") { SettingsItemPreview() },
        ComponentItem("Section Container") { SectionContainerPreview() },
        ComponentItem("Logo Image") { LogoImagePreview() },
        ComponentItem("Horizontal Pager Indicator") { HorizontalPagerIndicatorPreview() },
        ComponentItem("No data component") { EmptyContentPreview() },
        ComponentItem("Circle Button With Steps") { CircleButtonWithStepsPreview() },
        ComponentItem("AutoResized Text") { AutoResizableTextPreview() },
        ComponentItem("AppCard Container") { AppCardContainerPreview() },
        ComponentItem("Animated Horizontal Pager") { AnimatedHorizontalPagerPreview() },
        ComponentItem("Agree Privacy Policy Terms") { AgreePrivacyPolicyPreview() },
        ComponentItem("Manage Subscription Text") { ManageSubscriptionTextPreview() },
    )

@Composable
fun AllComponentsGallery() {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Light, 1 = Dark
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(AppTheme.spacing.defaultSpacing),
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Light Mode", Modifier.padding(16.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Dark Mode", Modifier.padding(16.dp))
            }
        }
        Divider()
        ComponentsGalleryContainer(isDarkMode = selectedTab == 1)
    }
}

@Composable
private fun ComponentsGallery() {
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredComponents by remember(allComponentItems, searchQuery) {
        derivedStateOf {
            allComponentItems.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        SideBarPanel(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            filteredComponents = filteredComponents,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = { selectedIndex = it },
        )

        // Component preview panel
        val selectedComponent = filteredComponents.getOrNull(selectedIndex)?.content
        ComponentPreviewPanel(
            modifier = Modifier.weight(1f),
            content = selectedComponent,
        )
    }
}

@Composable
private fun SideBarPanel(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredComponents: List<ComponentItem>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
) {
    Column(
        modifier =
        Modifier
            .width(250.dp)
            .fillMaxHeight()
            .background(AppTheme.colors.background)
            .padding(AppTheme.spacing.defaultSpacing),
    ) {
        UserInput(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = "Search components",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(AppTheme.spacing.defaultSpacing))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(filteredComponents) { index, item ->
                Text(
                    text = item.title,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (index == selectedIndex) {
                                AppTheme.colors.primary.copy(alpha = 0.1f)
                            } else {
                                Color.Transparent
                            },
                        ).clickable {
                            onSelectedIndexChange(index)
                        }.padding(AppTheme.spacing.defaultSpacing),
                    style =
                    AppTheme.typography.bodyExtraLarge.copy(
                        color =
                        if (index == selectedIndex) {
                            AppTheme.colors.primary
                        } else {
                            AppTheme.colors.text.primary
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun ComponentPreviewPanel(
    modifier: Modifier,
    content: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier =
        modifier
            .padding(AppTheme.spacing.defaultSpacing)
            .verticalScroll(rememberScrollState()),
    ) {
        if (content != null) {
            content()
        } else {
            EmptyContentPreview()
        }
    }
}

@Composable
private fun ComponentsGalleryContainer(isDarkMode: Boolean) {
    CompositionLocalProvider(LocalThemeIsDark provides isDarkMode) {
        AppTheme(isDarkMode = isDarkMode) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sectionSpacing),
                modifier = Modifier.padding(AppTheme.spacing.outerSpacing),
            ) {
                ScreenTitle(if (isDarkMode) "Dark Mode components" else "Light Mode components")
                ComponentsGallery()
            }
        }
    }
}
