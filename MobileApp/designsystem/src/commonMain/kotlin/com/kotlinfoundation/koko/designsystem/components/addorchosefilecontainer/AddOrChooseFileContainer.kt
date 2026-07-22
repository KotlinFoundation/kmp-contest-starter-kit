package com.kotlinfoundation.koko.designsystem.components.addorchosefilecontainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlinfoundation.koko.designsystem.components.AppButton
import com.kotlinfoundation.koko.designsystem.components.AsyncImageWithShimmer
import com.kotlinfoundation.koko.designsystem.components.ButtonSize
import com.kotlinfoundation.koko.designsystem.components.ButtonStyle
import com.kotlinfoundation.koko.designsystem.components.HorizontalScrollableList
import com.kotlinfoundation.koko.designsystem.components.IconSource
import com.kotlinfoundation.koko.designsystem.components.IconTitleDescriptionCard
import com.kotlinfoundation.koko.designsystem.components.IconWrapper
import com.kotlinfoundation.koko.designsystem.components.LoadingProgress
import com.kotlinfoundation.koko.designsystem.components.LoadingProgressMode
import com.kotlinfoundation.koko.designsystem.generated.resources.UiRes
import com.kotlinfoundation.koko.designsystem.generated.resources.add_image_description
import com.kotlinfoundation.koko.designsystem.generated.resources.add_image_title
import com.kotlinfoundation.koko.designsystem.generated.resources.add_video_description
import com.kotlinfoundation.koko.designsystem.generated.resources.add_video_title
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_add_image
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_add_more_images
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_add_more_videos
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_add_video
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_change_image
import com.kotlinfoundation.koko.designsystem.generated.resources.btn_change_video
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_close
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_file_image
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_file_video
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_image_plus
import com.kotlinfoundation.koko.designsystem.generated.resources.ic_video_plus
import com.kotlinfoundation.koko.designsystem.generated.resources.image_selected_description
import com.kotlinfoundation.koko.designsystem.generated.resources.image_selected_title
import com.kotlinfoundation.koko.designsystem.generated.resources.video_selected_description
import com.kotlinfoundation.koko.designsystem.generated.resources.video_selected_title
import com.kotlinfoundation.koko.designsystem.theme.AppTheme
import com.kotlinfoundation.koko.designsystem.util.PreviewHelper
import com.kotlinfoundation.koko.designsystem.util.UiText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOrChooseFileContainer(
    uiState: AddOrChooseFileContainerUiState =
        AddOrChooseFileContainerUiState(
            mode = FileSelectionMode.Single(FileUiType.IMAGE),
        ),
    onClickCaptureOrRecord: () -> Unit = {},
    onClickSelectFromGallery: () -> Unit = {},
    onFileRemoved: (FileItemUiState) -> Unit = {},
    isCaptureSupported: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val fileSelectionMode = uiState.mode
    var isChooseFileSourceModalVisible by rememberSaveable { mutableStateOf(false) }
    val files = uiState.files
    val maxAllowed = uiState.maxAllowed

    val noFilesSelected = uiState.noFilesSelected
    val isActionButtonVisible =
        when (fileSelectionMode) {
            is FileSelectionMode.Single -> true
            is FileSelectionMode.Multiple -> files.size < maxAllowed
        }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing),
    ) {
        val containerModifier = Modifier.fillMaxWidth().height(200.dp)
        when (noFilesSelected) {
            true -> {
                EmptyState(
                    modifier = containerModifier,
                    type = fileSelectionMode.type,
                    emptyStateTitleUiText = uiState.emptyStateTitleUiText,
                    emptyStateDescriptionUiText = uiState.emptyStateDescriptionUiText,
                    onClick = {
                        isChooseFileSourceModalVisible = true
                    },
                )
            }

            else -> {
                FileListSection(
                    modifier = containerModifier,
                    type = fileSelectionMode.type,
                    files = files,
                    maxAllowed = maxAllowed,
                    titleUiText = uiState.nonEmptyStateTitleUiText,
                    descriptionUiText = uiState.nonEmptyStateDescriptionUiText,
                    onClickAddNew = {
                        isChooseFileSourceModalVisible = true
                    },
                    onClickDelete = {
                        onFileRemoved(it)
                    },
                )
            }
        }

        val buttonTextRes =
            getActionButtonTextRes(mode = fileSelectionMode, noFilesSelected = noFilesSelected)
        val actionButtonText =
            when {
                fileSelectionMode is FileSelectionMode.Multiple && !noFilesSelected ->
                    stringResource(
                        buttonTextRes,
                        files.size,
                        maxAllowed,
                    )

                else -> stringResource(buttonTextRes)
            }

        AnimatedVisibility(isActionButtonVisible) {
            AppButton(
                size = ButtonSize.SMALL,
                style = if (noFilesSelected) ButtonStyle.PRIMARY else ButtonStyle.TEXT,
                text = actionButtonText,
                onClick = { isChooseFileSourceModalVisible = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (isChooseFileSourceModalVisible) {
        FileSourceModal(
            onDismiss = { isChooseFileSourceModalVisible = false },
            type = fileSelectionMode.type,
            isTipsTextVisible = uiState.isHintTextVisible,
            isCaptureSupported = isCaptureSupported,
            hintUiText = uiState.hintUiText,
            onClickCaptureOrRecord = {
                isChooseFileSourceModalVisible = false
                onClickCaptureOrRecord()
            },
            onClickSelectFromGallery = {
                isChooseFileSourceModalVisible = false
                onClickSelectFromGallery()
            },
        )
    }
}

@Composable
fun UserInputWithFileChooser(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    label: UiText = UiText.empty(),
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 2,
    fileChooserUiState: AddOrChooseFileContainerUiState =
        AddOrChooseFileContainerUiState(
            mode = FileSelectionMode.Single(FileUiType.IMAGE),
        ),
    onClickCaptureOrRecord: () -> Unit = {},
    onClickSelectFromGallery: () -> Unit = {},
    onFileRemoved: (FileItemUiState) -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        when {
            isFocused -> AppTheme.colors.primary
            else -> Color.Transparent
        },
        animationSpec = tween(),
    )

    val fileSelectionMode = fileChooserUiState.mode
    val type = fileSelectionMode.type
    var isChooseFileSourceModalVisible by rememberSaveable { mutableStateOf(false) }
    val files = fileChooserUiState.files
    val maxAllowed = fileChooserUiState.maxAllowed

    Box(
        modifier =
        modifier
            .clip(shape)
            .border(2.dp, borderColor, shape)
            .background(
                color = AppTheme.colors.textInput.background,
                shape = shape,
            ).padding(vertical = 18.dp, horizontal = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.groupedVerticalElementSpacing)) {
            AnimatedVisibility(!files.isEmpty()) {
                HorizontalScrollableList {
                    items(files, key = { it.nameWithExtension }) { fileItem ->
                        FileItem(
                            item = fileItem,
                            fileUiType = type,
                            onClickDelete = { onFileRemoved(fileItem) },
                        )
                    }
                    if (files.size < maxAllowed) {
                        item {
                            AddOrLoadingFileItem(
                                type = type,
                                isLoading = false,
                                onClickAddNew = {
                                    isChooseFileSourceModalVisible = true
                                },
                            )
                        }
                    }
                }
            }

            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                maxLines = maxLines,
                minLines = minLines,
                textStyle =
                AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.textInput.textIcon,
                ),
                value = value,
                cursorBrush = SolidColor(AppTheme.colors.text.primary),
                interactionSource = interactionSource,
                onValueChange = onValueChange,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = label.value,
                            style = AppTheme.typography.bodyExtraLarge,
                            fontWeight = FontWeight.Normal,
                            color = AppTheme.colors.textInput.placeholder,
                        )
                    }
                    innerTextField()
                },
            )

            val icon =
                when (type) {
                    FileUiType.IMAGE -> UiRes.drawable.ic_file_image
                    FileUiType.VIDEO -> UiRes.drawable.ic_file_video
                }

            IconWrapper(
                modifier =
                Modifier
                    .align(Alignment.End)
                    .size(20.dp)
                    .clickable {
                        isChooseFileSourceModalVisible = true
                    },
                tint = AppTheme.colors.textInput.textIcon,
                icon = IconSource.of(icon),
                contentDescription = null,
            )
        }

        if (isChooseFileSourceModalVisible) {
            FileSourceModal(
                onDismiss = { isChooseFileSourceModalVisible = false },
                type = fileSelectionMode.type,
                isTipsTextVisible = fileChooserUiState.isHintTextVisible,
                hintUiText = fileChooserUiState.hintUiText,
                onClickCaptureOrRecord = {
                    isChooseFileSourceModalVisible = false
                    onClickCaptureOrRecord()
                },
                onClickSelectFromGallery = {
                    isChooseFileSourceModalVisible = false
                    onClickSelectFromGallery()
                },
            )
        }
    }
}

@Composable
private fun FileListSection(
    modifier: Modifier = Modifier,
    type: FileUiType,
    titleUiText: UiText?,
    descriptionUiText: UiText?,
    files: List<FileItemUiState>,
    maxAllowed: Int,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onClickAddNew: () -> Unit,
    onClickDelete: (FileItemUiState) -> Unit,
) {
    val (defaultStateTitle, defaultStateDescription) =
        when (type) {
            FileUiType.IMAGE -> UiRes.string.image_selected_title to UiRes.string.image_selected_description
            FileUiType.VIDEO -> UiRes.string.video_selected_title to UiRes.string.video_selected_description
        }

    IconTitleDescriptionCard(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        iconContent = {
            HorizontalScrollableList {
                items(files, key = { it.nameWithExtension }) { fileItem ->
                    FileItem(
                        item = fileItem,
                        fileUiType = type,
                        onClickDelete = { onClickDelete(fileItem) },
                    )
                }

                if (files.size < maxAllowed) {
                    item {
                        AddOrLoadingFileItem(
                            type = type,
                            isLoading = false,
                            onClickAddNew = onClickAddNew,
                        )
                    }
                }
            }
        },
        title = titleUiText?.value ?: stringResource(defaultStateTitle),
        description = descriptionUiText?.value ?: stringResource(defaultStateDescription),
        onClick = { },
    )
}

@Composable
private fun FileItem(
    item: FileItemUiState,
    fileUiType: FileUiType,
    modifier: Modifier = Modifier.size(ITEM_SIZE).clip(ITEM_SHAPE),
    onClickDelete: () -> Unit,
) {
    Box(modifier = modifier) {
        when {
            item.isUploading -> AddOrLoadingFileItem(type = fileUiType, isLoading = true)
            fileUiType == FileUiType.IMAGE -> ImageItemPreview(source = item.path)
            fileUiType == FileUiType.VIDEO -> VideoItemPreview(source = item.path)
            else -> println("Unknown file type for $fileUiType")
        }

        Icon(
            painter = painterResource(UiRes.drawable.ic_close),
            contentDescription = "Remove",
            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.outline.copy(alpha = 0.8f))
                .clickable { onClickDelete() }
                .padding(4.dp),
            tint =
            AppTheme.colors.text.primary
                .copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun ImageItemPreview(source: String) {
    AsyncImageWithShimmer(
        model = source,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun VideoItemPreview(source: String) {
    ImageItemPreview(source)
}

@Composable
private fun EmptyState(
    type: FileUiType,
    emptyStateTitleUiText: UiText?,
    emptyStateDescriptionUiText: UiText?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (defaultEmptyStateTitle, defaultEmptyStateDescription) =
        when (type) {
            FileUiType.IMAGE -> UiRes.string.add_image_title to UiRes.string.add_image_description
            FileUiType.VIDEO -> UiRes.string.add_video_title to UiRes.string.add_video_description
        }

    IconTitleDescriptionCard(
        modifier = modifier,
        iconContent = {
            AddOrLoadingFileItem(type = type, onClickAddNew = onClick)
        },
        title = emptyStateTitleUiText?.value ?: stringResource(defaultEmptyStateTitle),
        description =
        emptyStateDescriptionUiText?.value ?: stringResource(
            defaultEmptyStateDescription,
        ),
        onClick = onClick,
    )
}

@Composable
private fun AddOrLoadingFileItem(
    modifier: Modifier = Modifier.size(ITEM_SIZE).clip(ITEM_SHAPE),
    type: FileUiType,
    isLoading: Boolean = false,
    onClickAddNew: () -> Unit = {},
) {
    val icon =
        when (type) {
            FileUiType.IMAGE -> UiRes.drawable.ic_image_plus
            FileUiType.VIDEO -> UiRes.drawable.ic_video_plus
        }

    Box(
        modifier =
        modifier
            .background(AppTheme.colors.surfaceContainer)
            .clickable(enabled = !isLoading) { onClickAddNew() },
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            LoadingProgress(mode = LoadingProgressMode.CIRCULAR)
        } else {
            IconWrapper(
                icon = IconSource.of(icon),
                modifier = Modifier.size(48.dp),
                tint = AppTheme.colors.outline,
                contentDescription = "Add new file",
            )
        }
    }
}

private fun getActionButtonTextRes(
    mode: FileSelectionMode,
    noFilesSelected: Boolean,
): StringResource = when (mode) {
    is FileSelectionMode.Single ->
        when (mode.type) {
            FileUiType.IMAGE -> if (noFilesSelected) UiRes.string.btn_add_image else UiRes.string.btn_change_image
            FileUiType.VIDEO -> if (noFilesSelected) UiRes.string.btn_add_video else UiRes.string.btn_change_video
        }

    is FileSelectionMode.Multiple ->
        when (mode.type) {
            FileUiType.IMAGE -> if (noFilesSelected) UiRes.string.btn_add_image else UiRes.string.btn_add_more_images
            FileUiType.VIDEO -> if (noFilesSelected) UiRes.string.btn_add_video else UiRes.string.btn_add_more_videos
        }
}

private val ITEM_SIZE = 80.dp
private val ITEM_SHAPE = RoundedCornerShape(20.dp)

@Composable
@Preview
internal fun AddOrChooseFileContainerPreview() {
    PreviewHelper {
        AddOrChooseFileContainer()
    }
}
